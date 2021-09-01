package com.zclever.ipc.core

import com.zclever.ipc.annotation.BigData
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

            var bigDataCount = 0

            kFunction.parameters.forEach { kParameter ->

                kParameter.findAnnotation<BigData>()?.let {

                    if (kParameter.type.classifier.safeAs<KClass<*>>() != ByteArray::class) {//只能修饰字节数组
                        throw IllegalAccessException("the annotation BigData can only decorate byte arrays. $kFunction")
                    }

                    if (bigDataCount >= 1) {//只能出现一次
                        throw IllegalAccessException("the annotation BigData can only appear once in function. $kFunction")
                    }

                    bigDataCount++
                }
            }


        }

    }


}