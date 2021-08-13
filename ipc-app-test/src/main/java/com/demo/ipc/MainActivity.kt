package com.demo.ipc

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ipc.extend.test.InfoService
import com.zclever.ipc.core.IpcManager

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        IpcManager.register(InfoService::class) //注册服务
        IpcManager.initVideoService(VideoManager) //初始化视频服务,用于提供视频数据
    }

    fun commonJump(view: View) {
        startActivity(Intent(this, CommonActivity::class.java))
    }

    fun videoJump(view: View) {
        startActivity(Intent(this, VideoActivity::class.java))
    }


}