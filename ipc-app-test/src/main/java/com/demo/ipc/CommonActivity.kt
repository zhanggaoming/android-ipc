package com.demo.ipc

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ipc.extend.test.*
import com.zclever.ipc.core.Config
import com.zclever.ipc.core.IpcManager
import com.zclever.ipc.core.Result
import com.zclever.ipc.core.client.FrameType
import com.zclever.ipc.core.client.IPreviewCallBack

class CommonActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CommonActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)
        IpcManager.config(Config.builder().configDebug(true).build())
        IpcManager.init(this)
        IpcManager.open("com.demo.ipcdemo"){
            runOnUiThread {
                Toast.makeText(this,"连接服务端成功，可以开始调用相关接口了", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun syncGetUserInfo(view: View) {

        Toast.makeText(this, IpcManager.getService<InfoService>().syncGetUserInfo().toString(), Toast.LENGTH_LONG).show()

    }


    fun asyncGetUserInfo(view: View) {

        IpcManager.getService<InfoService>().asyncGetUserInfo(object : Result<UserInfo>() {

            override fun onData(data: UserInfo) {
                runOnUiThread {

                    Toast.makeText(this@CommonActivity, data.toString(), Toast.LENGTH_LONG).show()
                }

            }

        })
    }

    fun sum(view: View) {

        IpcManager.getService<InfoService>().sum(1, 2, 3, object : Result<Int>() {
            override fun onData(data: Int) {
                runOnUiThread {
                    Toast.makeText(this@CommonActivity, "the sum is $data", Toast.LENGTH_LONG)
                        .show()
                }
            }
        })
    }

    fun sendBigData(view: View) {

        IpcManager.getService<InfoService>().sendBigData(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

    }


    fun setEventCallBack(view: View) {

        IpcManager.getService<InfoService>().setEventCallBack(object : Result<Event>() {
            override fun onData(data: Event) {
                Log.i(TAG, "onData: ${data.id}")
            }
        })
    }

    fun setResponeCallBack(view: View) {

        IpcManager.getService<InfoService>().setResponeCallBack(object : Result<BaseRespone<Event>>() {
            override fun onData(data: BaseRespone<Event>) {
                Log.i(TAG, "onData,BaseRespone:${data.data.id}")
            }
        })
    }


}
