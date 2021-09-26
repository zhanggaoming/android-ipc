package com.demo.ipc

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ipc.extend.test.Code
import com.ipc.extend.test.Event
import com.ipc.extend.test.InfoService
import com.ipc.extend.test.UserInfo
import com.zclever.ipc.core.Config
import com.zclever.ipc.core.IpcManager
import com.zclever.ipc.core.Result
import com.zclever.ipc.core.client.FrameType
import com.zclever.ipc.core.client.IPreviewCallBack

class CommonActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CommonActivity"
    }

    val instance by lazy { IpcManager.getService<InfoService>() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)
        IpcManager.config(Config.builder().configDebug(true).build())
        IpcManager.init(this)
        IpcManager.open("com.demo.ipcdemo")
    }

    fun syncGetUserInfo(view: View) {

        Toast.makeText(this, instance.syncGetUserInfo().toString(), Toast.LENGTH_LONG).show()

        Log.i(TAG, "syncGetUserInfo: ->${instance.getEnum(Code.FAILURE)}")

    }


    fun asyncGetUserInfo(view: View) {

        instance.asyncGetUserInfo(object : Result<UserInfo>() {

            override fun onData(data: UserInfo) {
                runOnUiThread {

                    Toast.makeText(this@CommonActivity, data.toString(), Toast.LENGTH_LONG).show()
                }

            }

        })
    }

    fun sum(view: View) {

        instance.sum(1, 2, 3, object : Result<Int>() {
            override fun onData(data: Int) {
                runOnUiThread {
                    Toast.makeText(this@CommonActivity, "the sum is $data", Toast.LENGTH_LONG)
                        .show()
                }
            }
        })
    }

    fun sendBigData(view: View) {

        instance.sendBigData(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

    }



    fun setEventCallBack(view: View) {

        instance.setEventCallBack(object : Result<Event>() {
            override fun onData(data: Event) {
                Log.i(TAG, "onData: ${data.id}")
            }
        })

    }


}
