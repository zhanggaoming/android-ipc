package com.zclever.ipc.core

import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import android.os.SharedMemory
import android.util.Log
import com.zclever.ipc.core.memoryfile.MemoryFileUtil
import kotlin.reflect.KFunction


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
    ) { it.type.toString() }

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


const val PICTURE_DATA_LENGTH = 1280 * 720 * 4

const val FRAME_DATA_LENGTH = 1280 * 720 * 4

val MemoryFile.parcelFileDescriptor: ParcelFileDescriptor
    get() = MemoryFileUtil.parcelFileDescriptorConstructor.newInstance(
        MemoryFileUtil.memoryFileKFunctionMap["getFileDescriptor"]!!.call(this)
    )

