package com.demo.ipc

import android.util.Log
import com.ipc.extend.test.*
import com.zclever.ipc.core.*
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
        Log.i(TAG, "sendBigData 0-20: ${data.copyOfRange(0, 20).contentToString()},size->${data.size}")
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
                responseCallBack?.onData(BaseResponse(Event(id = count)))
                Thread.sleep(2000)
            }
        }
    }

    override fun setEventCallBack(callBack: Result<Event>) {
        mCallBack = callBack
    }

    private var responseCallBack: Result<BaseResponse<Event>>? = null

    override fun setResponseCallBack(callBack: Result<BaseResponse<Event>>) {
        responseCallBack = callBack
    }


    override fun transformAreaBeans(data: ArrayList<AreaBean>): Int {

        data.forEachIndexed { index, bean ->
            Log.i(TAG, "transformAreaBeans: data[$index]->$bean")
        }


        return 1
    }

    override fun getBigByteArray(): ByteArray {
        return ByteArray(800_000)
    }

    private var asyncBigByteArrayCallback:Result<ByteArray>?=null

    override fun asyncGetBigByteArray(callBack: Result<ByteArray>) {
        asyncBigByteArrayCallback=callBack
        thread {
            asyncBigByteArrayCallback?.onData(ByteArray(1024*1024*2))
        }
    }
}