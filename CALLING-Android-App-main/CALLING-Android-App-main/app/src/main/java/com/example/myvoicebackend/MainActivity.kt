package com.example.myvoicebackend // Change this to your package name

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    // --- 1. THIS IS THE MOST IMPORTANT PART ---
    // This is the URL from your Vercel deployment.
    // This is the correct, permanent URL.
    private val VERCEL_API_URL = "https://my-voice-backend.vercel.app/api/chat"

    private val PERMISSION_REQUEST_CODE = 101
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private lateinit var tts: TextToSpeech

    // Networking client
    private val httpClient = OkHttpClient()

    // UI Views
    private lateinit var geminiResponseText: TextView
    private lateinit var recordButton: Button
    private lateinit var loadingIndicator: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        geminiResponseText = findViewById(R.id.geminiResponseText)
        recordButton = findViewById(R.id.recordButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)

        if (!hasAudioPermission()) {
            requestAudioPermission()
        }

        // Initialize TextToSpeech
        tts = TextToSpeech(this, this)
        setupRecordButton()
    }

    // --- 2. TextToSpeech Setup ---
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
                recordButton.isEnabled = true // Enable button when TTS is ready
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    // --- 3. Button Press Logic ---
    private fun setupRecordButton() {
        recordButton.setOnTouchListener { _, event ->
            // We need to check permissions again in case user denied
            if (!hasAudioPermission()) {
                requestAudioPermission()
                return@setOnTouchListener false
            }
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording()
                    recordButton.text = "Listening..."
                    true
                }
                MotionEvent.ACTION_UP -> {
                    stopAndSendAudio()
                    recordButton.text = "TALK"
                    true
                }
                else -> false
            }
        }
    }

    // --- 4. Audio Recording Logic ---
    private fun startRecording() {
        // We will save the file to the app's cache directory
        audioFile = File(cacheDir, "recorded_audio.mp3")

        mediaRecorder = MediaRecorder(this).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile!!.absolutePath)
            try {
                prepare()
                start()
                geminiResponseText.text = "Listening..."
            } catch (e: Exception) {
                Log.e("MediaRecorder", "Prepare failed: ${e.message}")
            }
        }
    }

    // --- 5. Networking Logic (The Core) ---
    private fun stopAndSendAudio() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        geminiResponseText.text = "Thinking..."
        loadingIndicator.visibility = ProgressBar.VISIBLE

        audioFile?.let { file ->
            // We run the network call on a background thread
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 1. Read audio file and Base64 encode it
                    val audioBytes = file.readBytes()
                    val audioBase64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
                    file.delete() // Clean up the temp file

                    // 2. Create the JSON payload for Vercel
                    val jsonPayload = JSONObject()
                    jsonPayload.put("audioData", audioBase64)
                    jsonPayload.put("mimeType", "audio/mp3") // Must match the mimeType in your Vercel code

                    val requestBody = jsonPayload.toString()
                        .toRequestBody("application/json; charset=utf-8".toMediaType())

                    // 3. Build the network request
                    val request = Request.Builder()
                        .url(VERCEL_API_URL)
                        .post(requestBody)
                        .build()

                    // 4. Send the request
                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw Exception("Server Error: ${response.code}")
                        }

                        // 5. Get the text response from Vercel's JSON
                        val responseBody = response.body?.string() ?: ""
                        val responseJson = JSONObject(responseBody)
                        val responseText = responseJson.optString("text", "Sorry, I couldn't understand that.")
                        
                        // 6. Update UI on the main thread
                        withContext(Dispatchers.Main) {
                            loadingIndicator.visibility = ProgressBar.INVISIBLE
                            geminiResponseText.text = responseText
                            speakResponse(responseText) // Speak the response
                        }
                    }
                } catch (e: Exception) {
                    Log.e("NetworkCall", "Error: ${e.message}")
                    withContext(Dispatchers.Main) {
                        loadingIndicator.visibility = ProgressBar.INVISIBLE
                        geminiResponseText.text = "Error: ${e.message}"
                    }
                }
            }
        }
    }

    // --- 6. TextToSpeech Speak Logic ---
    private fun speakResponse(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    // --- 7. Permission Handling ---
    private fun hasAudioPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            PERMISSION_REQUEST_CODE
        )
    }
    
    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission is required to use this app", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- 8. Cleanup ---
    override fun onDestroy() {
        // Don't forget to shut down TTS!
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}