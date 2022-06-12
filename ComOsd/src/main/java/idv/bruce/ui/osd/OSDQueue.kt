package idv.bruce.ui.osd

import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.Size
import java.util.*
import kotlin.NoSuchElementException

internal class OSDQueue(var eventListener: OsdEventListener? = null) : Queue<OSDItem> {
    companion object {
        private const val TAG = "OSDQueue"

        private const val ON_REMOVE_ITEM = 0x00

        private const val ON_SIZE_CHANGED = 0x01
    }

    var viewSize: Size? = null
        set(value) {
            val isChanged: Boolean =
                (field?.width == value?.width && field?.height == value?.height)

            field = value

            val size: Size = field ?: return

            var node: Node? = first

            if (isChanged)
                eventListener?.onContainerSizeChanged(size.width, size.height)
            else
                eventListener?.onContainerReady()

            while (node != null) {
                node.element.viewSize = size
                node = node.nextNode
            }
        }

    private var first: Node? = null

    private var last: Node? = null

    private var count = 0

    private val handler: ProcessHandler = ProcessHandler()

    override val size: Int
        get() = count

    override fun add(element: OSDItem?): Boolean {
        val item: OSDItem = element ?: return false
        if (find(element) != null) return false
        item.viewSize = viewSize
        linkLast(item)
        return true
    }

    override fun addAll(elements: Collection<OSDItem>): Boolean {
        if (containsAll(elements)) return false

        for (e in elements)
            linkLast(e)
        return true
    }

    override fun clear() {
        var node: Node? = first ?: return

        while (node != null) {
            node.element.release()
            node = node.nextNode
        }
        first = null

        last = null
    }

    override fun iterator(): MutableIterator<OSDItem> {
        val iterator: Iterator<OSDItem> = object : Iterator<OSDItem> {
            override fun hasNext(): Boolean {
                TODO("Not yet implemented")
            }

            override fun next(): OSDItem {
                TODO("Not yet implemented")
            }
        }
        return iterator()
    }

    override fun remove(): OSDItem {
        val node: Node = first ?: throw NoSuchElementException()
        val item: OSDItem = node.element
        removeNode(node)
        return item
    }

    override fun contains(element: OSDItem?): Boolean {
        return find(element ?: return false) != null
    }

    override fun containsAll(elements: Collection<OSDItem>): Boolean {
        for (e in elements) {
            if (find(e) == null)
                return false
        }
        return true
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun remove(element: OSDItem?): Boolean {

        val item: OSDItem = element ?: return false

        val node: Node = find(item) ?: return false

        node.removed = true
        return true
    }

    override fun removeAll(elements: Collection<OSDItem>): Boolean {
        return false
    }

    override fun retainAll(elements: Collection<OSDItem>): Boolean {
        return false
    }

    override fun offer(e: OSDItem?): Boolean {
        val item: OSDItem = e ?: return false
        linkLast(item)
        return true
    }

    override fun poll(): OSDItem? {
        if (first == null)
            return null

        val element: OSDItem = first!!.element

        removeNode(first)

        return element
    }

    override fun element(): OSDItem {
        return first!!.element
    }

    override fun peek(): OSDItem? {
        return first?.element
    }

    fun onDraw(canvas: Canvas, frameTimeNanos: Long, timeIntervalNanos: Long) {
        var node: Node? = first

        var tmpNode: Node?

//        val time: Long = System.currentTimeMillis()

        var isRemove: Boolean

        while (node != null) {
            isRemove =
                node.element.onUpdate(canvas, frameTimeNanos, timeIntervalNanos) || node.removed

            if (isRemove) {
                tmpNode = node
                node = node.nextNode
                val item = removeNode(tmpNode)
                val message: Message = Message()
                message.what = ON_REMOVE_ITEM
                message.obj = item
                handler.sendMessage(message)
            } else {
                node = node.nextNode
            }
        }
    }


    private fun find(element: OSDItem): Node? {
        var node: Node? = first ?: return null

        while (node != null) {
            if (node.element == element)
                return node
            node = node.nextNode
        }
        return null
    }

    private fun removeNode(node: Node?): OSDItem? {
        val mNode: Node = node ?: return null

        Log.d(TAG, "Remove node ${node.element.uid}")

        val element: OSDItem = mNode.element

        if (node == first)
            first = mNode.nextNode
        if (node == last)
            last = mNode.prevNode

        mNode.prevNode?.nextNode = mNode.nextNode
        mNode.nextNode?.prevNode = mNode.prevNode
        mNode.prevNode = null
        mNode.nextNode = null
        count--

        return element
    }

    private fun linkLast(element: OSDItem) {
        val l: Node? = last
        val node: Node = Node(l, element, null)
        last = node
        if (l == null)
            first = node
        else
            l.nextNode = node
        count++
    }

    private class Node(var prevNode: Node?, var element: OSDItem, var nextNode: Node?) {
        var removed: Boolean = false
    }

    private inner class ProcessHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            when (msg.what) {
                ON_REMOVE_ITEM -> {
                    Log.d(TAG, "ON_REMOVE_ITEM : $count")
                    val item: OSDItem = (msg.obj ?: return) as OSDItem
                    eventListener?.onDone(item)
                }
            }
        }
    }
}