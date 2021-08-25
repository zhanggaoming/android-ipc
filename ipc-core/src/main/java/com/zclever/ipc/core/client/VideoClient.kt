package com.zclever.ipc.core.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.MemoryFile
import android.os.SharedMemory
import android.util.Log
import com.zclever.ipc.core.*
import com.zclever.ipc.core.memoryfile.MemoryFileOpenMode
import com.zclever.ipc.core.memoryfile.MemoryFileUtil
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

    private var pictureMemoryFile: MemoryFile? = null

    private var previewMemoryFile: MemoryFile? = null

    private var pictureSharedMemory: SharedMemory? = null

    private var previewSharedMemory: SharedMemory? = null

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
                previewMemoryFile?.also { memoryFile ->

                    memoryFile.inputStream.use { inputStream ->
                        inputStream.read(it)
                    }

                } ?: previewSharedMemory!!.mapReadOnly().let { byteBuffer ->
                    byteBuffer.get(it)
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
                pictureMemoryFile?.also { memoryFile ->

                    memoryFile.inputStream.use { inputStream ->
                        inputStream.read(it)
                    }

                } ?: pictureSharedMemory!!.mapReadOnly().let { byteBuffer ->
                    byteBuffer.get(it)
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

        if (!IpcManager.useSharedMemory) {

            pictureMemoryFile = MemoryFileUtil.openMemoryFile(
                connector.obtainPictureFd(), PICTURE_DATA_LENGTH,MemoryFileOpenMode.MODE_READ
            )

            previewMemoryFile = MemoryFileUtil.openMemoryFile(
                connector.obtainFrameFd(), FRAME_DATA_LENGTH,MemoryFileOpenMode.MODE_READ
            )

        } else {

            pictureSharedMemory = connector.obtainPictureSharedMemory()

            previewSharedMemory = connector.obtainFrameSharedMemory()
        }

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        previewMemoryFile?.close()
        previewMemoryFile?.close()
        open()
    }


}