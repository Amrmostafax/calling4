package com.example.myvoicebackend

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

    // This is the URL from your Vercel deployment.
    // Make sure this is your correct backend URL.
    private val VERCEL_API_URL = "https://my-voice-backend.vercel.app/api/chat"

    private val PERMISSION_REQUEST_CODE = 101
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private lateinit var tts: TextToSpeech

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

        tts = TextToSpeech(this, this)
        setupRecordButton()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
                recordButton.isEnabled = true
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    private fun setupRecordButton() {
        recordButton.setOnTouchListener { _, event ->
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

    private fun startRecording() {
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

    private fun stopAndSendAudio() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        geminiResponseText.text = "Thinking..."
        loadingIndicator.visibility = ProgressBar.VISIBLE

        audioFile?.let { file ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val audioBytes = file.readBytes()
                    val audioBase64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
                    file.delete()

                    val jsonPayload = JSONObject()
                    jsonPayload.put("audioData", audioBase64)
                    jsonPayload.put("mimeType", "audio/mp3")

                    val requestBody = jsonPayload.toString()
                        .toRequestBody("application/json; charset=utf-8".toMediaType())

                    val request = Request.Builder()
                        .url(VERCEL_API_URL)
                        .post(requestBody)
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw Exception("Server Error: ${response.code}")
                        }

                        val responseBody = response.body?.string() ?: ""
                        val responseJson = JSONObject(responseBody)
                        val responseText = responseJson.optString("text", "Sorry, I couldn't understand that.")
                        
                        withContext(Dispatchers.Main) {
                            loadingIndicator.visibility = ProgressBar.INVISIBLE
                            geminiResponseText.text = responseText
                            speakResponse(responseText)
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

    private fun speakResponse(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

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

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
