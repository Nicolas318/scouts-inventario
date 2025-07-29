package com.example.inventariotiendas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.inventariotiendas.databinding.ItemTiendaBinding
import com.example.inventariotiendas.R

class TiendaAdapter(
    private val items: List<Tienda>,
    private val onClick: (Tienda) -> Unit
) : RecyclerView.Adapter<TiendaAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemTiendaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tienda: Tienda) {
            binding.tvNombre.text = tienda.nombre_tienda
            binding.tvTipo.text = tienda.tipo_tienda
            binding.tvEstado.text = tienda.estado

            // Color de fondo según estado
            val colorRes = when (tienda.estado) {
                "Nueva" -> R.color.blue_state
                "Usada" -> R.color.green_state
                "Reparar" -> R.color.red_state
                else -> R.color.blue_state
            }
            binding.root.setCardBackgroundColor(
                ContextCompat.getColor(binding.root.context, colorRes)
            )

            binding.root.setOnClickListener { onClick(tienda) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTiendaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
