package com.example.inventariotiendas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.inventariotiendas.databinding.ActivityDetalleTiendaBinding
import com.example.inventariotiendas.ui.AnnotationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

class DetalleTiendaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleTiendaBinding
    private val db = FirebaseFirestore.getInstance()
    private var tiendaId: String? = null
    private var fecha: Date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleTiendaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón deshacer
        binding.btnUndoMark.setOnClickListener {
            binding.annotationView.undoLastMark()
        }

        // Cerrar
        binding.btnClose.setOnClickListener { finish() }
        binding.btnDelete.visibility = View.GONE

        // Oculta anotaciones al inicio
        binding.flImageAnnotator.visibility = View.GONE

        // Spinner de estados
        val estados = listOf("Nueva", "Usada", "Reparar")
        val spinner: Spinner = binding.spnEstado
        spinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, estados
        )
        spinner.setSelection(0)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val estado = estados[position]
                val isRepair = estado == "Reparar"

                binding.repairOptions.visibility = if (isRepair) View.VISIBLE else View.GONE
                binding.flImageAnnotator.visibility = if (isRepair) View.VISIBLE else View.GONE

                if (!isRepair) {
                    binding.annotationView.clearMarks()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Leer ID para editar
        tiendaId = intent.getStringExtra("ID")
        if (tiendaId != null) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Confirmar borrado")
                    .setMessage("¿Quieres borrar esta tienda?")
                    .setPositiveButton("Sí") { _, _ ->
                        db.collection("tiendas").document(tiendaId!!)
                            .delete()
                            .addOnSuccessListener { finish() }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al borrar", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            db.collection("tiendas").document(tiendaId!!)
                .get()
                .addOnSuccessListener { doc ->
                    // Obtén el objeto Tienda
                    val tienda = doc.toObject(Tienda::class.java)
                    if (tienda != null) {
                        // Resto de tu restauración (campos, reparaciones, fecha, visibilidad…)
                        binding.edtTipo.setText(tienda.tipo_tienda)
                        binding.edtNombre.setText(tienda.nombre_tienda)
                        binding.edtPiquetas.setText(tienda.num_piquetas.toString())
                        binding.edtGomas.setText(tienda.num_gomas.toString())
                        val pos = estados.indexOf(tienda.estado)
                        spinner.setSelection(if (pos >= 0) pos else 0)

                        // Reparaciones…
                        binding.chkTienda.isChecked   = tienda.reparaciones?.contains("Tienda")  == true
                        binding.chkChupetes.isChecked = tienda.reparaciones?.contains("Chupetes")== true
                        binding.chkVientos.isChecked  = tienda.reparaciones?.contains("Vientos") == true

                        // Fecha…
                        fecha = tienda.fecha_revision.toDate()
                        binding.tvDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(fecha)

                        // Mostrar/ocultar anotaciones
                        val isRepair = tienda.estado == "Reparar"
                        binding.repairOptions.visibility    = if (isRepair) View.VISIBLE else View.GONE
                        binding.flImageAnnotator.visibility = if (isRepair) View.VISIBLE else View.GONE

                        // ————— Aquí restauramos las marcas —————
                        tienda.marks?.let { rawMarks ->
                            @Suppress("UNCHECKED_CAST")
                            val list = rawMarks as? List<Map<String, Double>>
                            list?.let { maps ->
                                // Convierte cada map a Pair<Float,Float>
                                val relMarks = maps.mapNotNull { m ->
                                    val x = m["x"]?.toFloat()
                                    val y = m["y"]?.toFloat()
                                    if (x != null && y != null) x to y else null
                                }
                                // Espera a que View tenga tamaño
                                binding.annotationView.post {
                                    binding.annotationView.setMarksRelative(relMarks)
                                }
                            }
                        }
                    }
                }
                .addOnCompleteListener { binding.progressBar.visibility = View.GONE }
        }

        // DatePicker
        binding.btnDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    fecha = GregorianCalendar(y, m, d).time
                    binding.tvDate.text = "${d}/${m + 1}/${y}"
                },
                cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]
            ).show()
        }

        // Guardar
        binding.btnGuardar.setOnClickListener {
            val estadoSel = spinner.selectedItem.toString()
            if (estadoSel == "Reparar" &&
                !binding.chkTienda.isChecked &&
                !binding.chkChupetes.isChecked &&
                !binding.chkVientos.isChecked
            ) {
                Toast.makeText(this, "Marca al menos una reparación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val reparaciones = mutableListOf<String>()
            if (binding.chkTienda.isChecked) reparaciones.add("Tienda")
            if (binding.chkChupetes.isChecked) reparaciones.add("Chupetes")
            if (binding.chkVientos.isChecked) reparaciones.add("Vientos")

            // Guarda marcas relativas en un List<Map<String,Double>>
            val marksRel = binding.annotationView.getMarksRelative()
            val marksMap = marksRel.map { (xr, yr) ->
                mapOf("x" to xr.toDouble(), "y" to yr.toDouble())
            }

            val data = mapOf(
                "tipo_tienda" to binding.edtTipo.text.toString(),
                "nombre_tienda" to binding.edtNombre.text.toString(),
                "num_piquetas" to binding.edtPiquetas.text.toString().toIntOrNull().orZero(),
                "num_gomas" to binding.edtGomas.text.toString().toIntOrNull().orZero(),
                "estado" to estadoSel,
                "reparaciones" to reparaciones,
                "fecha_revision" to Timestamp(fecha),
                "marks" to marksMap
            )
            if (tiendaId != null) db.collection("tiendas")
                .document(tiendaId!!).set(data, SetOptions.merge())
            else db.collection("tiendas").add(data)

            finish()
        }
    }

    private fun Int?.orZero() = this ?: 0
}
