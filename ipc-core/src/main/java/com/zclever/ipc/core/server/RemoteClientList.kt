package com.zclever.ipc.core.server

import android.os.IBinder
import android.os.IInterface
import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.util.ArrayMap

/**
 * 维护客户端的远程IBinder回调实例
 */
internal class RemoteClientList<T : IInterface>(
    val callbackSharedMemoryMap: MutableMap<Int, MemoryFile>,
    val responseSharedMemoryMap: MutableMap<Int, MemoryFile>,
    val clientSharedMemoryMap: MutableMap<Int, ParcelFileDescriptor>
) {

    private val mCallBacks = ArrayMap<IBinder, CallBack>()

    private val pidBinderMap = ArrayMap<Int, IBinder>()


    inner class CallBack(internal val client: T, internal val pid: Int) : IBinder.DeathRecipient {

        override fun binderDied() {
            synchronized(mCallBacks) {
                mCallBacks.remove(pidBinderMap[pid])
                pidBinderMap.remove(pid)
                callbackSharedMemoryMap.remove(pid)?.let {
                    it.close()
                }

                responseSharedMemoryMap.remove(pid)?.let {
                    it.close()
                }

                clientSharedMemoryMap.remove(pid)?.let {
                    it.close()
                }

            }
        }

    }

    fun register(client: T, pid: Int): Boolean {

        return synchronized(mCallBacks) {
            try {
                val binder = client.asBinder()
                val cb = CallBack(client, pid)
                binder.linkToDeath(cb, 0)
                mCallBacks[binder] = cb
                pidBinderMap[pid] = binder
                true
            } catch (e: RemoteException) {
                false
            }
        }

    }

    fun unregister(callBack: T): Boolean {

        return synchronized(mCallBacks) {

            val cb = mCallBacks.remove(callBack.asBinder())

            pidBinderMap.remove(cb?.pid)

            callbackSharedMemoryMap.remove(cb?.pid)?.let {
                it.close()
            }

            responseSharedMemoryMap.remove(cb?.pid)?.let {
                it.close()
            }

            clientSharedMemoryMap.remove(cb?.pid)?.let {
                it.close()
            }

            cb?.client?.asBinder()?.unlinkToDeath(cb, 0) == null
        }
    }

    fun getClientByPid(pid: Int): T? {
        return synchronized(mCallBacks) {
            mCallBacks[pidBinderMap[pid]]?.client
        }
    }

}