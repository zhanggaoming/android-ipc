package com.zclever.ipc.core.server

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import com.zclever.ipc.core.*
import com.zclever.ipc.core.memoryfile.*
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

        override fun obtainPictureSharedMemory(): IpcSharedMemory = pictureSharedMemory

        override fun obtainFrameSharedMemory(): IpcSharedMemory = frameSharedMemory
    }

    internal fun onTakePicture(
        byteArray: ByteArray, width: Int, height: Int, size: Int, pictureFormat: Int
    ) {
        if (pictureSharedMemory.canWrite()){

            debugI("VideoCenter onTakePicture:width->$width,hegiht->$height,size->$size")
            pictureSharedMemory.writeVideoStruct(IpcSharedMemory.VideoStruct(false,pictureFormat,width, height, size,byteArray))

        }

    }

    internal fun onTakeFrame(byteArray: ByteArray, width: Int, height: Int, size: Int, type: Int) {

        if (frameSharedMemory.canWrite()){

            frameSharedMemory.writeVideoStruct(IpcSharedMemory.VideoStruct(false,type,width, height, size,byteArray))

        }
    }


}