// IMediaCallback.aidl
package com.zclever.ipc.media;

// Declare any non-default types here with import statements
import android.os.ParcelFileDescriptor;

interface IMediaCallback {

    void onPicture(int width,int height,int size,int format);

    void onFrame(int width,int height,int size,int type);

}