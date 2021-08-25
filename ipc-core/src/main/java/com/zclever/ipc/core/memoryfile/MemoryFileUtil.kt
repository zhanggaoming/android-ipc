package com.zclever.ipc.core.memoryfile

import android.os.Build
import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import android.os.SharedMemory
import android.util.Log
import com.zclever.ipc.core.TAG
import java.io.FileDescriptor
import java.lang.reflect.Constructor
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

    fun createMemoryFile(name: String, capacity: Int) = MemoryFile(name, capacity)

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


//    fun createMemoryFile(sharedMemory: SharedMemory, openMode: MemoryFileOpenMode) =
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) MemoryFile("tem", 1).apply {
//
//            close()
//
//            memoryFileMemberMap["mSharedMemory"]!!.set(this, sharedMemory)
//            memoryFileMemberMap["mMapping"]!!.set(
//                this, if (openMode == MemoryFileOpenMode.MODE_READ) {
//
//                    sharedMemory.mapReadOnly()
//
//                } else {
//                    sharedMemory.mapReadWrite()
//                }
//            )
//
//        } else throw IllegalAccessException("SDK_INT < VERSION_CODES.O_MR1 is illegal!")


}

enum class MemoryFileOpenMode(val mode: Int) {
    MODE_READ(0x01), //只读方式

    MODE_WRITE(0x2), //可写方式

    MODE_READ_WRITE(MODE_READ.mode or MODE_WRITE.mode) //可读写方式
}