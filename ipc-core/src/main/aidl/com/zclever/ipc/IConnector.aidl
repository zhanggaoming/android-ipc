// IConnector.aidl
package com.zclever.ipc;

import com.zclever.ipc.IClient;
import android.os.ParcelFileDescriptor;

// Declare any non-default types here with import statements

interface IConnector {

    String connect(String request);

    void registerClient(IClient client,int clientPid);

    void unregisterClient(IClient client);

}