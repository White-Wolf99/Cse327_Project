// F:\LLM_Project\app\src\main\java\com\example\llm_project\MainInteractionFragment.kt
package com.example.llm_project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.llm_project.databinding.FragmentMainInteractionBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import android.app.Activity


class MainInteractionFragment : Fragment() {

    private var _binding: FragmentMainInteractionBinding? = null
    private val binding get() = _binding!!
    private var currentPhotoPath: String = ""

    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val results = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            binding.inputEditText.setText(results?.get(0))
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            binding.imageThumbnail.setImageURI(uri)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val photoFile = File(currentPhotoPath)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            binding.imageThumbnail.setImageURI(uri)
        }
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val content = requireContext().contentResolver
                    .openInputStream(uri)?.bufferedReader().use { it?.readText() }
                binding.inputEditText.setText(content)
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainInteractionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        binding.progressBar.visibility = View.GONE
    }

    private fun setupClickListeners() {
        binding.voiceInputButton.setOnClickListener { startVoiceInput() }
        binding.fileUploadButton.setOnClickListener { openFilePicker() }
        binding.selectImageButton.setOnClickListener { showImageSourceDialog() }
        binding.runM1Button.setOnClickListener { runModel(1) }
        binding.runM2Button.setOnClickListener { runModel(2) }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        speechRecognizerLauncher.launch(intent)
    }

    private fun openFilePicker() {
        filePickerLauncher.launch("text/*")
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = requireContext().externalCacheDir
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoURI)
        } catch (ex: IOException) {
            Toast.makeText(context, "Error creating file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun runModel(modelId: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.outputTextView.text = ""

        binding.root.postDelayed({
            binding.progressBar.visibility = View.GONE
            binding.outputTextView.text = when (modelId) {
                1 -> "M1 Result:\nProcessed input: ${binding.inputEditText.text}"
                2 -> "M2 Result:\nImage processed with text: ${binding.inputEditText.text}"
                else -> "Invalid model"
            }
        }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}