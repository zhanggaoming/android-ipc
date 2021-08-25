package com.zclever.ipc.core.server

import android.app.Service
import android.content.Intent
import android.os.*
import com.zclever.ipc.core.*
import com.zclever.ipc.core.memoryfile.MemoryFileOpenMode
import com.zclever.ipc.core.memoryfile.MemoryFileUtil
import com.zclever.ipc.media.IMediaConnector
import com.zclever.ipc.media.IMediaReceiver
import java.lang.ref.WeakReference
import java.nio.ByteBuffer

class VideoCenter : Service() {

    companion object {
        internal var instanceWeakReference = WeakReference<VideoCenter>(null)
    }

    init {
        instanceWeakReference = WeakReference(this)
    }


    private val connector by lazy { MediaConnector() }


    private val pictureSharedMemory by lazy {
        if (IpcManager.useSharedMemory) {
            SharedMemory.create("pictureMemoryFile", PICTURE_DATA_LENGTH)
        } else {
            null
        }
    }

    private val frameSharedMemory by lazy {
        if (IpcManager.useSharedMemory) {
            SharedMemory.create("frameMemoryFile", FRAME_DATA_LENGTH)
        } else {
            null
        }
    }


    private val pictureMemoryFile by lazy {

        if (!IpcManager.useSharedMemory) {
            MemoryFileUtil.createMemoryFile(
                "pictureMemoryFile", PICTURE_DATA_LENGTH
            )
        } else {
            null
        }
    }

    private val frameMemoryFile by lazy {

        if (!IpcManager.useSharedMemory) {
            MemoryFileUtil.createMemoryFile(
                "frameMemoryFile", FRAME_DATA_LENGTH
            )
        } else {
            null
        }

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

        override fun obtainPictureFd(): ParcelFileDescriptor? =
            pictureMemoryFile?.parcelFileDescriptor

        override fun obtainFrameFd(): ParcelFileDescriptor? = frameMemoryFile?.parcelFileDescriptor

        override fun obtainPictureSharedMemory(): SharedMemory? = pictureSharedMemory

        override fun obtainFrameSharedMemory(): SharedMemory? = frameSharedMemory
    }

    internal fun onTakePicture(
        byteArray: ByteArray, width: Int, height: Int, size: Int, pictureFormat: Int
    ) {


        (pictureMemoryFile?.also {
            it.outputStream.use { outputStream ->
                outputStream.write(byteArray)
            }
        } ?: pictureSharedMemory!!.mapReadWrite().let {
            it.position(0)
            it.put(byteArray)
        }).let {
            pictureCallBack?.onData(width, height, size, pictureFormat)
        }


    }

    internal fun onTakeFrame(byteArray: ByteArray, width: Int, height: Int, size: Int, type: Int) {

        (frameMemoryFile?.also {
            it.outputStream.use { outputStream ->
                outputStream.write(byteArray)
            }
        } ?: frameSharedMemory!!.mapReadWrite().let {
            it.position(0)
            it.put(byteArray)
        }).let {
            previewCallBack?.onData(width, height, size, type)
        }
    }


}