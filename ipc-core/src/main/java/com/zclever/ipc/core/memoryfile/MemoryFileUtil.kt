package com.zclever.ipc.core.memoryfile

import android.os.Build
import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import android.util.Log
import com.zclever.ipc.core.TAG
import java.io.FileDescriptor
import java.lang.IllegalArgumentException
import java.lang.reflect.Constructor
import java.util.*
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible

/**
 * 共享内存工具类,8.1(包含8.1)以后使用SharedMemory，之前使用MemoryFile
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


    internal val parcelFileDescriptorConstructor: Constructor<ParcelFileDescriptor> by lazy {
        ParcelFileDescriptor::class.java.getDeclaredConstructor(FileDescriptor::class.java)
            .apply { isAccessible = true }
    }


    fun openMemoryFile(fd: ParcelFileDescriptor, capacity: Int, openMode: MemoryFileOpenMode) =
        openMemoryFile(fd.fileDescriptor, capacity, openMode)


    fun openMemoryFile(
        fd: FileDescriptor, capacity: Int, openMode: MemoryFileOpenMode
    ) = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) MemoryFile("tem", 1).apply {
        close()

        memoryFileMemberMap["mFD"]!!.set(this, fd)
        memoryFileMemberMap["mLength"]!!.set(this, capacity)
        memoryFileMemberMap["mAddress"]!!.set(
            this, memoryFileKFunctionMap["native_mmap"]!!.call(
                fd, capacity, openMode.mode
            )
        )
    } else throw IllegalStateException("SDK_INT >= VERSION_CODES.O_MR1 is illegal!")

}

enum class MemoryFileOpenMode(val mode: Int) {
    MODE_READ(0x01), //只读方式

    MODE_WRITE(0x2), //可写方式

    MODE_READ_WRITE(MODE_READ.mode or MODE_WRITE.mode) //可读写方式
}

fun Int.toByteArray(): ByteArray {
    val byteArray = ByteArray(4)
    val highH = ((this shr 24) and 0xff).toByte()
    val highL = ((this shr 16) and 0xff).toByte()
    val LowH = ((this shr 8) and 0xff).toByte()
    val LowL = (this and 0xff).toByte()
    byteArray[0] = highH
    byteArray[1] = highL
    byteArray[2] = LowH
    byteArray[3] = LowL
    return byteArray
}

fun ByteArray.toInt(): Int {

    if (size < 4) {
        throw IllegalArgumentException("the byte array size must >= 4!")
    }
    return this[0].toInt().shl(24).or(this[1].toInt().shl(16)).or(this[2].toInt().shl(8))
        .or(this[3].toInt())
}

const val VIDEO_DESCRIPTION_LEN = 14

fun IpcSharedMemory.canWrite() = ByteArray(1).let { resultByte ->

    inputStream().use { inputStream ->
        inputStream.read(resultByte)
    }.let {
        resultByte[0].toInt() != 0
    }
}

fun IpcSharedMemory.canRead() = !canWrite()


fun IpcSharedMemory.writeCanWrite(canWrite: Boolean) {
    outputStream().use { it.write(if (canWrite) 1 else 0) }
}

//1个字节canRead+1个字节format+4个字节width+4个字节height+4个字节size+数据部分
fun IpcSharedMemory.readVideoStruct(): IpcSharedMemory.VideoStruct {

    var format: Int
    var widht: Int
    var height: Int
    var size: Int

    return ByteArray(VIDEO_DESCRIPTION_LEN).also { param ->
        inputStream().use { inputStream ->
            inputStream.read(param)
        }
    }.let { paramBytes ->
        format = paramBytes[1].toInt()
        widht = paramBytes.copyOfRange(2, 6).toInt()
        height = paramBytes.copyOfRange(6, 10).toInt()
        size = paramBytes.copyOfRange(10, 14).toInt()

        IpcSharedMemory.VideoStruct(
            false,
            format,
            widht,
            height,
            size,
            if (size > 0) ByteArray(size).also { data ->
                inputStream().use {
                    it.skip(VIDEO_DESCRIPTION_LEN.toLong())
                    it.read(data)
                }
            } else null)
    }
}


fun IpcSharedMemory.writeVideoStruct(videoStruct: IpcSharedMemory.VideoStruct) {

    outputStream().use { outputStream ->

        ByteArray(VIDEO_DESCRIPTION_LEN).let { params ->
            params[0] = if (videoStruct.canWrite) 1 else 0
            params[1] = videoStruct.format.toByte()
            var offset = 2

            videoStruct.width.toByteArray().forEach { widthByte ->
                params[offset] = widthByte
                offset++
            }

            videoStruct.height.toByteArray().forEach { heightByte ->
                params[offset] = heightByte
                offset++
            }

            videoStruct.size.toByteArray().forEach { sizeByte ->
                params[offset] = sizeByte
                offset++
            }

            params.copyOf(VIDEO_DESCRIPTION_LEN + videoStruct.size).let { memoryData ->

                System.arraycopy(
                    videoStruct.data,
                    0,
                    memoryData,
                    VIDEO_DESCRIPTION_LEN,
                    videoStruct.size
                )

                outputStream.write(memoryData)
            }

        }

    }


}


