package com.demo.ipc

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ipc.extend.test.Event
import com.ipc.extend.test.InfoService
import com.zclever.ipc.core.Config
import com.zclever.ipc.core.IpcManager
import com.zclever.ipc.core.Result
import com.zclever.ipc.core.client.FrameType
import com.zclever.ipc.core.client.IPictureCallBack
import com.zclever.ipc.core.client.IPreviewCallBack
import com.zclever.ipc.core.client.PictureFormat
import kotlin.concurrent.thread

class VideoActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "VideoActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        //配置开启媒体服务
        IpcManager.config(Config.builder().configOpenMedia(true).build())
        IpcManager.init(this)
        IpcManager.open("com.demo.ipcdemo")
    }


    fun takePicture(view: View) {

        IpcManager.getMediaService().setPictureCallBack(object : IPictureCallBack {
            override fun onPictureTaken(
                data: ByteArray?, width: Int, height: Int, pictureFormat: PictureFormat
            ) {
                Log.i(TAG, "onPictureTaken: ${data.contentToString()},format->$pictureFormat，thread->${Thread.currentThread().name}")
            }
        })

        IpcManager.getMediaService().takePicture(PictureFormat.JPEG)

    }


    fun takeFrame(view: View) {

        IpcManager.getMediaService().setPreviewCallBack(object : IPreviewCallBack {
            override fun onPreviewFrame(
                data: ByteArray?, width: Int, height: Int, frameType: FrameType
            ) {
                Log.i(TAG, "onPreviewFrame: thread->${Thread.currentThread().name},size->${data?.size},format->$frameType},data->${data.contentToString()}")
            }
        })


        IpcManager.getMediaService().takeFrame(FrameType.NV21)
    }


    fun stopTakeFrame(view: View) {

        IpcManager.getMediaService().stopTakeFrame()

    }
}