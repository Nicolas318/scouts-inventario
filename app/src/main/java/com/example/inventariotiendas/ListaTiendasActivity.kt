package com.example.inventariotiendas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventariotiendas.databinding.ActivityListaTiendasBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Locale

class ListaTiendasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaTiendasBinding
    private val db = FirebaseFirestore.getInstance()
    private val listaOriginal = mutableListOf<Tienda>()
    private val listaFiltrada = mutableListOf<Tienda>()
    private lateinit var adapter: TiendaAdapter

    private var filtroEstadoActivo: String? = null  // “nueva”, “usada”, “reparar” o null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaTiendasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = FirebaseAuth.getInstance().currentUser

        // RecyclerView
        adapter = TiendaAdapter(listaFiltrada) { tienda ->
            val intent = Intent(this, DetalleTiendaActivity::class.java)
            intent.putExtra("ID", tienda.id)
            startActivity(intent)
        }
        binding.rvTiendas.layoutManager = LinearLayoutManager(this)
        binding.rvTiendas.adapter = adapter

        // SearchView
        binding.svSearch.setIconifiedByDefault(false)
        binding.svSearch.queryHint = "Buscar tiendas..."
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        // Botones de filtro
        binding.btnNueva.setOnClickListener { aplicarFiltroEstado("nueva", binding.btnNueva) }
        binding.btnUsada.setOnClickListener { aplicarFiltroEstado("usada", binding.btnUsada) }
        binding.btnReparar.setOnClickListener { aplicarFiltroEstado("reparar", binding.btnReparar) }

        // FAB
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, DetalleTiendaActivity::class.java))
        }

        // Firestore listener filtrando por grupo del usuario
        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val group = result.claims["group"] as? String ?: ""

            db.collection("tiendas")
                .whereEqualTo("grupo", group)
                .orderBy("nombre_tienda", Query.Direction.ASCENDING)
                .addSnapshotListener { snap, error ->
                    if (error != null) return@addSnapshotListener
                    listaOriginal.clear()
                    snap?.documents?.forEach { doc ->
                        val t = doc.toObject(Tienda::class.java)!!
                        t.id = doc.id
                        listaOriginal.add(t)
                    }
                    filterList(binding.svSearch.query.toString())
                }
        }

        binding.btnLogout.setOnClickListener {
            // Opcional: mostrar un diálogo de confirmación
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    // Cerrar sesión en Firebase
                    FirebaseAuth.getInstance().signOut()
                    // Volver a la pantalla de login
                    val intent = Intent(this, LoginActivity::class.java)
                    // Evitar que el usuario pueda volver con el botón atrás
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun aplicarFiltroEstado(estado: String, boton: Button) {
        // Si se pulsa el mismo botón dos veces, se quita el filtro
        filtroEstadoActivo = if (filtroEstadoActivo == estado) null else estado
        actualizarColoresFiltro()
        filterList(binding.svSearch.query.toString())
    }

    private fun actualizarColoresFiltro() {
        val colorActivo = ContextCompat.getColor(this, R.color.teal_900)
        val colorInactivo = ContextCompat.getColor(this, R.color.teal_200)

        val botones = listOf(binding.btnNueva, binding.btnUsada, binding.btnReparar)

        for (boton in botones) {
            val estadoBoton = when (boton.id) {
                binding.btnNueva.id -> "nueva"
                binding.btnUsada.id -> "usada"
                binding.btnReparar.id -> "reparar"
                else -> null
            }
            boton.backgroundTintList = ContextCompat.getColorStateList(
                this,
                if (estadoBoton == filtroEstadoActivo) R.color.teal_900 else R.color.teal_200
            )
        }
    }

    private fun filterList(query: String?) {
        val tokens = query
            ?.lowercase(Locale.getDefault())
            ?.split("\\s+".toRegex())
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        listaFiltrada.clear()

        val tiendasFiltradas = listaOriginal.filter { tienda ->
            val coincideTexto = if (tokens.isEmpty()) true else {
                tokens.all { token ->
                    tienda.nombre_tienda.lowercase(Locale.getDefault()).contains(token) ||
                            tienda.tipo_tienda.lowercase(Locale.getDefault()).contains(token) ||
                            tienda.estado.lowercase(Locale.getDefault()).contains(token)
                }
            }
            val coincideEstado = filtroEstadoActivo?.let {
                tienda.estado.equals(it, ignoreCase = true)
            } ?: true
            coincideTexto && coincideEstado
        }

        listaFiltrada.addAll(tiendasFiltradas)
        adapter.notifyDataSetChanged()
    }
}