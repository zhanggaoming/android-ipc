package com.zclever.ipc.core.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.ParcelFileDescriptor
import com.zclever.ipc.core.IpcManager
import com.zclever.ipc.core.shared_memory.SharedMemoryFactory
import com.zclever.ipc.core.shared_memory.writeByteArray
import com.zclever.ipc.media.IMediaCallback
import com.zclever.ipc.media.IMediaConnector
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
        SharedMemoryFactory.create("picture", IpcManager.config.mediaMemoryCapacity)
    }

    private val frameSharedMemory by lazy {
        SharedMemoryFactory.create("frame", IpcManager.config.mediaMemoryCapacity)
    }

    var mediaCallback: IMediaCallback? = null

    override fun onBind(intent: Intent?): IBinder = connector

    private inner class MediaConnector : IMediaConnector.Stub() {

        override fun takePicture(format: Int) {
            ServiceCache.videoService!!.takePicture(format)
        }

        override fun takeFrame(type: Int) {
            ServiceCache.videoService!!.takeFrame(type)
        }

        override fun stopTakeFrame() {
            ServiceCache.videoService!!.stopTakeFrame()
        }

        override fun obtainPictureSharedMemory(): ParcelFileDescriptor =
            pictureSharedMemory.parcelFileDescriptor

        override fun obtainFrameSharedMemory(): ParcelFileDescriptor =
            frameSharedMemory.parcelFileDescriptor

        override fun setMediaCallback(callback: IMediaCallback?) {
            mediaCallback = callback
        }

    }

    internal fun onTakePicture(
        byteArray: ByteArray, width: Int, height: Int, size: Int, pictureFormat: Int
    ) {
        pictureSharedMemory.writeByteArray(byteArray)

        mediaCallback?.onPicture(width, height, size, pictureFormat)
    }

    internal fun onTakeFrame(byteArray: ByteArray, width: Int, height: Int, size: Int, type: Int) {

        frameSharedMemory.writeByteArray(byteArray)

        mediaCallback?.onFrame(width, height, size, type)

    }


}