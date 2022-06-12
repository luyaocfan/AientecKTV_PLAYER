package com.example.uitester

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.unary.circularseekbar.CircularSeekBar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val seekbar: CircularSeekBar = findViewById(R.id.seeker)

        val progressValue: TextView = findViewById(R.id.progress_value)

        seekbar.onProgressChangeListener = object : CircularSeekBar.OnProgressChangeListener {
            override fun onProgressChanging(seekBar: CircularSeekBar, progress: Int): Boolean {
//                Log.d("Trace", "onProgressChanging : $progress")
                return true
            }

            override fun onProgressChanged(
                seekBar: CircularSeekBar,
                progress: Int,
                finished: Boolean
            ) {
                Log.d("Trace", "onProgressChanged : $progress, $finished")
                progressValue.text = progress.toString()
            }
        }
    }
}