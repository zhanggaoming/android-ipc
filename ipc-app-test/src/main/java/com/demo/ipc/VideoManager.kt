package com.demo.ipc

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.zclever.ipc.core.client.FrameType
import com.zclever.ipc.core.client.PictureFormat
import com.zclever.ipc.core.server.VideoService

/**
 * 这个类是模拟发送数据
 */
object VideoManager : VideoService() {

    private const val TAG = "VideoManager"

    @Volatile
    var sendFrame = false

    override fun takePicture(pictureFormat: Int) {

        jpegPictureData?.let {
            onTakePicture(it, 1410, 882, it.size, PictureFormat.JPEG.format)
        }
    }

    override fun takeFrame(type: Int) {
        sendFrame = true
    }

    override fun stopTakeFrame() {
        sendFrame = false
    }

    val handler = Handler(Looper.getMainLooper())

    val frameData = ByteArray(10) { i ->
        i.toByte()
    }

    private val runnable = object : Runnable {
        override fun run() {

            if (sendFrame) {
                frameData[0]++

                onTakeFrame(
                    frameData, 640, 480, 10, FrameType.NV21.type
                )

            }

            handler.postDelayed(this, 150)
        }

    }

    init {
        handler.post(runnable)
    }

    var jpegPictureData: ByteArray? = null

}