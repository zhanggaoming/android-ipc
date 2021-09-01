package com.zclever.ipc.core


class Config private constructor(builder: Builder) {

    val debug = builder.debug

    val openMedia = builder.openMedia

    val sharedMemoryCapacity = builder.sharedMemoryCapacity

    val mediaMemoryCapacity = builder.mediaMemoryCapacity

    companion object {
        const val DEFAULT_MEMORY_SIZE = 1280 * 720 * 4
        fun builder() = Builder()
    }

    class Builder internal constructor() {

        internal var debug = false

        internal var openMedia = false

        internal var sharedMemoryCapacity = DEFAULT_MEMORY_SIZE

        internal var mediaMemoryCapacity = DEFAULT_MEMORY_SIZE

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