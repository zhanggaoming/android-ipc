package com.zclever.ipc.core.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import com.zclever.ipc.core.IpcManager
import com.zclever.ipc.core.debugI
import com.zclever.ipc.core.inputStream
import com.zclever.ipc.core.server.VideoCenter
import com.zclever.ipc.media.IMediaCallback
import com.zclever.ipc.media.IMediaConnector


/**
 * 媒体服务客户端
 */
internal object VideoClient : IMediaManager, ServiceConnection, IMediaCallback.Stub() {

    internal fun open() {
        val mediaComponentName = ComponentName(IpcManager.packageName, VideoCenter::class.java.name)
        val mediaIntent = Intent()
        mediaIntent.component = mediaComponentName
        IpcManager.appContext.bindService(mediaIntent, VideoClient, Context.BIND_AUTO_CREATE)
    }


    private lateinit var connector: IMediaConnector

    private var previewCallBack: IPreviewCallBack? = null

    private var pictureCallBack: IPictureCallBack? = null

    private var previewIpcSharedMemory: ParcelFileDescriptor? = null

    private var pictureIpcSharedMemory: ParcelFileDescriptor? = null


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

    }

    override fun setPictureCallBack(callBack: IPictureCallBack) {
        //取拍照数据回调接口
        pictureCallBack = callBack
    }

    internal object ServerDeathRecipient : IBinder.DeathRecipient {
        override fun binderDied() {
            //反馈给客户端
            IpcManager.serverDeath?.invoke()

            connector.setMediaCallback(null)
            pictureIpcSharedMemory?.close()
            previewIpcSharedMemory?.close()

            open()
            IpcManager.openComplete?.invoke()
        }
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder) {

        debugI("onServiceConnected: ->MediaClient")

        connector = IMediaConnector.Stub.asInterface(service)

        try {
            service.linkToDeath(ServerDeathRecipient, 0)
        } catch (e: RemoteException) {

        }

        pictureIpcSharedMemory = connector.obtainPictureSharedMemory()

        previewIpcSharedMemory = connector.obtainFrameSharedMemory()

        connector.setMediaCallback(this)
    }


    override fun onServiceDisconnected(name: ComponentName?) {
        connector.setMediaCallback(null)
        pictureIpcSharedMemory?.close()
        previewIpcSharedMemory?.close()

        // open()
    }

    override fun onPicture(width: Int, height: Int, size: Int, format: Int) {

        pictureCallBack?.let { callback ->

            pictureIpcSharedMemory?.inputStream()?.use { inputStream ->

                ByteArray(size).also { inputStream.read(it) }.let { data ->

                    callback.onPictureTaken(
                        data,
                        width,
                        height,
                        if (format == 1) PictureFormat.JPEG else PictureFormat.PNG
                    )
                }
            }
        }


    }

    override fun onFrame(width: Int, height: Int, size: Int, type: Int) {
        previewCallBack?.let { callback ->

            previewIpcSharedMemory?.inputStream()?.use { inputStream ->

                ByteArray(size).also { inputStream.read(it) }.let { data ->

                    callback.onPreviewFrame(
                        data,
                        width,
                        height,
                        if (type == 1) FrameType.NV21 else FrameType.H264
                    )
                }
            }
        }
    }


}