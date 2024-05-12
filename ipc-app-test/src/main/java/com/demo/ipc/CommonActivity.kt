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
import kotlin.concurrent.thread

class CommonActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CommonActivity"

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)
        IpcManager.config(Config.builder().configDebug(true).configSharedMemoryCapacity(2*1024*1024).build())
        IpcManager.init(this)
        IpcManager.open("com.demo.ipcdemo") {
            runOnUiThread {
                Toast.makeText(this, "连接服务端成功，可以开始调用相关接口了", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun syncGetUserInfo(view: View) {

        Toast.makeText(
            this,
            IpcManager.getService<InfoService>().syncGetUserInfo().toString(),
            Toast.LENGTH_LONG
        ).show()

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

    val bigByteArray = ByteArray(1024 * 1024) {
        it.toByte()
    }

    fun sendBigData(view: View) {

        thread {
            IpcManager.getService<InfoService>().sendBigData(bigByteArray)
        }

    }


    fun setEventCallBack(view: View) {

        IpcManager.getService<InfoService>().setEventCallBack(object : Result<Event>() {
            override fun onData(data: Event) {
                Log.i(TAG, "onData: ${data.id}")
            }
        })
    }

    fun setResponseCallBack(view: View) {

        IpcManager.getService<InfoService>()
            .setResponseCallBack(object : Result<BaseResponse<Event>>() {
                override fun onData(data: BaseResponse<Event>) {
                    Log.i(TAG, "onData,BaseRespone:${data.data.id}")
                }
            })
    }

    fun transformAreaBeanList(view: View) {

        val data =
            arrayListOf(AreaBean().apply { areaId = 1 }, AreaBean().apply { areaId = 2 })

        IpcManager.getService<InfoService>().transformAreaBeans(data)

    }

    fun getBigByteArray(view: View) {

        thread {
            val data = IpcManager.getService<InfoService>().getBigByteArray()


            Log.i(
                TAG,
                "getBigByteArray size->${data.size}, data(0,20)->${
                    data.copyOfRange(0, 20).contentToString()
                }"
            )
        }

    }

    fun asyncGetBigByteArray(view: View) {

        IpcManager.getService<InfoService>().asyncGetBigByteArray(object : Result<ByteArray>() {
            override fun onData(data: ByteArray) {
                Log.i(
                    TAG,
                    "asyncGetBigByteArray size->${data.size}, data(0,20)->${
                        data.copyOfRange(0, 20).contentToString()
                    }"
                )
            }
        })


    }


}

abstract class ComparableCallback<T:Comparable<T>>:Result<T>()
