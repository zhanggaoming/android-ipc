package com.zclever.ipc.core

import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import android.util.Log
import com.zclever.ipc.annotation.BigData
import com.zclever.ipc.core.memoryfile.MemoryFileUtil
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation


const val TAG = "android-ipc"


/**
 * 简化强转的写法
 */
fun <T> Any?.safeAs(): T? {
    return this as? T
}

/**
 * 针对于每个KFunction生成唯一的签名，有点类似于native的方法签名，但不完全一样
 */
fun <T : KFunction<*>> T.signature(): String {

    return parameters.joinToString(
        separator = ";", prefix = "${name}(", postfix = ")"
    ) {

        it.type.toString()
    }

}

fun <T : KFunction<*>> T.bigDataIndex(): Int {

    parameters.forEach {
        if (it.findAnnotation<BigData>() != null) {
            return it.index
        }
    }

    return -1
}

fun debugI(msg: String) {
    if (IpcManager.debug()) {
        Log.i(TAG, msg)
    }
}

fun debugD(msg: String) {
    if (IpcManager.debug()) {
        Log.d(TAG, msg)
    }
}

fun debugW(msg: String) {
    if (IpcManager.debug()) {
        Log.w(TAG, msg)
    }
}

fun debugE(msg: String) {
    if (IpcManager.debug()) {
        Log.e(TAG, msg)
    }
}

val MemoryFile.parcelFileDescriptor: ParcelFileDescriptor
    get() = MemoryFileUtil.parcelFileDescriptorConstructor.newInstance(
        MemoryFileUtil.memoryFileKFunctionMap["getFileDescriptor"]!!.call(this)
    )

