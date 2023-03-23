package com.zclever.ipc.core.client

import com.zclever.ipc.IConnector
import com.zclever.ipc.core.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

/**
 * 动态代理InvocationHandler
 */
internal class ServiceInvocationHandler(
    private val connector: IConnector,
    private val targetClazzName: String
) : InvocationHandler {


    /**
     * 主要是把参数转成Request发出去
     */
    override fun invoke(proxy: Any, method: Method?, args: Array<out Any>?): Any? {

        return method?.kotlinFunction?.let { kotlinFunction ->

            try {
                val signature = kotlinFunction.signature()

                val sharedMemoryIndex = kotlinFunction.bigDataIndex()

                var sharedMemoryLength = 0

                if (sharedMemoryIndex > 0) {
                    ClientCache.sharedMemoryMap[SharedMemoryType.CLIENT]!!.outputStream()
                        .use { outputStream ->
                            outputStream.write(
                                args!![sharedMemoryIndex - 1].safeAs<ByteArray>()!!
                                    .also { sharedMemoryLength = it.size })
                        }
                }
                val callBackInvoke = if (args?.last() is Result<*>) {
                    ClientCache.dataCallBack[signature] = args.last() as DataCallBack
                    true
                } else {
                    false
                }

                val responseJson = connector.connect(
                    GsonInstance.toJson(
                        Request(
                            targetClazzName = targetClazzName,
                            functionKey = kotlinFunction.signature(),
                            valueParametersMap = kotlinFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }
                                .mapIndexed { index, kParameter ->
                                    kParameter.name!! to if (callBackInvoke && index == args!!.size - 1) "" else if (sharedMemoryIndex > 0 && sharedMemoryIndex - 1 == index) "" else GsonInstance.toJson(
                                        args!![index]
                                    )
                                }.toMap(),
                            invokeID = if (callBackInvoke) signature else "",
                            sharedMemoryParameterIndex = sharedMemoryIndex,
                            sharedMemoryLength = sharedMemoryLength
                            //                        dataType = if (callBackInvoke) args!!.last().javaClass.kotlin.supertypes.first { it.classifier == Result::class }
                            //                            .arguments.first().type?.classifier.safeAs<KClass<*>>()!!.qualifiedName
                            //                            ?: "" else ""
                        )
                    )
                )


                if (kotlinFunction.returnType.classifier == Unit::class) {
                    GsonInstance.fromJson(
                        responseJson,
                        kotlinFunction.returnType.classifier!!.safeAs<KClass<*>>()!!.java
                    )
                } else {
                    GsonInstance.fromJson<Any>(responseJson, kotlinFunction.returnType.javaType)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}