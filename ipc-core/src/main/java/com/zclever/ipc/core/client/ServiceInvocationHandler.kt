package com.zclever.ipc.core.client

import android.util.Log
import com.zclever.ipc.IConnector
import com.zclever.ipc.core.*
import com.zclever.ipc.core.GsonInstance
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
    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {

        return method?.kotlinFunction?.let { kotlinFunction ->

            val callBackInvoke = if (args?.last() is Result<*>) {
                ClientCache.dataCallBack[Request.invokeId.incrementAndGet()] =
                    args.last() as DataCallBack
                Log.i(TAG, "invoke: ${args.last()}")
                true
            } else {
                false
            }

            GsonInstance.gson.fromJson(connector.connect(
                GsonInstance.gson.toJson(
                    Request(
                targetClazzName = targetClazzName,
                functionKey = kotlinFunction.signature(),
                valueParametersMap = kotlinFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }
                    .mapIndexed { index, kParameter ->
                        kParameter.name!! to if (callBackInvoke && index == args!!.size - 1) "" else GsonInstance.gson.toJson(
                            args!![index]
                        )
                    }.toMap(),
                invokeID = if (callBackInvoke) Request.invokeId.get() else -1,
                //                        dataType = if (callBackInvoke) args!!.last().javaClass.kotlin.supertypes.first { it.classifier == Result::class }
                //                            .arguments.first().type?.classifier.safeAs<KClass<*>>()!!.qualifiedName
                //                            ?: "" else ""
            )
            )
            ), kotlinFunction.returnType.classifier!!.safeAs<KClass<*>>()!!.java
            )

            //  Log.i(TAG, "invoke: ->$result")

        }
    }
}