package com.example.reto11

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : ComponentActivity() {
    private lateinit var editTextTopic: EditText
    private lateinit var spinnerStyle: Spinner
    private lateinit var buttonGenerate: Button
    private lateinit var textViewStory: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextTopic = findViewById(R.id.editTextTopic)
        spinnerStyle = findViewById(R.id.spinnerStyle)
        buttonGenerate = findViewById(R.id.buttonGenerate)
        textViewStory = findViewById(R.id.textViewStory)

        val styles = arrayOf("Fantasy", "Aventure", "Horror")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, styles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStyle.adapter = adapter
        buttonGenerate.setOnClickListener { generateStory() }
    }

    private fun generateStory() {
        val topic = editTextTopic.text.toString()
        val style = spinnerStyle.selectedItem.toString()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-inference.huggingface.co/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(HuggingFaceApi::class.java)
        val request = HuggingFaceRequest("Write a $style about $topic in about 80 words")

        api.getStory(request).enqueue(object : Callback<HuggingFaceResponse> {
            override fun onResponse(call: Call<HuggingFaceResponse>, response: Response<HuggingFaceResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val generatedText = response.body()?.firstOrNull()?.generated_text ?: "No se generó ninguna historia."
                    textViewStory.text = generatedText
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("API_ERROR", errorMessage)
                    textViewStory.text = "Error al generar la historia: $errorMessage"
                }
            }


            override fun onFailure(call: Call<HuggingFaceResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error en la petición: ${t.message}")
                textViewStory.text = "Error al generar la historia. Revisa tu conexión a Internet."
            }
        })
    }

}