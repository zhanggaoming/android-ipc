package com.zclever.ipc.core.memoryfile

import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import com.zclever.ipc.core.inputStream
import com.zclever.ipc.core.outputStream
import com.zclever.ipc.core.safeAs
import java.io.FileDescriptor
import kotlin.math.ceil
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible

/**
 * 共享内存工具类
 */
object MemoryFileUtil {

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


//获取最接近的4K字节数
internal fun Int.getNext4KMultiple(): Int {
    return (ceil(toDouble() / 4096).toInt()) * 4096
}


