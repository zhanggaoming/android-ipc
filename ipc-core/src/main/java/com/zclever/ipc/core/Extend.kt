package com.zclever.ipc.core

import android.os.ParcelFileDescriptor
import android.system.Os
import android.system.OsConstants
import android.util.Log
import com.zclever.ipc.annotation.BigData
import java.io.FileInputStream
import java.io.FileOutputStream
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
internal fun <T : KFunction<*>> T.signature(): String {

    return parameters.joinToString(
        separator = ";", prefix = "${name}(", postfix = ")"
    ) {

        it.type.toString()
    }

}

internal fun <T : KFunction<*>> T.bigDataIndex(): Int {

    parameters.forEach {
        if (it.findAnnotation<BigData>() != null) {
            return it.index
        }
    }

    return -1
}

internal fun debugI(msg: String) {
    if (IpcManager.debug()) {
        Log.i(TAG, msg)
    }
}

internal fun debugD(msg: String) {
    if (IpcManager.debug()) {
        Log.d(TAG, msg)
    }
}

internal fun debugW(msg: String) {
    if (IpcManager.debug()) {
        Log.w(TAG, msg)
    }
}

internal fun debugE(msg: String) {
    if (IpcManager.debug()) {
        Log.e(TAG, msg)
    }
}

internal fun Any?.toJson() = GsonInstance.toJson(this)

internal fun ParcelFileDescriptor.outputStream() = FileOutputStream(fileDescriptor.apply {
    Os.lseek(this, 0, OsConstants.SEEK_SET)
})

internal fun ParcelFileDescriptor.inputStream() = FileInputStream(fileDescriptor.apply {
    Os.lseek(this, 0, OsConstants.SEEK_SET)
})


