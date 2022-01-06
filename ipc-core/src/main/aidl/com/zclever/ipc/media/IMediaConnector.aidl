// IMediaConnector.aidl
package com.zclever.ipc.media;

// Declare any non-default types here with import statements
import com.zclever.ipc.media.IMediaReceiver;
//import android.os.ParcelFileDescriptor;
//import android.os.SharedMemory;
import com.zclever.ipc.core.memoryfile.IpcSharedMemory;
interface IMediaConnector {

    void takePicture(int format);

    void takeFrame(int type);

    void stopTakeFrame();

    IpcSharedMemory obtainPictureSharedMemory();

    IpcSharedMemory obtainFrameSharedMemory();

}