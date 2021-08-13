// IMediaReceiver.aidl
package com.zclever.ipc.media;

// Declare any non-default types here with import statements
import android.os.ParcelFileDescriptor;

interface IMediaReceiver {

    void onData(int width,int height,int size,int format);

}