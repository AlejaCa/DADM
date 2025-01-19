package com.example.reto8

import android.app.AlertDialog
import android.database.Cursor
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    private lateinit var db: DatabaseHelper
    lateinit var editTextName: EditText
    lateinit var editTextNameUpdate: EditText
    lateinit var editTextID:EditText
    lateinit var buttonAdd: Button
    lateinit var buttonUpdate: Button
    lateinit var buttonDelete: Button
    lateinit var buttonSearch: Button
    lateinit var editTextSearchName: EditText
    lateinit var textViewSearchResult: TextView
    lateinit var editTextURL: EditText
    lateinit var editTextURLUpdate: EditText
    lateinit var editTextPhone: EditText
    lateinit var editTextPhoneUpdate: EditText
    lateinit var editTextEmail: EditText
    lateinit var editTextEmailUpdate: EditText
    lateinit var editTextPyS: EditText
    lateinit var editTextPySUpdate: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)

        editTextName = findViewById(R.id.editTextName)
        editTextNameUpdate = findViewById(R.id.editTextNameUpdate)
        editTextID = findViewById(R.id.editTextID)
        buttonAdd = findViewById(R.id.buttonAdd)
        buttonDelete = findViewById(R.id.buttonDelete)
        buttonUpdate = findViewById(R.id.buttonUpdate)
        editTextURL= findViewById(R.id.editTextURL)
        editTextURLUpdate = findViewById(R.id.editTextURLUpdate)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextPhoneUpdate = findViewById(R.id.editTextPhoneUpdate)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextEmailUpdate = findViewById(R.id.editTextEmailUpdate)
        editTextPyS = findViewById(R.id.editTextPyS)
        editTextPySUpdate = findViewById(R.id.editTextPySUpdate)
        val checkBoxConsultoria: CheckBox = findViewById(R.id.checkBoxConsultoria)
        val checkBoxDesarrollo: CheckBox = findViewById(R.id.checkBoxDesarrollo)
        val checkBoxFabrica: CheckBox = findViewById(R.id.checkBoxFabrica)
        val checkBoxConsultoriaUpdate: CheckBox = findViewById(R.id.checkBoxConsultoriaUpdate)
        val checkBoxDesarrolloUpdate: CheckBox = findViewById(R.id.checkBoxDesarrolloUpdate)
        val checkBoxFabricaUpdate: CheckBox = findViewById(R.id.checkBoxFabricaUpdate)
        buttonAdd.setOnClickListener {
            val name = editTextName.getText().toString()
            val url = editTextURL.getText().toString()
            val phoneStr = editTextPhone.text.toString()
            val email = editTextEmail.text.toString()
            val pys = editTextPyS.text.toString()
            val selectedCategory = mutableListOf<String>()
            if (checkBoxConsultoria.isChecked) selectedCategory.add("Consultoría")
            if (checkBoxDesarrollo.isChecked) selectedCategory.add("Desarrollo a la Medida")
            if (checkBoxFabrica.isChecked) selectedCategory.add("Fábrica de Software")
            if (name.isEmpty()  || url.isEmpty() || phoneStr.isEmpty() || email.isEmpty() ||
                pys.isEmpty() || selectedCategory.isEmpty()) {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            }
            val phone = phoneStr.toInt()
            val isInserted = db.insertData(name,url,phone,email,pys,selectedCategory.joinToString(","))
            if (isInserted) Toast.makeText(
                this@MainActivity,
                "Data Inserted",
                Toast.LENGTH_SHORT
            ).show()
            else Toast.makeText(
                this@MainActivity,
                "Data Not Inserted",
                Toast.LENGTH_SHORT
            ).show()
        }

        buttonUpdate.setOnClickListener{
            val id: String = editTextID.getText().toString()
            val name = editTextNameUpdate.text.toString()
            val url = editTextURLUpdate.text.toString()
            val phoneStr = editTextPhoneUpdate.text.toString()
            val email =editTextEmailUpdate.text.toString()
            val pys = editTextPySUpdate.text.toString()
            val selectedCategory = mutableListOf<String>()
            if (checkBoxConsultoriaUpdate.isChecked) selectedCategory.add("Consultoría")
            if (checkBoxDesarrolloUpdate.isChecked) selectedCategory.add("Desarrollo a la Medida")
            if (checkBoxFabricaUpdate.isChecked) selectedCategory.add("Fábrica de Software")
            if (id.isEmpty() || name.isEmpty() || url.isEmpty() || phoneStr.isEmpty() ||
                email.isEmpty() || pys.isEmpty() || selectedCategory.isEmpty()) {
                  Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            }

            val phone = phoneStr.toInt()
            AlertDialog.Builder(this)
                .setTitle("Update Confirmation")
                .setMessage("Are you sure you want to update this entry?")
                .setPositiveButton("Yes") { _, _ ->
                    val isUpdated = db.updateData(id, name, url, phone, email,pys, selectedCategory.joinToString(","))
                    if (isUpdated) {
                        Toast.makeText(this, "Data Updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Data Not Updated", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss() // Close dialog if the user selects "No"
                }
                .show()
        }

       buttonDelete.setOnClickListener{
               val id = editTextID.getText().toString()

            if (id.isEmpty()) {
                     Toast.makeText(this, "Please enter an ID", Toast.LENGTH_SHORT).show();
          }
           AlertDialog.Builder(this)
               .setTitle("Delete Confirmation")
               .setMessage("Are you sure you want to delete this entry?")
               .setPositiveButton("Yes") { _, _ ->
                   val deletedRows = db.deleteData(id)
                   if (deletedRows > 0) {
                       Toast.makeText(this, "Data Deleted", Toast.LENGTH_SHORT).show()
                   } else {
                       Toast.makeText(this, "Data Not Deleted", Toast.LENGTH_SHORT).show()
                   }
               }
               .setNegativeButton("No") { dialog, _ ->
                   dialog.dismiss() // Close dialog if the user selects "No"
               }
               .show()
         }
        buttonSearch = findViewById(R.id.buttonSearch)
        editTextSearchName = findViewById(R.id.editTextSearchName)
        textViewSearchResult= findViewById(R.id.textViewSearchResult)
        textViewSearchResult.movementMethod = ScrollingMovementMethod()
        val checkBoxConsultoriaSearch: CheckBox = findViewById(R.id.checkBoxConsultoriaSearch)
        val checkBoxDesarrolloSearch: CheckBox = findViewById(R.id.checkBoxDesarrolloSearch)
        val checkBoxFabricaSearch: CheckBox = findViewById(R.id.checkBoxFabricaSearch)

        buttonSearch.setOnClickListener {
            val name = editTextSearchName.text.toString()
            val selectedCategory = mutableListOf<String>()
            if (checkBoxConsultoriaSearch.isChecked) selectedCategory.add("Consultoría")
            if (checkBoxDesarrolloSearch.isChecked) selectedCategory.add("Desarrollo a la Medida")
            if (checkBoxFabricaSearch.isChecked) selectedCategory.add("Fábrica de Software")

            val res: Cursor = if (name.isEmpty() && selectedCategory.isEmpty()) {
                // Fetch all data if no parameters are provided
                db.getAllData()
            } else {
                db.searchUserWithMultipleAges(name, selectedCategory)
            }

            if (res.count == 0) {
                textViewSearchResult.text = "No users found."
                return@setOnClickListener
            }

            val buffer = StringBuilder()
            while (res.moveToNext()) {
                buffer.append("ID: ").append(res.getString(0)).append("\n")
                buffer.append("Nombre de la Empresa: ").append(res.getString(1)).append("\n")
                buffer.append("URL de la Empresa: ").append(res.getString(3)).append("\n")
                buffer.append("Teléfono de Contacto: ").append(res.getString(4)).append("\n")
                buffer.append("Email de Contacto: ").append(res.getString(5)).append("\n")
                buffer.append("Productos y Servicios: ").append(res.getString(6)).append("\n")
                buffer.append("Clasificación de la Empresa: ").append(res.getString(2)).append("\n\n")
            }
            textViewSearchResult.text = buffer.toString()
        }



    }
    override  fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val viewSwitcher: ViewFlipper = findViewById(R.id.viewFlipper)

        return when (item.itemId) {
            R.id.menu_add -> {
                viewSwitcher.displayedChild = 0 // Show Add User view
                true
            }
            R.id.menu_search -> {
                viewSwitcher.displayedChild = 1 // Show Search User view
                true
            }
            R.id.menu_update_delete -> {
                viewSwitcher.displayedChild = 2 // Show Search User view
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    fun showMessage(title: String, message: String) {
        Toast.makeText(
            this, """
     $title
     $message
     """.trimIndent(), Toast.LENGTH_LONG
        ).show()
    }
}

