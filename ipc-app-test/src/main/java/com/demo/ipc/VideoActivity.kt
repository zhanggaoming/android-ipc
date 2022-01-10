package com.demo.ipc

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodSession
import android.widget.ImageView
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

class VideoActivity : AppCompatActivity(){

    companion object {
        private const val TAG = "VideoActivity"
    }

    private lateinit var showImageView:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        //配置开启媒体服务
        IpcManager.config(Config.builder().configDebug(true).configOpenMedia(true).build())
        IpcManager.init(this)
        IpcManager.open("com.demo.ipcdemo")

        showImageView=findViewById(R.id.img_show)

        Handler(Looper.getMainLooper()).postDelayed(
            {
                IpcManager.getService<InfoService>().setEventCallBack(object : Result<Event>() {
                    override fun onData(data: Event) {
                        Log.i(TAG, "onData: ${data.id}")
                    }

                })
            },3000
        )
    }


    fun takePicture(view: View) {

        IpcManager.getMediaService().setPictureCallBack(object : IPictureCallBack {
            override fun onPictureTaken(
                data: ByteArray?, width: Int, height: Int, pictureFormat: PictureFormat
            ) {

                data?.let {

                    Log.i(TAG, "data: size->${data.size},format->$pictureFormat，thread->${Thread.currentThread().name}")

                    BitmapFactory.decodeByteArray(data,0,data.size).let {

                        runOnUiThread {

                            showImageView.setImageBitmap(it)


                        }
                    }

                }


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