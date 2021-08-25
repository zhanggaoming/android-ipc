package com.zclever.ipc.core.memoryfile

import android.os.*

class IpcSharedMemory(
    val parcelFileDescriptor: ParcelFileDescriptor?,
    val sharedMemory: SharedMemory? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(ParcelFileDescriptor::class.java.classLoader),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            parcel.readParcelable(SharedMemory::class.java.classLoader)
        } else null
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(parcelFileDescriptor, flags)
        parcel.writeParcelable(sharedMemory, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IpcSharedMemory> {
        override fun createFromParcel(parcel: Parcel): IpcSharedMemory {
            return IpcSharedMemory(parcel)
        }

        override fun newArray(size: Int): Array<IpcSharedMemory?> {
            return arrayOfNulls(size)
        }
    }
}