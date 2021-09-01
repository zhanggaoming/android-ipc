package com.zclever.ipc.core.server

import android.app.Service
import android.content.Intent
import android.os.*
import com.zclever.ipc.core.*
import com.zclever.ipc.core.memoryfile.IpcSharedMemory
import com.zclever.ipc.media.IMediaConnector
import com.zclever.ipc.media.IMediaReceiver
import java.lang.ref.WeakReference

class VideoCenter : Service() {

    companion object {
        internal var instanceWeakReference = WeakReference<VideoCenter>(null)
    }

    init {
        instanceWeakReference = WeakReference(this)
    }


    private val connector by lazy { MediaConnector() }


    private val pictureSharedMemory by lazy {
        IpcSharedMemory.create(IpcManager.config.mediaMemoryCapacity)
    }

    private val frameSharedMemory by lazy {
        IpcSharedMemory.create(IpcManager.config.mediaMemoryCapacity)
    }

    var pictureCallBack: IMediaReceiver? = null

    var previewCallBack: IMediaReceiver? = null


    override fun onBind(intent: Intent?): IBinder = connector

    private inner class MediaConnector : IMediaConnector.Stub() {

        private val previewDeathRecipient = IBinder.DeathRecipient {
            this@VideoCenter.previewCallBack = null
        }

        private val pictureDeathRecipient = IBinder.DeathRecipient {
            this@VideoCenter.pictureCallBack = null
        }

        override fun takePicture(format: Int) {
            ServiceCache.videoService!!.takePicture(format)
        }


        override fun takeFrame(type: Int) {
            ServiceCache.videoService!!.takeFrame(type)
        }

        override fun stopTakeFrame() {
            ServiceCache.videoService!!.stopTakeFrame()
        }


        override fun setPreviewCallBack(previewCallBack: IMediaReceiver?) {
            this@VideoCenter.previewCallBack?.asBinder()?.unlinkToDeath(previewDeathRecipient, 0)
            this@VideoCenter.previewCallBack = previewCallBack!!
            this@VideoCenter.previewCallBack?.asBinder()?.linkToDeath(previewDeathRecipient, 0)
        }

        override fun setPictureCallBack(pictureCallBack: IMediaReceiver?) {
            this@VideoCenter.pictureCallBack?.asBinder()?.unlinkToDeath(pictureDeathRecipient, 0)
            this@VideoCenter.pictureCallBack = pictureCallBack!!
            this@VideoCenter.pictureCallBack?.asBinder()?.linkToDeath(pictureDeathRecipient, 0)
        }


        override fun obtainPictureSharedMemory(): IpcSharedMemory = pictureSharedMemory

        override fun obtainFrameSharedMemory(): IpcSharedMemory = frameSharedMemory
    }

    internal fun onTakePicture(
        byteArray: ByteArray, width: Int, height: Int, size: Int, pictureFormat: Int
    ) {

        pictureSharedMemory.outputStream().use {
            it.write(byteArray)
        }

        pictureCallBack?.onData(width, height, size, pictureFormat)

    }

    internal fun onTakeFrame(byteArray: ByteArray, width: Int, height: Int, size: Int, type: Int) {

        frameSharedMemory.outputStream().use {
            it.write(byteArray)
        }
        previewCallBack?.onData(width, height, size, type)

    }


}