package idv.bruce.ui.osd

interface OsdContainer {

    fun addOsdItem(item: OSDItem)

    fun removeOsdItem(item: OSDItem)

    fun onStart()

    fun onStop()
}