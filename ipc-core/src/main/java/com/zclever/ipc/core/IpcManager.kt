package com.zclever.ipc.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.util.Log
import com.zclever.ipc.IConnector
import com.zclever.ipc.core.client.Client
import com.zclever.ipc.core.client.ClientCache
import com.zclever.ipc.core.client.ServiceInvocationHandler
import com.zclever.ipc.core.client.IMediaManager
import com.zclever.ipc.core.client.VideoClient
import com.zclever.ipc.core.server.VideoService
import com.zclever.ipc.core.server.ServiceCache
import com.zclever.ipc.core.server.ServiceCenter
import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.staticFunctions


/**
 * 核心操作类，客户端都是通过这个类来实现跨进程通信
 */
object IpcManager {

    internal val useSharedMemory: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

    lateinit var connector: IConnector

    init {
        injectService()
    }

    private fun injectService() {
        //        //反射拿到kapt生成的类，把this传进去，完成注册服务
        //        Class.forName("com.lonbon.ipclib.ServiceInject").kotlin.let { kClass ->
        //
        //            kClass.declaredFunctions.first { it.name == "inject" }.let { kFunction ->
        //                kFunction.call(kClass.objectInstance, this)
        //            }
        //
        //        }
    }


    /**
     * 注册对应的接口服务类
     */
    fun register(kClazz: KClass<*>) {

        kClazz.findAnnotation<BindImpl>()?.let { bindImpl ->

            ServiceCache.kFunctionMap[bindImpl.value] =
                kClazz.declaredFunctions.map { declaredFunction ->

                    declaredFunction.signature() to declaredFunction //针对于同一个类kFunction的signature是唯一的，可以作为key

                }.toMutableList().apply {

                    //罗列java的getInstance方法
                    Class.forName(bindImpl.value).kotlin.staticFunctions.filter {
                        it.signature() == "getInstance()"
                    }.let { instanceFunctions ->
                        if (instanceFunctions.isNotEmpty()) {
                            add(instanceFunctions[0].signature() to instanceFunctions[0])
                        }
                    }


                }.toMap()


            Log.i(TAG, "register: ${ServiceCache.kFunctionMap}")
        }

    }


    fun register(clazz: Class<*>) {
        register(clazz.kotlin)
    }


    internal lateinit var packageName: String

    internal lateinit var appContext: Context

    fun init(context: Context) {
        this.appContext = context.applicationContext
    }


    fun open(packageName: String = "com.lonbon.lonbon_app") {
        this.packageName = packageName
        val componentName = ComponentName(packageName, ServiceCenter::class.java.name)
        val intent = Intent()
        intent.component = componentName
        appContext.bindService(intent, Connection, Context.BIND_AUTO_CREATE)

        VideoClient.open()
    }


    internal object Connection : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            connector = IConnector.Stub.asInterface(service)
            connector.registerClient(Client, Process.myPid())

            Log.i(TAG, "onServiceConnected: $connector")
        }

        override fun onServiceDisconnected(name: ComponentName?) {

            Log.i(TAG, "onServiceDisconnected: ")

            ClientCache.dataCallBack.clear()

            open()
        }
    }

    fun <T : Any> getDefault(javaClazz: Class<T>): T = getDefault(javaClazz.kotlin)


    fun <T : Any> getDefault(interfaceClazz: KClass<T>): T {

        return interfaceClazz.findAnnotation<BindImpl>()?.let { bindImpl ->

            val request = Request(type = REQUEST_TYPE_CREATE, targetClazzName = bindImpl.value)

            //这里需要通知主进程去创建实例
            connector.connect(GsonInstance.gson.toJson(request))

            Proxy.newProxyInstance(
                interfaceClazz.java.classLoader,
                arrayOf(interfaceClazz.java),
                ServiceInvocationHandler(connector, bindImpl.value)
            ).safeAs<T>()!!

        }
            ?: throw IllegalAccessException("the annotation BindImpl is not be found in ${interfaceClazz.qualifiedName}!!")

    }


    /**
     * 客户端获取媒体服务
     */
    fun getMediaService(): IMediaManager = VideoClient

    /**
     * 服务端必须要初始化后才能提供视频服务
     */
    fun initVideoService(videoService: VideoService) {
        ServiceCache.videoService = videoService
    }

}