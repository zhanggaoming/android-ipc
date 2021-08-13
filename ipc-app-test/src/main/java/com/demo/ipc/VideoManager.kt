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

        Log.i(TAG, "takePicture: ")
        
        onTakePicture(ByteArray(10) { i ->
            (i * i).toByte()
        }, 640, 480, 10, PictureFormat.JPEG.format)

    }

    override fun takeFrame(type: Int) {
        sendFrame = true
    }

    override fun stopTakeFrame() {
        sendFrame = false
    }

    val handler = Handler(Looper.getMainLooper())


    private val runnable = object : Runnable {
        override fun run() {

            if (sendFrame) {

                onTakeFrame(ByteArray(100) { i ->
                    i.toByte()
                }, 640, 480, 100, FrameType.NV21.type)

            }

            handler.postDelayed(this, 2000)
        }

    }

    init {
        handler.post(runnable)
    }


}