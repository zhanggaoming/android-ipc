// IMediaConnector.aidl
package com.zclever.ipc.media;

// Declare any non-default types here with import statements
import com.zclever.ipc.media.IMediaCallback;
import android.os.ParcelFileDescriptor;
interface IMediaConnector {

    void takePicture(int format);

    void takeFrame(int type);

    void stopTakeFrame();

    ParcelFileDescriptor obtainPictureSharedMemory();

    ParcelFileDescriptor obtainFrameSharedMemory();

    void setMediaCallback(IMediaCallback callback);

}