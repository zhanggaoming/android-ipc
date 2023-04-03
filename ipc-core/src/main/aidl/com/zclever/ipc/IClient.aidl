// IClient.aidl
package com.zclever.ipc;
import android.os.ParcelFileDescriptor;
// Declare any non-default types here with import statements

interface IClient {

   void onReceive(String response);

}