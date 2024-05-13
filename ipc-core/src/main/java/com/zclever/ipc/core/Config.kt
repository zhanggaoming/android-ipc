package com.zclever.ipc.core


class Config private constructor(builder: Builder) {

    val debug = builder.debug

    val openMedia = builder.openMedia

    val sharedMemoryCapacity = builder.sharedMemoryCapacity

    val mediaMemoryCapacity = builder.mediaMemoryCapacity

    companion object {
        const val DEFAULT_MEDIA_MEMORY_SIZE = 8 * 1024 * 1024 //默认8M数据
        const val SHARED_MEMORY_DEFAULT_SIZE = 8 * 1024 * 1024 //默认8M数据
        fun builder() = Builder()
    }

    class Builder internal constructor() {

        internal var debug = false

        internal var openMedia = false

        internal var sharedMemoryCapacity = SHARED_MEMORY_DEFAULT_SIZE

        internal var mediaMemoryCapacity = DEFAULT_MEDIA_MEMORY_SIZE

        fun configDebug(debug: Boolean) = apply {
            this.debug = debug
        }

        fun configOpenMedia(openMedia: Boolean) = apply {
            this.openMedia = openMedia
        }


        fun configSharedMemoryCapacity(capacity: Int) = apply {
            this.sharedMemoryCapacity = capacity
        }

        fun configMediaMemoryCapacity(capacity: Int) = apply {
            this.mediaMemoryCapacity = capacity
        }


        fun build() = Config(this)
    }
}