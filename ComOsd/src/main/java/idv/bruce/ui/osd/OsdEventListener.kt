package idv.bruce.ui.osd

interface OsdEventListener {
    fun onDone(item: OSDItem)
    fun onContainerReady()
    fun onContainerSizeChanged(width: Int, height: Int)
}