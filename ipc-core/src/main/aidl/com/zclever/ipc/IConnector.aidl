// IConnector.aidl
package com.zclever.ipc;

import com.zclever.ipc.IClient;
import android.os.ParcelFileDescriptor;
import com.zclever.ipc.core.memoryfile.FileDescriptorWrapper;
// Declare any non-default types here with import statements

interface IConnector {

    String connect(String baseRequest,String requestParam);

    void registerClient(IClient client,int clientPid);

    FileDescriptorWrapper exchangeSharedMemory(int clientPid,in ParcelFileDescriptor fd);

    void unregisterClient(IClient client);

}