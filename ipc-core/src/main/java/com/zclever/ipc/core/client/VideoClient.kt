package com.zclever.ipc.core.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.zclever.ipc.core.IpcManager
import com.zclever.ipc.core.debugI
import com.zclever.ipc.core.memoryfile.IpcSharedMemory
import com.zclever.ipc.core.server.VideoCenter
import com.zclever.ipc.media.IMediaConnector
import com.zclever.ipc.media.IMediaReceiver

/**
 * 媒体服务客户端
 */
internal object VideoClient : IMediaManager, ServiceConnection {

    internal fun open() {
        val mediaComponentName = ComponentName(IpcManager.packageName, VideoCenter::class.java.name)
        val mediaIntent = Intent()
        mediaIntent.component = mediaComponentName
        IpcManager.appContext.bindService(mediaIntent, VideoClient, Context.BIND_AUTO_CREATE)
    }


    private lateinit var connector: IMediaConnector

    private var previewCallBack: IPreviewCallBack? = null

    private var pictureCallBack: IPictureCallBack? = null

    private var previewIpcSharedMemory: IpcSharedMemory? = null

    private var pictureIpcSharedMemory: IpcSharedMemory? = null


    override fun takeFrame(frameType: FrameType) {
        //取帧接口
        connector.takeFrame(frameType.type)
    }

    override fun stopTakeFrame() {
        connector.stopTakeFrame()
    }

    override fun takePicture(pictureFormat: PictureFormat) {
        //取图像接口
        connector.takePicture(pictureFormat.format)

    }

    override fun setPreviewCallBack(callBack: IPreviewCallBack) {
        //取帧数据回调接口
        previewCallBack = callBack
        connector.setPreviewCallBack(PreviewReceiver)

    }

    override fun setPictureCallBack(callBack: IPictureCallBack) {
        //取拍照数据回调接口
        pictureCallBack = callBack
        connector.setPictureCallBack(PictureReceiver)

    }


    object PreviewReceiver : IMediaReceiver.Stub() {
        override fun onData(
            width: Int, height: Int, size: Int, format: Int
        ) {

            ByteArray(size).also {
                previewIpcSharedMemory?.also { ipcSharedMemory ->

                    ipcSharedMemory.inputStream().use { inputStream ->
                        inputStream.read(it)
                    }
                }
            }.let {
                previewCallBack?.onPreviewFrame(
                    it, width, height, if (format == 1) FrameType.NV21 else FrameType.H264
                )
            }
        }

    }

    object PictureReceiver : IMediaReceiver.Stub() {
        override fun onData(
            width: Int, height: Int, size: Int, format: Int
        ) {


            ByteArray(size).also {
                pictureIpcSharedMemory?.also { ipcSharedMemory ->

                    ipcSharedMemory.inputStream().use { inputStream ->
                        inputStream.read(it)
                    }
                }
            }.let {
                pictureCallBack?.onPictureTaken(
                    it, width, height, if (format == 1) PictureFormat.JPEG else PictureFormat.PNG
                )
            }

        }

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

        debugI("onServiceConnected: ->MediaClient")

        connector = IMediaConnector.Stub.asInterface(service)

        pictureIpcSharedMemory = connector.obtainPictureSharedMemory()

        previewIpcSharedMemory = connector.obtainFrameSharedMemory()

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        pictureIpcSharedMemory?.close()
        previewIpcSharedMemory?.close()
        open()
    }


}