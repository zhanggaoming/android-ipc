package com.zclever.ipc.core.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.zclever.ipc.IClient
import com.zclever.ipc.IConnector
import com.zclever.ipc.core.*
import com.zclever.ipc.core.GsonInstance
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

/**
 * 服务中心，核心Service组件
 */
class ServiceCenter : Service() {


    override fun onBind(intent: Intent?): IBinder? {
        return ConnectorStub
    }


    internal object ConnectorStub : IConnector.Stub() {

        override fun connect(request: String?): String {

            val requestObj: Request = GsonInstance.fromJson(request, Request::class.java).safeAs<Request>()!!

            debugI("connect: $requestObj")

            when (requestObj.type) {

                REQUEST_TYPE_CREATE -> { //创建单实例对象

                    createInstance(requestObj.targetClazzName)

                }

                REQUEST_TYPE_INVOKE -> { //方法调用


                    return invokeFunction(requestObj)

                }

                else -> {

                }

            }

            return ""
        }

        /**
         * 根据传过来的request来反射调用相应的方法
         */
        private fun invokeFunction(request: Request): String {


            val resultCallBack: Result<Any>? =
                if (request.invokeID >= 0 /*&& request.dataType.isNotEmpty()*/) ServiceCallBack(
                    request.pid, request.invokeID, request.dataType
                ) else null


            return ServiceCache.kFunctionMap[request.targetClazzName]?.get(request.functionKey)
                ?.let { invokeFunction ->

                    GsonInstance.toJson(invokeFunction.callBy(invokeFunction.parameters.mapIndexed { index, kParameter ->
                        kParameter to when (kParameter.kind) {
                            KParameter.Kind.INSTANCE -> {
                                ServiceCache.kInstanceMap[request.targetClazzName]
                            }

                            else -> {
                                if (index == invokeFunction.parameters.size - 1 && resultCallBack != null) {
                                    resultCallBack //最后一个参数直接用我们构造的的回调对象，服务端要返回给客户端必须要通过这个参数返回
                                } else {
                                    GsonInstance.fromJson(
                                        request.valueParametersMap[kParameter.name],
                                        kParameter.type.classifier!!.safeAs<KClass<*>>()!!.java
                                    )
                                }
                            }

                        }
                    }.toMap()))

                }
                ?: throw IllegalAccessException("the ${request.targetClazzName} is not be registered!!")
        }

        private fun createInstance(targetClazzName: String) {

            //将实例缓存到InstanceMap里面
            ServiceCache.kInstanceMap[targetClazzName] =
                Class.forName(targetClazzName).kotlin.objectInstance //Kotlin写服务直接用Object
                    ?: ServiceCache.kFunctionMap[targetClazzName]?.let { functionMap ->
                        Log.i(TAG, "createInstance: $functionMap")
                        functionMap["getInstance()"]?.call() //Java写服务的需要实现静态的getInstance单例方法
                    }

        }

        override fun registerClient(client: IClient?, clientPid: Int) {
            debugI("registerClient: $clientPid")
            ServiceCache.remoteClients.register(client!!, clientPid)
        }

        override fun unregisterClient(client: IClient?) {
            ServiceCache.remoteClients.unregister(client!!)
        }

    }
}

