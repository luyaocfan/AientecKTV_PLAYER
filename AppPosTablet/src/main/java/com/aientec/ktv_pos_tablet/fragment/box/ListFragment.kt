package com.aientec.ktv_pos_tablet.fragment.box

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_pos_tablet.R
import com.aientec.ktv_pos_tablet.databinding.FragmentBoxListBinding
import com.aientec.ktv_pos_tablet.databinding.ViewBoxListFooterBinding
import com.aientec.ktv_pos_tablet.databinding.ViewBoxListHeaderBinding
import com.aientec.ktv_pos_tablet.databinding.ViewBoxListItemBinding
import com.aientec.ktv_pos_tablet.structure.Box
import com.aientec.ktv_pos_tablet.viewmodel.*
import com.aientec.structure.Room
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ListFragment : Fragment() {
      companion object {
            const val TYPE_ITEM = 0
            const val TYPE_HEADER = 1
            const val TYPE_FOOTER = 2

            const val COLUMN_MIN = 5
            const val COLUMN_MAX = 6
      }

      private lateinit var binding: FragmentBoxListBinding

      private val floorViewModel: FloorViewModel by activityViewModels()

      private val typeViewModel: TypeViewModel by activityViewModels()

      private val boxViewModel: BoxViewModel by activityViewModels()

      private val orderViewModel: OrderViewModel by activityViewModels()

      private val infoAdapter: InfoAdapter = InfoAdapter()

      private lateinit var colorList: IntArray

      private var columnCount = COLUMN_MIN

      private var layoutManager: GridLayoutManager? = null


      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentBoxListBinding.inflate(inflater, container, false)
            return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            colorList = resources.getIntArray(R.array.box_state_color)

            binding.list.apply {
                  val mGr = GridLayoutManager(
                        context,
                        columnCount,
                        GridLayoutManager.VERTICAL,
                        false
                  ).apply {
                        this.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                              override fun getSpanSize(position: Int): Int {
                                    return if (infoAdapter.getItemViewType(position) == TYPE_ITEM)
                                          1
                                    else
                                          columnCount
                              }
                        }
                  }

                  layoutManager = mGr

                  this.layoutManager = layoutManager
                  this.adapter = infoAdapter
            }

            floorViewModel.selectFloor.observe(viewLifecycleOwner) {
                  updateList()
            }

            typeViewModel.selectedType.observe(viewLifecycleOwner) {
                  updateList()
            }

            boxViewModel.boxItems.observe(viewLifecycleOwner) {
                  infoAdapter.itemArray = it
            }

            boxViewModel.selectedBox.observe(viewLifecycleOwner) {
                  infoAdapter.selectBox = it
            }

            orderViewModel.isCreated.observe(viewLifecycleOwner, {
                  if (it == true)
                        updateList()
            })

            orderViewModel.isBoxClose.observe(viewLifecycleOwner, {
                  if (it == true)
                        updateList()
            })

            ViewModelImpl.isNavigationShown.observe(viewLifecycleOwner) { isShown ->
                  columnCount = if (isShown) COLUMN_MIN else COLUMN_MAX
                  layoutManager =
                        GridLayoutManager(
                              context,
                              columnCount,
                              GridLayoutManager.VERTICAL,
                              false
                        ).apply {
                              this.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                                    override fun getSpanSize(position: Int): Int {
                                          return if (infoAdapter.getItemViewType(position) == TYPE_ITEM)
                                                1
                                          else
                                                columnCount
                                    }
                              }
                        }
                  binding.list.layoutManager = layoutManager
                  infoAdapter.notifyDataSetChanged()
            }


      }

      private fun updateList() {
            val floor = floorViewModel.getSelectionFloorId()
            val type = typeViewModel.getSelectionTypeId()
            if (floor == null || type == null) return
            boxViewModel.filterBoxes(floor, type)
      }

      private inner class InfoAdapter : RecyclerView.Adapter<InfoAdapter.InfoItem>() {

            var selectBox: Box? = null
                  set(value) {
                        field = value
                        if (selectedIndex != -1)
                              notifyItemChanged(selectedIndex)

                        selectedIndex = if (value == null)
                              -1
                        else
                              itemArray?.indexOf(value as Any) ?: -1


                        notifyItemChanged(selectedIndex)
                  }

            var itemArray: ArrayList<Any>? = null
                  set(value) {
                        field = value
                        notifyDataSetChanged()
                  }

            private var selectedIndex: Int = -1


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoItem {
                  return when (viewType) {
                        TYPE_HEADER -> {
                              val mBinding: ViewBoxListHeaderBinding =
                                    ViewBoxListHeaderBinding.inflate(layoutInflater, parent, false)
                              HeaderItem(mBinding)
                        }
                        TYPE_ITEM -> {
                              val mBinding: ViewBoxListItemBinding =
                                    ViewBoxListItemBinding.inflate(layoutInflater, parent, false)
                              ContentItem(mBinding)
                        }
                        else -> {
                              val mBinding: ViewBoxListFooterBinding =
                                    ViewBoxListFooterBinding.inflate(layoutInflater, parent, false)
                              FooterItem(mBinding)
                        }
                  }
            }

            override fun getItemViewType(position: Int): Int {
                  return when (itemArray!![position]) {
                        is Room.Type -> TYPE_HEADER
                        is Box -> TYPE_ITEM
                        else -> TYPE_FOOTER
                  }
            }

            override fun onBindViewHolder(holder: InfoItem, position: Int) {
                  holder.item = itemArray!![position]
            }

            override fun getItemCount(): Int {
                  return itemArray?.size ?: 0
            }

            private abstract inner class InfoItem(view: View) : RecyclerView.ViewHolder(view) {
                  open var item: Any? = null
            }

            private inner class HeaderItem(private val mBinding: ViewBoxListHeaderBinding) :
                  InfoItem(mBinding.root) {
                  override var item: Any? = null
                        get() = super.item
                        set(value) {
                              field = value
                              mBinding.title.text = (field as Room.Type).name
                        }
            }

            private inner class ContentItem(private val mBinding: ViewBoxListItemBinding) :
                  InfoItem(mBinding.root) {

                  private val dateFormat: SimpleDateFormat =
                        SimpleDateFormat("HH:mm", Locale.TAIWAN)

                  override var item: Any? = null
                        get() = super.item
                        set(value) {
                              field = value
                              if (field == null) return

                              val box: Box = field as Box

                              mBinding.title.text = box.name

                              mBinding.card.apply {
                                    this.isChecked = box == selectBox
                                    if (this.isChecked)
                                          this.strokeColor =
                                                resources.getColor(R.color.secondaryColor, null)
                                    else
                                          this.strokeColor = Color.TRANSPARENT

                                    this.backgroundTintList =
                                          ColorStateList.valueOf(colorList[box.state - 1])
                              }


                              itemView.setOnClickListener {
                                    boxViewModel.onBoxSelected(box)
                              }
                        }
            }

            private inner class FooterItem(private val mBinding: ViewBoxListFooterBinding) :
                  InfoItem(mBinding.root)
      }

}