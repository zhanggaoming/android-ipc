package com.zclever.ipc.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.zclever.ipc.annotation.BindImpl
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

@SupportedAnnotationTypes("com.zclever.ipc.annotation.BindImpl")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class RegisterProcessor : AbstractProcessor() {

    lateinit var filer: Filer

    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)

        filer = processingEnvironment.filer
    }


    override fun process(
        elements: MutableSet<out TypeElement>,
        roundEnvironment: RoundEnvironment
    ): Boolean {

        println("RegisterProcessor---------------------")

        val elements = roundEnvironment.getElementsAnnotatedWith(BindImpl::class.java)

        if (elements.size > 0) {

            elements.filter { it.kind == ElementKind.INTERFACE }.map {
                (it as TypeElement).qualifiedName
            }.let { qualifiedNames ->

                val registerClassName = ClassName("com.zclever.ipc.core", "IpcRegisterHelper")

                FileSpec.builder("com.zclever.ipc.core", "IpcRegisterHelper")
                    .addType(
                        TypeSpec.objectBuilder(registerClassName)
                            .addFunction(
                                FunSpec.builder("register")
                                    .also { funSpecBuilder ->
                                        qualifiedNames.forEach {
                                            println(it)
                                            funSpecBuilder.addStatement("IpcManager.register(Class.forName(\"$it\"))")
                                        }
                                    }
                                    .build()
                            )
                            .build()
                    ).build().writeTo(filer)

            }

        }


        return true

    }
}