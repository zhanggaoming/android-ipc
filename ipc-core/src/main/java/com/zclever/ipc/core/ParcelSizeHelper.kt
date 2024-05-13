package com.zclever.ipc.core

import android.os.Parcel

/*
 * *****************************************************************************
 * <p>
 * Copyright (C),2007-2016, LonBon Technologies Co. Ltd. All Rights Reserved.
 * <p>
 * *****************************************************************************
 */
object ParcelSizeHelper {


    fun  getStringParcelSize(data:String):Int{

        val parcel = Parcel.obtain()

        parcel.writeString(data)


        val size=parcel.marshall().size

        parcel.recycle()

        return size
    }



}