package com.aientec.ktv_pos_tablet.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.aientec.ktv_pos_tablet.R
import com.aientec.ktv_pos_tablet.databinding.ActivityMainBinding
import ovh.plrapps.mapview.MapViewConfiguration
import ovh.plrapps.mapview.api.addMarker
import ovh.plrapps.mapview.core.TileStreamProvider

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val roomNumbers: Array<String> = arrayOf("101", "102", "103")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        init()
    }

    private fun init() {
        val config: MapViewConfiguration =
            MapViewConfiguration(1, 2560, 2560, 2560, TileStreamProvider { row, col, zoomLvl ->
                assets.open("map.png")
            })
                .setMaxScale(1.5f)
                .setStartScale(0.8f)
                .setMinScale(0.8f)


        binding.map.configure(config)

        binding.map.defineBounds(0.0, 0.0, 1.0, 1.0)

        for (i in roomNumbers.indices) {
            val name = roomNumbers[i]
            val view: TextView = layoutInflater.inflate(R.layout.view_map_marker, null) as TextView
            view.text = name
            view.setOnClickListener {
                Toast.makeText(this, "Room $name clicked", Toast.LENGTH_LONG).show()
            }
            binding.map.addMarker(view, 0.15 * (i + 1), 0.1125)
        }
    }
}