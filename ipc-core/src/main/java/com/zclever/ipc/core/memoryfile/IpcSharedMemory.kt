package com.zclever.ipc.core.memoryfile

import android.os.*
import com.zclever.ipc.core.debugI
import com.zclever.ipc.core.parcelFileDescriptor
import com.zclever.ipc.core.safeAs
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

class IpcSharedMemory(
    private var memoryFile: MemoryFile? = null,
    private var sharedMemory: Any? = null,
) : Parcelable {

    private var mMapping: ByteBuffer? =
        sharedMemory?.let { it.safeAs<SharedMemory>()!!.mapReadWrite() }

    constructor(parcel: Parcel) : this(
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            val len = parcel.readInt()
            parcel.readParcelable<ParcelFileDescriptor>(ParcelFileDescriptor::class.java.classLoader)
                ?.let {
                    MemoryFileUtil.openMemoryFile(
                        it,
                        len,
                        MemoryFileOpenMode.MODE_READ_WRITE
                    )
                }
        } else {
            null
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            parcel.readParcelable<SharedMemory>(SharedMemory::class.java.classLoader)
        } else {
            null
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {

        if (useSharedMemory) {
            parcel.writeParcelable(sharedMemory.safeAs<SharedMemory>(), flags)
        } else {
            memoryFile?.let {
                debugI("writeToParcel: length->${it.length()} fd->${it.parcelFileDescriptor}")
                parcel.writeInt(it.length())
                parcel.writeParcelable(it.parcelFileDescriptor, flags)
            }
        }
    }


    fun readFromParcel(parcel: Parcel) {
        if (useSharedMemory) {
            this.sharedMemory =
                parcel.readParcelable<SharedMemory>(SharedMemory::class.java.classLoader)
            mMapping = sharedMemory?.let { it.safeAs<SharedMemory>()!!.mapReadWrite() }
        } else {

            val length = parcel.readInt()

            val readParcelable =
                parcel.readParcelable<ParcelFileDescriptor>(ParcelFileDescriptor::class.java.classLoader)

            this.memoryFile = readParcelable?.let {
                MemoryFileUtil.openMemoryFile(
                    it,
                    length,
                    MemoryFileOpenMode.MODE_READ_WRITE
                )
            }
        }
    }


    override fun describeContents(): Int {
        return 0
    }

    //
    companion object CREATOR : Parcelable.Creator<IpcSharedMemory> {
        override fun createFromParcel(parcel: Parcel): IpcSharedMemory {
            return IpcSharedMemory(parcel)
        }

        override fun newArray(size: Int): Array<IpcSharedMemory?> {
            return arrayOfNulls(size)
        }

        private val useSharedMemory = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

        fun create(capacity: Int): IpcSharedMemory =
            if (useSharedMemory) {
                IpcSharedMemory(null, SharedMemory.create(null, capacity))
            } else {
                IpcSharedMemory(memoryFile = MemoryFile(null, capacity))
            }
    }


    fun inputStream(): InputStream =
        if (useSharedMemory) MemoryInputStream() else memoryFile!!.inputStream


    fun outputStream(): OutputStream =
        if (useSharedMemory) MemoryOutputStream() else memoryFile!!.outputStream


    @Throws(IOException::class)
    private fun readBytes(buffer: ByteArray, srcOffset: Int, destOffset: Int, count: Int): Int {

        mMapping?.let {
            it.position(srcOffset)
            it.get(buffer, destOffset, count)
        }

        return count
    }

    @Throws(IOException::class)
    private fun writeBytes(buffer: ByteArray, srcOffset: Int, destOffset: Int, count: Int) {
        mMapping?.let {
            it.position(destOffset)
            it.put(buffer, srcOffset, count)
        }
    }


    private inner class MemoryInputStream : InputStream() {
        private var mMark = 0
        private var mOffset = 0
        private var mSingleByte: ByteArray? = null

        @Throws(IOException::class)
        override fun available(): Int {
            return if (mOffset >= sharedMemory.safeAs<SharedMemory>()!!.size) {
                0
            } else sharedMemory.safeAs<SharedMemory>()!!.size - mOffset
        }

        override fun markSupported(): Boolean {
            return true
        }

        override fun mark(readlimit: Int) {
            mMark = mOffset
        }

        @Throws(IOException::class)
        override fun reset() {
            mOffset = mMark
        }

        @Throws(IOException::class)
        override fun read(): Int {
            if (mSingleByte == null) {
                mSingleByte = ByteArray(1)
            }
            val result = read(mSingleByte!!, 0, 1)
            return if (result != 1) {
                -1
            } else mSingleByte!![0].toInt()
        }

        @Throws(IOException::class)
        override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
            var countTemp = count
            if (offset < 0 || countTemp < 0 || offset + countTemp > buffer.size) {
                // readBytes() also does this check, but we need to do it before
                // changing count.
                throw IndexOutOfBoundsException()
            }
            countTemp = Math.min(countTemp, available())
            if (countTemp < 1) {
                return -1
            }
            val result: Int = readBytes(buffer, mOffset, offset, countTemp)
            if (result > 0) {
                mOffset += result
            }
            return result
        }

        @Throws(IOException::class)
        override fun skip(n: Long): Long {
            var nTemp = n
            if (mOffset + nTemp > sharedMemory.safeAs<SharedMemory>()!!.size) {
                nTemp = (sharedMemory.safeAs<SharedMemory>()!!.size - mOffset).toLong()
            }
            mOffset += nTemp.toInt()
            return nTemp
        }
    }

    private inner class MemoryOutputStream : OutputStream() {
        private var mOffset = 0
        private var mSingleByte: ByteArray? = null

        @Throws(IOException::class)
        override fun write(buffer: ByteArray, offset: Int, count: Int) {
            writeBytes(buffer, offset, mOffset, count)
            mOffset += count
        }

        @Throws(IOException::class)
        override fun write(oneByte: Int) {
            if (mSingleByte == null) {
                mSingleByte = ByteArray(1)
            }
            mSingleByte!![0] = oneByte.toByte()
            write(mSingleByte!!, 0, 1)
        }
    }

    fun close() {
        memoryFile?.close()
        sharedMemory?.let { it.safeAs<SharedMemory>()!!.close() }
    }

}




