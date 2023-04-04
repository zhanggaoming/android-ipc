package com.zclever.ipc.core.client

import com.zclever.ipc.IConnector
import com.zclever.ipc.core.*
import com.zclever.ipc.core.shared_memory.readJsonStr
import com.zclever.ipc.core.shared_memory.writeByteArray
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.kotlinFunction

/**
 * 动态代理InvocationHandler
 */
internal class ServiceInvocationHandler(
    private val connector: IConnector, private val targetClazzName: String
) : InvocationHandler {


    /**
     * 主要是把参数转成Request发出去
     */
    override fun invoke(proxy: Any, method: Method?, args: Array<out Any>?): Any? {

        return method?.kotlinFunction?.let { kotlinFunction ->

            try {
                val signature = kotlinFunction.signature()

                val callbackInstance = if (args?.last() is Result<*>) {
                    ClientCache.dataCallback[signature] = args.last() as DataCallBack
                    true
                } else {
                    false
                }


                val paramValueMap =
                    kotlinFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }
                        .mapIndexed { index, kParameter ->
                            kParameter.name!! to if (callbackInstance && index == args!!.size - 1) "" else GsonInstance.toJson(
                                args!![index]
                            )
                        }.toMap()

                val requestParamJson = GsonInstance.toJson(RequestParam(paramValueMap))

                debugD("invoke requestParamJson length ->${requestParamJson.length}, content->$requestParamJson")


                var requestBase = RequestBase(
                    targetClazzName = targetClazzName,
                    functionKey = kotlinFunction.signature(),
                    callbackKey = if (callbackInstance) signature else "",
                )

                val response =
                    if (requestParamJson.length < BINDER_MAX_TRANSFORM_JSON_LENGTH) {//binder传输

                        var responseObj = GsonInstance.fromJson<Response>(
                            connector.connect(
                                requestBase.toJson(),
                                requestParamJson
                            )
                        )

                        if (responseObj.dataByteSize > 0) {
                            Response(
                                ClientCache.serverResponseSharedMemory!!.readJsonStr(
                                    responseObj.dataByteSize
                                )
                            )//服务端返回共享内存写入结果后，客户端再读
                        } else {
                            responseObj
                        }


                    } else {//共享内存传输参数

                        requestParamJson.encodeToByteArray().let { paramByteArray ->

                            synchronized(ClientCache.clientSharedMemory!!) { //保证同步

                                ClientCache.clientSharedMemory!!.writeByteArray(paramByteArray) //把utf-8编码的字节数组写入共享内存区域

                                val responseStr = connector.connect(             //写完共享内存通知服务端读取
                                    GsonInstance.toJson(requestBase.createNoParameterRequest(
                                        paramByteArray.size
                                    ).apply { requestBase = this }), null
                                )

                                var responseObj = GsonInstance.fromJson<Response>(responseStr)

                                if (responseObj.dataStr.isNullOrEmpty()) {
                                    Response(
                                        ClientCache.serverResponseSharedMemory!!.readJsonStr(
                                            responseObj.dataByteSize
                                        )
                                    )//服务端返回共享内存写入结果后，客户端再读
                                } else {
                                    responseObj
                                }
                            }
                        }
                    }

                if (kotlinFunction.returnType.classifier == Unit::class) {
                    GsonInstance.fromJson(
                        response.dataStr,
                        kotlinFunction.returnType.classifier!!.safeAs<KClass<*>>()!!.java
                    )
                } else {
                    GsonInstance.fromJson<Any>(response.dataStr, kotlinFunction.returnType)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}