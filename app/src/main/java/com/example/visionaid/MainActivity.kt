package com.example.visionaid

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {

    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var textureView: TextureView
    private lateinit var imageReader: ImageReader
    private lateinit var capturedImageView: ImageView
    private lateinit var backButton: ImageButton

    private lateinit var captureButton: ImageButton
    private lateinit var menuButton: ImageButton
    private lateinit var contactButton: Button

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Log.e("CameraApp", "Permission Denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textureView = findViewById(R.id.textureView)
        captureButton = findViewById(R.id.capture_button)
        menuButton = findViewById(R.id.menu_button)
        contactButton = findViewById(R.id.contact)
        capturedImageView = findViewById(R.id.capturedImageView)
        backButton = findViewById(R.id.back_button)

        // Hide contact button and captured image initially
        contactButton.visibility = View.GONE
        capturedImageView.visibility = View.GONE
        backButton.visibility = View.GONE
        menuButton.visibility = View.VISIBLE // Ensure menu button is visible initially

        // Request camera permission and open camera if granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            openCamera()
        }

        // Set up capture button to take a photo
        captureButton.setOnClickListener {
            capturePhoto()
        }

        // Set up menu button to toggle "Call Contacts" button visibility
        menuButton.setOnClickListener {
            toggleContactButtonVisibility()
        }

        // Set up contact button to open Contacts fragment
        contactButton.setOnClickListener {
            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.contact_list, Contacts())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        // Set up back button to go back to the camera screen
        backButton.setOnClickListener {
            hideCapturedImage()
        }
    }

    private fun openCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val previewSize = map?.getOutputSizes(SurfaceTexture::class.java)?.get(0) ?: Size(1920, 1080)

            imageReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.JPEG, 1)
            imageReader.setOnImageAvailableListener({ reader ->
                displayCapturedImage(reader.acquireLatestImage())
            }, null)

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraDevice = camera
                        createCameraPreviewSession(previewSize)
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        camera.close()
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        Log.e("CameraApp", "Error opening camera: $error")
                        camera.close()
                    }
                }, null)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun createCameraPreviewSession(previewSize: Size) {
        try {
            val texture = textureView.surfaceTexture
            texture?.setDefaultBufferSize(previewSize.width, previewSize.height)
            val surface = Surface(texture)

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)

            cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e("CameraApp", "Configuration failed")
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun capturePhoto() {
        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureRequestBuilder.addTarget(imageReader.surface)
            captureSession.capture(captureRequestBuilder.build(), null, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun displayCapturedImage(image: Image) {
        // Convert image to bitmap and display in ImageView
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        runOnUiThread {
            capturedImageView.setImageBitmap(bitmap)
            capturedImageView.visibility = View.VISIBLE
            backButton.visibility = View.VISIBLE
            textureView.visibility = View.GONE
            menuButton.visibility = View.GONE // Hide menu button when image is displayed
        }
        image.close()
    }

    private fun hideCapturedImage() {
        capturedImageView.visibility = View.GONE
        backButton.visibility = View.GONE
        textureView.visibility = View.VISIBLE
        menuButton.visibility = View.VISIBLE // Show menu button when going back to camera
    }

    private fun toggleContactButtonVisibility() {
        contactButton.visibility = if (contactButton.visibility == View.GONE) View.VISIBLE else View.GONE
    }
}
