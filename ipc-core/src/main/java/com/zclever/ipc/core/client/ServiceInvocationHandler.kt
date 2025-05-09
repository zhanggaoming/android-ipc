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

                val bigDataIndex = kotlinFunction.bigDataIndex() - 1

                val useBigData = bigDataIndex >= 0

                var bigDataParamName = ""

                val callbackInstance = if (args?.last() is Result<*>) {
                    ClientCache.dataCallback[signature] = args.last() as DataCallBack
                    true
                } else {
                    false
                }

                debugD("invoke: args->${args.contentToString()},bigDataIndex->$bigDataIndex")


                val paramValueMap =
                    kotlinFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }
                        .mapIndexed { index, kParameter ->
                            if (index == bigDataIndex) {
                                bigDataParamName = kParameter.name!!
                            }
                            kParameter.name!! to if ((callbackInstance && index == args!!.size - 1) || bigDataIndex == index) "" else GsonInstance.toJson(
                                args!![index]
                            )
                        }.toMap().toMutableMap()

                if (useBigData) {
                    val bigData = args!![bigDataIndex].safeAs<ByteArray>()!!
                    paramValueMap[bigDataParamName] = bigData.size.toString()
                    ClientCache.bigDataClientSharedMemory!!.writeByteArray(bigData)
                }

                val requestParamJson = GsonInstance.toJson(RequestParam(paramValueMap))


                var requestBase = RequestBase(
                    targetClazzName = targetClazzName,
                    functionKey = kotlinFunction.signature(),
                    callbackKey = if (callbackInstance) signature else "",
                    useBigIndex = useBigData,
                    bigIndexParamName = bigDataParamName
                )

                val requestBaseJson = requestBase.toJson()


                val parcelSize =
                    ParcelSizeHelper.getStringParcelSize(requestBaseJson) + ParcelSizeHelper.getStringParcelSize(
                        requestParamJson
                    )

                debugD("invoke requestParamJson parcelSize ->${parcelSize}, content->$requestParamJson")

                val response = if (parcelSize < BINDER_MAX_TRANSFORM_PARCEL_SIZE) {//binder传输

                    if (useBigData) {
                        synchronized(ClientCache.bigDataClientSharedMemory!!) {
                            parseFromBinder(requestBaseJson, requestParamJson)
                        }
                    } else {
                        parseFromBinder(requestBaseJson, requestParamJson)
                    }
                } else {//共享内存传输参数

                    val paramByteArray = requestParamJson.encodeToByteArray()

                    if (useBigData) {
                        synchronized(ClientCache.bigDataClientSharedMemory!!) {
                            parseFromSharedMemory(paramByteArray, requestBase)
                        }
                    } else {
                        parseFromSharedMemory(paramByteArray, requestBase)
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

    private fun parseFromSharedMemory(
        paramByteArray: ByteArray,
        requestBase: RequestBase
    ): Response {
        var requestBase1 = requestBase
        return synchronized(ClientCache.serverResponseSharedMemory!!) { //保证同步

            ClientCache.clientSharedMemory!!.writeByteArray(paramByteArray) //把utf-8编码的字节数组写入共享内存区域

            val responseStr = connector.connect(             //写完共享内存通知服务端读取
                GsonInstance.toJson(
                    requestBase1.createNoParameterRequest(
                        paramByteArray.size
                    ).apply { requestBase1 = this }), null
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

    private fun parseFromBinder(
        requestBaseJson: String,
        requestParamJson: String?
    ): Response {
        return synchronized(ClientCache.serverResponseSharedMemory!!) {//确保同步

            var responseObj = GsonInstance.fromJson<Response>(
                connector.connect(
                    requestBaseJson,
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
        }
    }
}