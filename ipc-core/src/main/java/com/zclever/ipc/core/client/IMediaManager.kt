package com.zclever.ipc.core.client

interface IMediaManager {

    fun takeFrame(frameType: FrameType)

    fun stopTakeFrame()

    fun takePicture(pictureFormat: PictureFormat)

    fun setPreviewCallBack(callBack: IPreviewCallBack)

    fun setPictureCallBack(callBack: IPictureCallBack)
}

enum class FrameType(val type: Int) {
    NV21(1), H264(2)
}

enum class PictureFormat(val format: Int) {
    JPEG(1), PNG(2)
}

interface IPreviewCallBack {
    fun onPreviewFrame(data: ByteArray?, width: Int, height: Int, frameType: FrameType)
}

interface IPictureCallBack {
    fun onPictureTaken(data: ByteArray?, width: Int, height: Int, pictureFormat: PictureFormat)
}