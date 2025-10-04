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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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

        // --- Spinner de tipo de tienda ---
        val tiposTienda = listOf("Canadiense", "Iglú")
        val spnTipo: Spinner = binding.spnTipoTienda
        spnTipo.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, tiposTienda
        )
        spnTipo.setSelection(0)

        // --- Spinner de estados ---
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

        // --- Leer ID para editar ---
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
                    val tienda = doc.toObject(Tienda::class.java)
                    if (tienda != null) {
                        // --- Restaurar datos ---
                        val posTipo = tiposTienda.indexOf(tienda.tipo_tienda)
                        spnTipo.setSelection(if (posTipo >= 0) posTipo else 0)
                        binding.edtNombre.setText(tienda.nombre_tienda)
                        binding.edtPiquetas.setText(tienda.num_piquetas.toString())
                        binding.edtGomas.setText(tienda.num_gomas.toString())

                        val posEstado = estados.indexOf(tienda.estado)
                        spinner.setSelection(if (posEstado >= 0) posEstado else 0)

                        // Reparaciones
                        binding.chkTienda.isChecked   = tienda.reparaciones?.contains("Tienda")  == true
                        binding.chkChupetes.isChecked = tienda.reparaciones?.contains("Chupetes")== true
                        binding.chkVientos.isChecked  = tienda.reparaciones?.contains("Vientos") == true

                        // Configurar currentType según checkbox seleccionado
                        binding.chkTienda.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) binding.annotationView.currentType = "Tienda"
                        }
                        binding.chkChupetes.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) binding.annotationView.currentType = "Chupete"
                        }
                        binding.chkVientos.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) binding.annotationView.currentType = "Viento"
                        }


                        // Fecha
                        fecha = tienda.fecha_revision.toDate()
                        binding.tvDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(fecha)

                        // Mostrar/ocultar anotaciones
                        val isRepair = tienda.estado == "Reparar"
                        binding.repairOptions.visibility    = if (isRepair) View.VISIBLE else View.GONE
                        binding.flImageAnnotator.visibility = if (isRepair) View.VISIBLE else View.GONE

                        // --- Restaurar marcas con tipo y color ---
                        tienda.marks?.let { rawMarks ->
                            @Suppress("UNCHECKED_CAST")
                            val list = rawMarks as? List<Map<String, Any>>
                            list?.let { maps ->
                                binding.annotationView.post {
                                    binding.annotationView.setMarksRelative(maps)
                                }
                            }
                        }
                    }
                }
                .addOnCompleteListener { binding.progressBar.visibility = View.GONE }
        }

        // --- DatePicker ---
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

        // --- Guardar ---
        binding.btnGuardar.setOnClickListener {
            val estadoSel = spinner.selectedItem.toString()
            val tipoSel = spnTipo.selectedItem.toString()
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (estadoSel == "Reparar" &&
                !binding.chkTienda.isChecked &&
                !binding.chkChupetes.isChecked &&
                !binding.chkVientos.isChecked
            ) {
                Toast.makeText(this, "Marca al menos una reparación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lista de reparaciones
            val reparaciones = mutableListOf<String>()
            if (binding.chkTienda.isChecked) reparaciones.add("Tienda")
            if (binding.chkChupetes.isChecked) reparaciones.add("Chupetes")
            if (binding.chkVientos.isChecked) reparaciones.add("Vientos")

            // Guardar marcas
            val marksMap = binding.annotationView.getMarksRelative()

            // --- Obtener el grupo del usuario correctamente ---
            currentUser?.getIdToken(false)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val claims = task.result?.claims
                    val grupo = claims?.get("group") as? String

                    if (grupo == null) {
                        Toast.makeText(this, "No se pudo obtener el grupo del usuario", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    val data = mapOf(
                        "tipo_tienda" to tipoSel,
                        "nombre_tienda" to binding.edtNombre.text.toString(),
                        "num_piquetas" to binding.edtPiquetas.text.toString().toIntOrNull().orZero(),
                        "num_gomas" to binding.edtGomas.text.toString().toIntOrNull().orZero(),
                        "estado" to estadoSel,
                        "reparaciones" to reparaciones,
                        "fecha_revision" to Timestamp(fecha),
                        "marks" to marksMap,
                        "grupo" to grupo
                    )

                    if (tiendaId != null)
                        db.collection("tiendas").document(tiendaId!!).set(data, SetOptions.merge())
                    else
                        db.collection("tiendas").add(data)

                    finish()
                } else {
                    Toast.makeText(this, "Error al obtener el token del usuario", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun Int?.orZero() = this ?: 0
}