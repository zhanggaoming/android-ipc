package com.zclever.ipc.core

import com.zclever.ipc.annotation.BindImpl
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation

class Analyzer(private val interfaceClazz: KClass<*>) {

    var targetQualifiedName = ""
        private set

    fun analysis() = apply {

        analysisInterface()

        analysisFunctions()
    }


    private fun analysisInterface() {

        if (!interfaceClazz.java.isInterface) {//必须是接口
            throw IllegalAccessException("the ${interfaceClazz.qualifiedName} is not a interface!!")
        }

        interfaceClazz.findAnnotation<BindImpl>()?.let {
            targetQualifiedName = it.value
        }
            ?: throw IllegalAccessException("the annotation BindImpl is not be found in ${interfaceClazz.qualifiedName}!!")

    }


    private fun analysisFunctions() {

        interfaceClazz.declaredFunctions.forEach { kFunction ->

            kFunction.parameters.forEachIndexed { index, kParameter ->

                if (kParameter.type.classifier == Result::class && index != kFunction.parameters.lastIndex) {//如果是Result作为参数则必须是最后一个参数

                    throw IllegalAccessException("the Result callback can only appear once and must be the last parameter in function. $kFunction")

                }
            }

        }
    }


}