package com.aientec.ktv_diningout.dialog

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_diningout.TemporaryHandler
import com.aientec.ktv_diningout.databinding.DialogMealsListCheckBinding
import com.aientec.ktv_diningout.databinding.ItemSelectedMealsBinding
import com.aientec.ktv_diningout.viewmodel.MealsViewModel
import com.aientec.structure.Meals
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MealsListCheckDialog : DialogFragment() {
    private lateinit var binding: DialogMealsListCheckBinding

    private val mealsViewModel: MealsViewModel by activityViewModels()

    private lateinit var preview: Preview

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private lateinit var cameraProvider: ProcessCameraProvider

    private lateinit var imageCapture: ImageCapture

    private var cameraSelector: CameraSelector? = TemporaryHandler.cameraSelector

    private val itemAdapter: ItemAdapter = ItemAdapter()

    private var isCaptured: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogMealsListCheckBinding.inflate(inflater, container, false)

        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE);

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setNormal()

        preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.preview.surfaceProvider)
        }

        initCamera()

        binding.list.apply {
            this.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.adapter = itemAdapter
        }

        binding.ctrl.setOnClickListener {
            if (isCaptured)
                mealsViewModel.onReCapture()
            else
                capture()
        }

        binding.upload.setOnClickListener {
            mealsViewModel.onMealsConfirm()
        }

        mealsViewModel.selectedMealsGroup.observe(viewLifecycleOwner, {
            if (it != null)
                itemAdapter.list = it.mealsList
        })

        mealsViewModel.imageFile.observe(viewLifecycleOwner, {
            if (it == null) {
                isCaptured = false
                binding.upload.visibility = View.INVISIBLE
                binding.ctrl.text = "拍照"
                binding.preview.visibility = View.VISIBLE
                binding.image.visibility = View.INVISIBLE
                startPreview()
            } else {
                isCaptured = true
                binding.upload.visibility = View.VISIBLE
                binding.ctrl.text = "重拍"
                binding.preview.visibility = View.INVISIBLE
                binding.image.visibility = View.VISIBLE
                binding.image.setImageURI(Uri.fromFile(it))
                stopPreview()
            }
        })
    }


    private fun setNormal() {
        binding.root.setPadding(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                4f,
                resources.displayMetrics
            ).toInt()
        )

        val dm = Resources.getSystem().displayMetrics
        val width: Int = (dm.widthPixels.toFloat() * 3.0f / 4.0f).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun initCamera() {
        if (cameraSelector == null) return

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        val c = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner

            imageCapture = ImageCapture.Builder().build().apply {
                try {
                    targetRotation = binding.preview.display.rotation
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }

            cameraProvider = cameraProviderFuture.get()

            setupUseCase()

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setupUseCase() {
        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            val autoFocusPoint = SurfaceOrientedMeteringPointFactory(1f, 1f)
                .createPoint(.5f, .5f)

            val autoFocusAction = FocusMeteringAction.Builder(
                autoFocusPoint,
                FocusMeteringAction.FLAG_AF
            ).apply {
                //start auto-focusing after 2 seconds
                setAutoCancelDuration(2, TimeUnit.SECONDS)
            }.build()

            // Bind use cases to camera
            val camera = cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector!!,
                preview,
                imageCapture
            )

            camera.cameraControl.startFocusAndMetering(autoFocusAction)
        } catch (exc: Exception) {
            Log.e("TAG", "Use case binding failed", exc)
        }
    }

    private fun startPreview() {
        if (!this::cameraProvider.isInitialized) return
        if (!cameraProvider.isBound(preview))
            cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector!!, preview)
    }

    private fun stopPreview() {
        if (!this::cameraProvider.isInitialized) return
        if (cameraProvider.isBound(preview))
            cameraProvider.unbind(preview)
    }

    private fun capture() {
        imageCapture.takePicture(
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val planeProxy: ImageProxy.PlaneProxy = image.planes[0]
                    val buffer = planeProxy.buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer[bytes]

                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    val matrix: Matrix = Matrix()

                    matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())

                    val rotateBitmap =
                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                    val file: File =
                        File(requireContext().cacheDir, "${System.currentTimeMillis()}.jpg")

                    val fileStream: FileOutputStream = FileOutputStream(file)

                    rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileStream)

                    bitmap.recycle()

                    rotateBitmap.recycle()

                    mealsViewModel.onCaptured(file)
                }

                override fun onError(exception: ImageCaptureException) {
                    MainScope().launch {
                        Toast.makeText(requireContext(), exception.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            })
    }

    private inner class ItemAdapter : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
        var list: List<Meals>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            return ItemViewHolder(ItemSelectedMealsBinding.inflate(layoutInflater, parent, false))
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.meals = list!![position]
        }

        override fun getItemCount(): Int {
            return list?.size ?: 0
        }

        private inner class ItemViewHolder(private val mBinding: ItemSelectedMealsBinding) :
            RecyclerView.ViewHolder(mBinding.root) {
            var meals: Meals? = null
                @SuppressLint("SetTextI18n")
                set(value) {
                    field = value
                    if (field == null) return
                    mBinding.root.text = "${field!!.name} : ${field!!.count}"
                }
        }
    }
}