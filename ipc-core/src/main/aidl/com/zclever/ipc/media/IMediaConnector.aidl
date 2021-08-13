// IMediaConnector.aidl
package com.zclever.ipc.media;

// Declare any non-default types here with import statements
import com.zclever.ipc.media.IMediaReceiver;
import android.os.ParcelFileDescriptor;
import android.os.SharedMemory;
interface IMediaConnector {

    void takePicture(int format);

    void takeFrame(int type);

    void stopTakeFrame();

    void setPreviewCallBack(IMediaReceiver previewCallBack);

    void setPictureCallBack(IMediaReceiver pictureCallBack);

    ParcelFileDescriptor obtainPictureFd();

    ParcelFileDescriptor obtainFrameFd();

    SharedMemory obtainPictureSharedMemory();

    SharedMemory obtainFrameSharedMemory();
}