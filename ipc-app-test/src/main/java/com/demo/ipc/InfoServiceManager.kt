package com.demo.ipc

import android.util.Log
import com.ipc.extend.test.*
import com.zclever.ipc.core.*
import java.lang.reflect.TypeVariable
import kotlin.concurrent.thread


object InfoServiceManager : InfoService {

    //获取userInfo，走的是回调的方式
    override fun asyncGetUserInfo(callBack: Result<UserInfo>) {
        thread {
            callBack.onData(UserInfo("asyncGetUserInfo", 20))
        }
    }


    override fun syncGetUserInfo(): UserInfo {
        return UserInfo("syncGetUserInfo", 18)
    }


    override fun sum(a: Int, b: Int, c: Int, result: Result<Int>) {
        result.onData(a + b + c)
    }

    override fun sendBigData(data: ByteArray) {
        Log.i(TAG, "sendBigData: ${data.contentToString()}")
    }

    override fun getEnum(code: Code): Code {
        Log.i(TAG, "getEnum: $code")
        return Code.SUCCESS
    }

    private var count = 0

    private var mCallBack: Result<Event>? = null

    init {

        thread {
            while (true) {
                mCallBack?.onData(Event(count++))
                responeCallBack?.onData(BaseRespone(Event(id = count)))
                Thread.sleep(2000)
            }
        }
    }

    override fun setEventCallBack(callBack: Result<Event>) {
        mCallBack = callBack
    }

    private var responeCallBack: Result<BaseRespone<Event>>? = null

    override fun setResponeCallBack(callBack: Result<BaseRespone<Event>>) {
        responeCallBack = callBack
    }


    override fun transformAreaBeans(data: ArrayList<AreaBean>): Int {

        data.forEach {
            debugI(it.toString())
        }
        return 1
    }
}