package com.example.reto10

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.ComponentActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var empresaAdapter: EmpresaAdapter
    private lateinit var etBuscar: EditText
    private lateinit var spinnerDepartamento: Spinner
    private var listaEmpresas: List<Empresa> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etBuscar = findViewById(R.id.etBuscar)
        spinnerDepartamento = findViewById(R.id.spinnerDepartamento)
        recyclerView = findViewById(R.id.recyclerEmpresas)

        recyclerView.layoutManager = LinearLayoutManager(this)
        empresaAdapter = EmpresaAdapter(listaEmpresas)
        recyclerView.adapter = empresaAdapter

        getEmpresas()

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filtrarEmpresas()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        spinnerDepartamento.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filtrarEmpresas() // Call filtering function when user selects an item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing if no selection is made
            }
        }
    }

    private fun getEmpresas() {
        RetrofitClient.instance.getEmpresas().enqueue(object : Callback<List<Empresa>> {
            override fun onResponse(call: Call<List<Empresa>>, response: Response<List<Empresa>>) {
                if (response.isSuccessful) {
                    listaEmpresas = response.body() ?: listOf()
                    setupSpinner()
                    empresaAdapter.updateList(listaEmpresas)
                }
            }

            override fun onFailure(call: Call<List<Empresa>>, t: Throwable) {
                println("Error: ${t.message}")
            }
        })
    }

    private fun setupSpinner() {
        val departamentos = listaEmpresas.map { it.municipio }.distinct().sorted()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departamentos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDepartamento.adapter = adapter
    }

    private fun filtrarEmpresas() {
        val filtroNombre = etBuscar.text.toString().lowercase()
        val filtroDepartamento = spinnerDepartamento.selectedItem?.toString() ?: ""

        val empresasFiltradas = listaEmpresas.filter {
            (it.razon_social.lowercase().contains(filtroNombre) || filtroNombre.isEmpty()) &&
                    (it.municipio == filtroDepartamento || filtroDepartamento.isEmpty())
        }
        empresaAdapter.updateList(empresasFiltradas)
    }
}
