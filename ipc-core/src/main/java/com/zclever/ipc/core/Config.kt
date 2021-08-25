package com.zclever.ipc.core

class Config private constructor(builder: Builder) {

    val debug = builder.debug

    val openMedia = builder.openMedia

    companion object {
        fun builder() = Builder()
    }

    class Builder internal constructor() {

        internal var debug = false

        internal var openMedia = false

        fun configDebug(debug: Boolean) = apply {
            this.debug = debug
        }

        fun configOpenMedia(openMedia: Boolean) = apply {
            this.openMedia = openMedia
        }

        fun build() = Config(this)
    }
}