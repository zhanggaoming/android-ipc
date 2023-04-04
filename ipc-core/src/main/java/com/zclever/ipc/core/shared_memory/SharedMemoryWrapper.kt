package com.zclever.ipc.core.shared_memory

import android.annotation.SuppressLint
import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import android.os.SharedMemory
import com.zclever.ipc.core.IpcManager
import java.io.Closeable
import java.nio.ByteBuffer

/**
 * 共享内存抽象接口
 */
abstract class AbstractSharedMemory(val name: String, val size: Int) : Closeable {

    abstract val parcelFileDescriptor: ParcelFileDescriptor

    abstract fun writeByteArray(byteArray: ByteArray, offset: Int, length: Int)

}

fun AbstractSharedMemory.writeByteArray(byteArray: ByteArray) =
    writeByteArray(byteArray, 0, byteArray.size)


class MemoryFileImpl(name: String, size: Int) : AbstractSharedMemory(name, size) {

    private val memoryFile: MemoryFile


    init {
        memoryFile = MemoryFile(name, size)
    }

    override val parcelFileDescriptor: ParcelFileDescriptor
        get() = memoryFile.parcelFileDescriptor

    override fun writeByteArray(byteArray: ByteArray, offset: Int, length: Int) {
        memoryFile.outputStream.use {
            it.write(byteArray, offset, length)
        }
    }

    override fun close() {
        memoryFile.close()
    }

}


@SuppressLint("NewApi")
class SharedMemoryImpl(name: String, size: Int) : AbstractSharedMemory(name, size) {

    private val sharedMemory: SharedMemory

    private val mapByteBuffer: ByteBuffer

    init {
        sharedMemory = SharedMemory.create(name, size)
        mapByteBuffer = sharedMemory.mapReadWrite()
    }

    override val parcelFileDescriptor: ParcelFileDescriptor
        get() = sharedMemory.parcelFileDescriptor

    override fun writeByteArray(byteArray: ByteArray, offset: Int, length: Int) {
        mapByteBuffer.also { it.rewind() }.put(byteArray, offset, length)
    }

    override fun close() {
        sharedMemory.close()
    }

}


object SharedMemoryFactory {


    fun create(name: String, size: Int): AbstractSharedMemory {

        return if (IpcManager.useSharedMemory) SharedMemoryImpl(name, size)
        else MemoryFileImpl(name, size)

    }


}


