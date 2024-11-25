package com.zclever.ipc.core.shared_memory

import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import android.os.SharedMemory
import android.util.Log
import com.zclever.ipc.core.debugD
import com.zclever.ipc.core.inputStream
import com.zclever.ipc.core.outputStream
import com.zclever.ipc.core.safeAs
import java.io.FileDescriptor
import kotlin.math.ceil
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible

/**
 * 共享内存工具类
 */
internal object MemoryFileUtil {

    private val memoryFileMemberMap by lazy {
        MemoryFile::class.java.declaredFields.filter {
            it.name in listOf(
                "mFD", "mLength", "mAddress"
            )
        }.map {
            it.isAccessible = true
            it.name to it
        }.toMap()
    }

    internal val memoryFileKFunctionMap by lazy {
        MemoryFile::class.functions.filter {
            it.name in listOf("getFileDescriptor", "native_mmap")
        }.map {
            it.isAccessible = true
            it.name to it
        }.toMap()
    }


}


internal fun MemoryFile.readJsonStr(size: Int) = inputStream.use { inputStream ->
    String(ByteArray(size).also {
        inputStream.read(it)
    })
}

internal fun MemoryFile.writeJsonStr(json: String) = outputStream.use { outputStream ->
    json.encodeToByteArray().let {
        outputStream.write(it)
    }
}


internal fun MemoryFile.writeByteArray(byteArray: ByteArray) = outputStream.use { outputStream ->
    outputStream.write(byteArray)
}

internal val MemoryFile.parcelFileDescriptor: ParcelFileDescriptor
    get() = ParcelFileDescriptor.dup(
        MemoryFileUtil.memoryFileKFunctionMap["getFileDescriptor"]!!.call(
            this
        ).safeAs<FileDescriptor>()!!
    )


internal fun ParcelFileDescriptor.readJsonStr(size: Int) = inputStream().use { inputStream ->
    String(ByteArray(size).also {
        inputStream.read(it)
    })
}


internal fun ParcelFileDescriptor.writeByteArray(data: ByteArray) = outputStream().use {
    it.write(data)
}

internal fun ParcelFileDescriptor.readByteData(size: Int) =inputStream().use {inputStream ->
    ByteArray(size).also {
        inputStream.read(it)
    }
}


//获取最接近的4K字节数
internal fun Int.getNext4KMultiple(): Int {
    return (ceil(toDouble() / 4096).toInt()) * 4096
}


internal val SharedMemory.parcelFileDescriptor: ParcelFileDescriptor
    get() = ParcelFileDescriptor.fromFd(SharedMemory::class.declaredFunctions
        .first { it.name == "getFd" }
        .apply { isAccessible = true }.call(this).safeAs()!!
    )



