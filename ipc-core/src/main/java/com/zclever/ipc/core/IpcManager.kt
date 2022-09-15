package com.zclever.ipc.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.os.RemoteException
import android.util.Log
import com.zclever.ipc.IConnector
import com.zclever.ipc.annotation.BindImpl
import com.zclever.ipc.core.client.*
import com.zclever.ipc.core.memoryfile.IpcSharedMemory
import com.zclever.ipc.core.server.ServiceCache
import com.zclever.ipc.core.server.ServiceCenter
import com.zclever.ipc.core.server.VideoService
import java.io.IOException
import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.staticFunctions

typealias OpenComplete = () -> Unit

typealias OnServerDeath=()->Unit

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
        //反射拿到kapt生成的类，完成自动注册服务
        try {
            Class.forName("com.zclever.ipc.core.IpcRegisterHelper").kotlin.let {
                it.declaredFunctions
                    .first { function -> function.name == "register" }
                    .call(it.objectInstance)
            }
        } catch (e: Exception) {

            //debugE(e.message!!)
        }
    }


    /**
     * 注册对应的接口服务类
     */
    fun register(kClazz: KClass<*>) {

        try {
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


                Log.i(TAG,"register: ${ServiceCache.kFunctionMap}")
            }
        }catch (e:Exception){

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

    internal var openComplete: OpenComplete? = null

    fun open(packageName: String, openComplete: OpenComplete? = null) {
        this.packageName = packageName
        this.openComplete = openComplete
        val componentName = ComponentName(packageName, ServiceCenter::class.java.name)
        val intent = Intent()
        intent.component = componentName
        appContext.bindService(intent, Connection, Context.BIND_AUTO_CREATE)

        if (config.openMedia) {
            VideoClient.open()
        }
    }


    internal var config: Config = Config.builder().build()

    fun config(config: Config) = apply {
        this.config = config
    }

    fun debug() = config.debug



    var serverDeath:OnServerDeath?=null



    /**
     * 服务端进程死亡通知
     */
    internal object ServerDeathRecipient:IBinder.DeathRecipient{
        override fun binderDied() {
            //反馈给客户端
            serverDeath?.invoke()

            ClientCache.dataCallBack.clear()
            ClientCache.sharedMemoryMap[SharedMemoryType.SERVER]?.close()
            //重连
            open(packageName, openComplete)
        }
    }

    internal object Connection : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            connector = IConnector.Stub.asInterface(service)

            try {
                service.linkToDeath(ServerDeathRecipient,0)
            }catch (e:RemoteException){

            }
            connector.registerClient(Client, Process.myPid())

            /* ClientCache.sharedMemoryMap[SharedMemoryType.SERVER] = */
            connector.exchangeSharedMemory(
                Process.myPid(),
                ClientCache.sharedMemoryMap[SharedMemoryType.CLIENT] ?: IpcSharedMemory.create(
                    config.sharedMemoryCapacity
                ).also {
                    ClientCache.sharedMemoryMap[SharedMemoryType.CLIENT] = it
                }
            )


            openComplete?.invoke()

            debugI("onServiceConnected: $connector")
        }

        override fun onServiceDisconnected(name: ComponentName?) {

            debugI("onServiceDisconnected: ")

            ClientCache.dataCallBack.clear()
            ClientCache.sharedMemoryMap[SharedMemoryType.SERVER]?.close()

            //open(packageName, openComplete)
        }
    }

    fun <T : Any> getService(javaClazz: Class<T>): T = getService(javaClazz.kotlin)


    inline fun <reified T : Any> getService() = getService(T::class)

    fun <T : Any> getService(interfaceClazz: KClass<T>): T {

        return ClientCache.instanceMap[interfaceClazz]?.safeAs<T>()
            ?: Analyzer(interfaceClazz).analysis().let { analyzer ->
                val request =
                    Request(
                        type = REQUEST_TYPE_CREATE,
                        targetClazzName = analyzer.targetQualifiedName
                    )

                //这里需要通知主进程去创建实例
                connector.connect(GsonInstance.toJson(request))

                Proxy.newProxyInstance(
                    interfaceClazz.java.classLoader,
                    arrayOf(interfaceClazz.java),
                    ServiceInvocationHandler(connector, analyzer.targetQualifiedName)
                ).safeAs<T>()!!.also { instance ->
                    ClientCache.instanceMap[interfaceClazz] = instance
                }
            }

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

