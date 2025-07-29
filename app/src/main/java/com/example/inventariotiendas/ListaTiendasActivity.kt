package com.example.inventariotiendas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventariotiendas.databinding.ActivityListaTiendasBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Locale

class ListaTiendasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaTiendasBinding
    private val db = FirebaseFirestore.getInstance()
    private val listaOriginal = mutableListOf<Tienda>()
    private val listaFiltrada = mutableListOf<Tienda>()
    private lateinit var adapter: TiendaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaTiendasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura RecyclerView con listaFiltrada
        adapter = TiendaAdapter(listaFiltrada) { tienda ->
            val intent = Intent(this, DetalleTiendaActivity::class.java)
            intent.putExtra("ID", tienda.id)
            startActivity(intent)
        }
        binding.rvTiendas.layoutManager = LinearLayoutManager(this)
        binding.rvTiendas.adapter = adapter

        // Configura SearchView para filtrar por nombre, tipo o estado
        binding.svSearch.setIconifiedByDefault(false)
        binding.svSearch.queryHint = "Buscar tiendas..."
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        // FAB para crear nueva tienda
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, DetalleTiendaActivity::class.java))
        }

        // Escucha Firestore en tiempo real
        db.collection("tiendas")
            .orderBy("nombre_tienda", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) return@addSnapshotListener
                listaOriginal.clear()
                snap?.documents?.forEach { doc ->
                    val t = doc.toObject(Tienda::class.java)!!
                    t.id = doc.id
                    listaOriginal.add(t)
                }
                // Filtra tras actualizar la lista original
                filterList(binding.svSearch.query.toString())
            }
    }

    /**
     * Filtra listaOriginal según tokens de búsqueda y actualiza listaFiltrada
     */
    private fun filterList(query: String?) {
        val tokens = query
            ?.lowercase(Locale.getDefault())
            ?.split("\\s+".toRegex())
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
        listaFiltrada.clear()
        if (tokens.isEmpty()) {
            listaFiltrada.addAll(listaOriginal)
        } else {
            listaFiltrada.addAll(
                listaOriginal.filter { tienda ->
                    tokens.all { token ->
                        tienda.nombre_tienda.lowercase(Locale.getDefault()).contains(token) ||
                                tienda.tipo_tienda.lowercase(Locale.getDefault()).contains(token) ||
                                tienda.estado.lowercase(Locale.getDefault()).contains(token)
                    }
                }
            )
        }
        adapter.notifyDataSetChanged()
    }
}
