package com.demo.ipc

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ipc.extend.test.InfoService
import com.ipc.extend.test.UserInfo
import com.zclever.ipc.core.IpcManager
import com.zclever.ipc.core.Result
import com.zclever.ipc.core.client.FrameType
import com.zclever.ipc.core.client.IPreviewCallBack

class CommonActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CommonActivity"
    }

    val instance by lazy { IpcManager.getDefault(InfoService::class) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)
        IpcManager.init(this)
        IpcManager.open("com.demo.ipcdemo")

    }

    fun syncGetUserInfo(view: View) {

        Toast.makeText(this, instance.syncGetUserInfo().toString(), Toast.LENGTH_LONG).show()

    }


    fun asyncGetUserInfo(view: View) {

        instance.asyncGetUserInfo(object : Result<UserInfo>() {

            override fun onSuccess(data: UserInfo) {
                runOnUiThread {

                    Toast.makeText(this@CommonActivity, data.toString(), Toast.LENGTH_LONG).show()
                }

            }

            override fun onFailure(message: String) {

                runOnUiThread {

                    Toast.makeText(
                        this@CommonActivity, "asyncGetUserInfo failed", Toast.LENGTH_LONG
                    ).show()
                }

            }

        })
    }

    fun sum(view: View) {

        instance.sum(1, 2, 3, object : Result<Int>() {
            override fun onSuccess(data: Int) {
                runOnUiThread {
                    Toast.makeText(this@CommonActivity, "the sum is $data", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onFailure(message: String) {
                runOnUiThread {

                    Toast.makeText(this@CommonActivity, "sum failed", Toast.LENGTH_LONG).show()

                }
            }

        })

    }


}
