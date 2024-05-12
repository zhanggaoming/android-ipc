package com.zclever.ipc.core.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import com.zclever.ipc.IClient
import com.zclever.ipc.IConnector
import com.zclever.ipc.core.*
import com.zclever.ipc.core.memoryfile.FileDescriptorWrapper
import com.zclever.ipc.core.shared_memory.SharedMemoryFactory
import com.zclever.ipc.core.shared_memory.readJsonStr
import com.zclever.ipc.core.shared_memory.writeByteArray
import kotlin.reflect.KParameter

/**
 * 服务中心，核心Service组件
 */
class ServiceCenter : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return ConnectorStub
    }


    internal object ConnectorStub : IConnector.Stub() {

        override fun connect(baseRequest: String, requestParam: String?): String {

            val requestBaseObj = GsonInstance.fromJson<RequestBase>(baseRequest)

            debugI("server connect: $requestBaseObj thread->${Thread.currentThread().name}")

            when (requestBaseObj.type) {

                REQUEST_TYPE_CREATE -> { //创建单实例对象

                    createInstance(requestBaseObj.targetClazzName)

                }

                REQUEST_TYPE_INVOKE -> { //方法调用

                    return invokeFunction(requestBaseObj, requestParam)

                }
            }



            return ""
        }

        /**
         * 根据传过来的request来反射调用相应的方法
         */
        private fun invokeFunction(requestBase: RequestBase, requestParam: String?): String {


            //构造回调对象给服务端
            val resultCallBack: Result<*>? = if (requestBase.callbackKey != "") ServerCallBack(
                requestBase.pid, requestBase.callbackKey
            ) else null

            val requestParamObj = GsonInstance.fromJson<RequestParam>(
                if (requestBase.transformType == TransformType.BINDER) requestParam else ServiceCache.clientSharedMemoryMap[requestBase.pid]!!.readJsonStr(
                    requestBase.paramValueBytesLen
                )
            )

            debugD("server requestParamObj->${requestParamObj}")


            return ServiceCache.kFunctionMap[requestBase.targetClazzName]?.get(requestBase.functionKey)
                ?.let { invokeFunction ->


                    val invokeResult =
                        invokeFunction.callBy(invokeFunction.parameters.mapIndexed { index, kParameter ->
                            kParameter to when (kParameter.kind) {
                                KParameter.Kind.INSTANCE -> {
                                    ServiceCache.kInstanceMap[requestBase.targetClazzName]
                                }

                                else -> {
                                    if (index == invokeFunction.parameters.size - 1 && resultCallBack != null) {
                                        resultCallBack //最后一个参数直接用我们构造的的回调对象给到服务端，服务端要返回给客户端必须要通过这个对象返回给客户端
                                    } else {
                                        GsonInstance.fromJson<Any>(
                                            requestParamObj.paramValueMap[kParameter.name],
                                            kParameter.type
                                        )
                                    }
                                }

                            }
                        }.toMap())


                    val resultJson = invokeResult.toJson()

                    val resultByteArray = resultJson.encodeToByteArray()

                    if (resultByteArray.size < BINDER_MAX_TRANSFORM_JSON_BYTE_ARRAY_SIZE) {
                        debugD("return use binder")
                        Response(resultJson).toJson()
                    } else {
                        debugD("return use shared memory")

                        ServiceCache.serverResponseMemoryMap[requestBase.pid]!!.writeByteArray(resultByteArray)

                        Response(null, resultByteArray.size).toJson()
                    }
                }
                ?: throw IllegalAccessException("the ${requestBase.targetClazzName} is not be registered!!")
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

        override fun registerClient(
            client: IClient?, clientPid: Int
        ) {
            debugI("registerClient: $clientPid")
            ServiceCache.remoteClients.register(client!!, clientPid)
        }

        override fun exchangeSharedMemory(
            clientPid: Int, clientFd: ParcelFileDescriptor
        ): FileDescriptorWrapper {

            ServiceCache.clientSharedMemoryMap[clientPid] = clientFd

            return FileDescriptorWrapper(SharedMemoryFactory.create(
                "ServerResponse-$clientPid", IpcManager.config.sharedMemoryCapacity
            ).let {
                ServiceCache.serverResponseMemoryMap[clientPid] = it
                it.parcelFileDescriptor
            }, SharedMemoryFactory.create(
                "ServerCallback-$clientPid", IpcManager.config.sharedMemoryCapacity
            ).let {
                ServiceCache.serverCallbackMemoryMap[clientPid] = it
                it.parcelFileDescriptor
            })

        }

        override fun unregisterClient(client: IClient?) {

            ServiceCache.remoteClients.unregister(client!!)

        }

    }
}

