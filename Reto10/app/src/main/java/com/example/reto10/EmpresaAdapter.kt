package com.example.reto10

import android.app.ProgressDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class EmpresaAdapter(private var empresas: List<Empresa>) : RecyclerView.Adapter<EmpresaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRazonSocial: TextView = view.findViewById(R.id.tvRazonSocial)
        val tvDireccion: TextView = view.findViewById(R.id.tvDireccion)
        val btnVerMapa: Button = view.findViewById(R.id.btnVerMapa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_empresa, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val empresa = empresas[position]
        holder.tvRazonSocial.text = empresa.razon_social
        holder.tvDireccion.text = empresa.dir_comercial
        holder.btnVerMapa.setOnClickListener {
            obtenerCoordenadasYMostrarMapa(holder.itemView.context, empresa.dir_comercial)
        }
    }

    override fun getItemCount(): Int = empresas.size

    fun updateList(newList: List<Empresa>) {
        empresas = newList
        notifyDataSetChanged()
    }
    private fun obtenerCoordenadasYMostrarMapa(context: android.content.Context, direccion: String) {
    val progressDialog = ProgressDialog(context).apply {
        setMessage("Buscando ubicación...")
        setCancelable(false)
        show()
    }

    Thread {
        try {
            val formattedAddress = URLEncoder.encode(direccion, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?format=json&q=$formattedAddress"

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla/5.0") // Important to avoid being blocked

            val response = connection.inputStream.bufferedReader().readText()
            Log.d("NominatimResponse", "Response: $response") // Log response for debugging

            val jsonArray = JSONArray(response)
            if (jsonArray.length() > 0) {
                val location = jsonArray.getJSONObject(0)
                val latitud = location.getDouble("lat")
                val longitud = location.getDouble("lon")

                (context as ComponentActivity).runOnUiThread {
                    progressDialog.dismiss()
                    val intent = Intent(context, MapaActivity::class.java).apply {
                        putExtra("latitud", latitud)
                        putExtra("longitud", longitud)
                    }
                    context.startActivity(intent)
                }
            } else {
                (context as ComponentActivity).runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(context, "Ubicación no encontrada: $direccion", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            (context as ComponentActivity).runOnUiThread {
                progressDialog.dismiss()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }.start()
    }
}
