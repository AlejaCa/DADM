package com.example.reto10

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("y6ye-6gwu.json")
    fun getEmpresas(): Call<List<Empresa>>
}