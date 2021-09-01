package com.zclever.ipc.core.server

/**
 * 提供媒体服务的类继承这个类，并且通过IpcManager.initMediaService设置进来
 */
abstract class VideoService {

    //客户端通知拍照
    abstract fun takePicture(pictureFormat: Int)

    //客户端通知取帧
    abstract fun takeFrame(type: Int)

    //客户端通知停止取帧
    abstract fun stopTakeFrame()


    //照片数据通过这个函数返回
    fun onTakePicture(
        byteArray: ByteArray, width: Int, height: Int, size: Int, pictureFormat: Int
    ) {

        VideoCenter.instanceWeakReference.get()
            ?.onTakePicture(byteArray, width, height, size, pictureFormat)
    }

    //取帧数据通过这个函数返回
    fun onTakeFrame(byteArray: ByteArray, width: Int, height: Int, size: Int, type: Int) {
        VideoCenter.instanceWeakReference.get()?.onTakeFrame(byteArray, width, height, size, type)
    }

}