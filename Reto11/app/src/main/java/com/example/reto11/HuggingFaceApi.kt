package com.example.reto11
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface HuggingFaceApi {

    @Headers("Authorization: Bearer ${BuildConfig.HUGGING_FACE_API_KEY}", "Content-Type: application/json")
    @POST("models/tiiuae/falcon-7b-instruct")
    fun getStory(@Body request: HuggingFaceRequest): Call<HuggingFaceResponse>
}

// HuggingFaceRequest.kt
data class HuggingFaceRequest(val inputs: String)


// HuggingFaceResponse.kt

data class HuggingFaceResponseItem(val generated_text: String)
typealias HuggingFaceResponse = List<HuggingFaceResponseItem>
