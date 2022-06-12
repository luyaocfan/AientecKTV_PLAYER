package com.aientec.ktv_pos_tablet.fragment.box

import android.graphics.Camera
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_pos_tablet.R
import com.aientec.ktv_pos_tablet.databinding.FragmentBoxMapBinding
import com.aientec.ktv_pos_tablet.databinding.ViewMapListItemBinding
import com.aientec.ktv_pos_tablet.fragment.dialog.CameraPlayDialog
import com.aientec.ktv_pos_tablet.structure.Floor
import com.aientec.ktv_pos_tablet.structure.IpCamera
import com.aientec.ktv_pos_tablet.structure.MapMarker
import com.aientec.ktv_pos_tablet.viewmodel.FloorViewModel
import com.aientec.ktv_pos_tablet.viewmodel.MarkerViewModel
import ovh.plrapps.mapview.MapView
import ovh.plrapps.mapview.MapViewConfiguration
import ovh.plrapps.mapview.ReferentialData
import ovh.plrapps.mapview.api.addMarker
import ovh.plrapps.mapview.paths.removePathView
import java.io.File

class MapFragment : Fragment() {
    private lateinit var binding: FragmentBoxMapBinding

    private val markerViewModel: MarkerViewModel by activityViewModels()

    private val floorViewModel: FloorViewModel by activityViewModels()

    private val itemAdapter: ItemAdapter = ItemAdapter()

    private var switchDisplayCondition: Float = 0.0f

    private var minSize: Int? = null

    private var floor: Floor? = null

    private val mapSIze: Int = 1280

    private var r: ReferentialData? = null

    private var map: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBoxMapBinding.inflate(inflater, container, false)

        binding.root.post {
            if (minSize == null)
                minSize = binding.root.width.coerceAtMost(binding.root.height)


            floorViewModel.selectFloor.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (it.id == floor?.id) return@observe
                    floor = it
                    prepareMap()
                    markerViewModel.updateMarkers(it.id)
                }
            }

            markerViewModel.markers.observe(viewLifecycleOwner) {
                updateMarkers(it ?: return@observe)
                itemAdapter.list = it
            }


        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.list.apply {
            this.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.adapter = itemAdapter
        }

        floorViewModel.hasNextFloor.observe(viewLifecycleOwner) {
            binding.nextFloor.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }

        floorViewModel.hasLastFloor.observe(viewLifecycleOwner) {
            binding.lastFloor.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }

        markerViewModel.selectedCamera.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            playCamera()
        }

        binding.lastFloor.setOnClickListener {
            floorViewModel.lastFloor()
        }

        binding.nextFloor.setOnClickListener {
            floorViewModel.nextFloor()
        }
    }

    private fun prepareMap() {

        if (map != null) {
            binding.mapContainer.removeView(map)
            map = null
        }

        map = MapView(requireContext())
        map!!.isSaveEnabled = true

        binding.mapContainer.addView(map)

//        val layoutParam: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
//            FrameLayout.LayoutParams.MATCH_PARENT,
//            FrameLayout.LayoutParams.MATCH_PARENT
//        )


        val minScale: Float = minSize!!.toFloat() / mapSIze.toFloat()

        val maxScale: Float = minScale * 4.0f

        switchDisplayCondition = minScale * 2.0f

        val config: MapViewConfiguration =
            MapViewConfiguration(
                1,
                mapSIze,
                mapSIze,
                mapSIze
            ) { _, _, _ ->
//                val file: File = File(floor!!.map)
//                if (file.exists())
//                    return@MapViewConfiguration FileInputStream(file)
//                else
                if (floor == null)
                    return@MapViewConfiguration null
                else {
                    if (floor!!.id == 1)
                        return@MapViewConfiguration context?.assets?.open("aientec_map.png")
                    else
                        return@MapViewConfiguration context?.assets?.open("aientec_none.png")
                }
            }
                .setMaxScale(maxScale)
                .setStartScale(minScale)
                .setMinScale(minScale)

        map!!.apply {
            configure(config)
            addReferentialListener {
                r = it
                Log.d("Trace", "Scale : ${it.scale}")
                if (it.scale <= switchDisplayCondition) {
                    binding.list.visibility = View.VISIBLE
                    markerLayout?.visibility = View.INVISIBLE
                } else {
                    binding.list.visibility = View.INVISIBLE
                    markerLayout?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateMarkers(list: ArrayList<MapMarker>) {
        if (map == null) return

        map!!.defineBounds(0.0, 0.0, 1.0, 1.0)

        map!!.markerLayout?.removeAllViews()

        for (i in list.indices) {
            val name = (list[i].item as IpCamera).name
            val view: TextView =
                layoutInflater.inflate(R.layout.view_map_ipcam_item, null) as TextView
            view.text = name
            view.setOnClickListener {
//                Toast.makeText(context, "Room $name clicked", Toast.LENGTH_LONG).show()
                markerViewModel.onCameraSelected(list[i].item as IpCamera)
            }
            map!!.addMarker(
                view,
                list[i].location.x.toDouble() / floor!!.size!!.width,
                list[i].location.y.toDouble() / floor!!.size!!.height
            )
        }
    }

    private fun playCamera() {
        val fm = childFragmentManager
        CameraPlayDialog().show(fm, "camera_play")
    }

    private inner class ItemViewHolder(private val mBinding: ViewMapListItemBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        var item: Any? = null
            set(value) {
                field = value
                if (field == null) return
                if (field is IpCamera) {
                    mBinding.name.text = (field as IpCamera).name
                }
            }

        init {
            itemView.setOnClickListener {
                if (item == null) return@setOnClickListener
                if (item is IpCamera)
                    markerViewModel.onCameraSelected(item as IpCamera)
            }
        }
    }

    private inner class ItemAdapter : RecyclerView.Adapter<ItemViewHolder>() {
        var list: ArrayList<MapMarker>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val mBinding: ViewMapListItemBinding =
                ViewMapListItemBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(mBinding)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.item = list?.get(position)?.item
        }

        override fun getItemCount(): Int {
            return list?.size ?: 0
        }
    }
}
