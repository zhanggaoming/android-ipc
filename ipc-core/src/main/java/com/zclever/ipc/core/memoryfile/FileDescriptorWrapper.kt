package com.zclever.ipc.core.memoryfile

import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable

class FileDescriptorWrapper(
    responseFileDescriptor: ParcelFileDescriptor, callbackFileDescriptor: ParcelFileDescriptor
) : Parcelable {

    var responseFileDescriptor: ParcelFileDescriptor
        private set

    var callbackFileDescriptor: ParcelFileDescriptor
        private set

    init {
        this.responseFileDescriptor = responseFileDescriptor
        this.callbackFileDescriptor = callbackFileDescriptor
    }


    constructor(parcel: Parcel) : this(
        parcel.readParcelable(ParcelFileDescriptor::class.java.classLoader)!!,
        parcel.readParcelable(ParcelFileDescriptor::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(responseFileDescriptor, flags)
        parcel.writeParcelable(callbackFileDescriptor, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FileDescriptorWrapper> {
        override fun createFromParcel(parcel: Parcel): FileDescriptorWrapper {
            return FileDescriptorWrapper(parcel)
        }

        override fun newArray(size: Int): Array<FileDescriptorWrapper?> {
            return arrayOfNulls(size)
        }
    }

}