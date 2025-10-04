package com.example.inventariotiendas

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.inventariotiendas.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usamos View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Instancia de FirebaseAuth
        auth = Firebase.auth

        // Botón de login
        binding.btnLogin.setOnClickListener {
            login()
        }

        // TextView "¿Has olvidado tu contraseña?"
        binding.tvForgotPassword.setOnClickListener {
            showResetPasswordDialog()
        }
    }

    private fun login() {
        val email = binding.edtEmail.text.toString().trim()
        val pass  = binding.edtPassword.text.toString().trim()

        // Validaciones básicas
        if (email.isEmpty() || pass.isEmpty()) {
            showError("Email y contraseña son obligatorios")
            return
        }

        // Ocultamos el texto de error
        binding.tvError.visibility = View.GONE
        // Mostramos un snack bar de carga
        val snack = Snackbar.make(binding.root, "Iniciando sesión…", Snackbar.LENGTH_INDEFINITE)
        snack.show()

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                snack.dismiss()
                // Navegamos a la lista y cerramos esta actividad
                startActivity(Intent(this, ListaTiendasActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                snack.dismiss()
                showError(e.localizedMessage ?: "Error al autenticar")
            }
    }

    private fun showError(message: String) {
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = message
    }

    // Función para mostrar el diálogo de recuperación de contraseña
    private fun showResetPasswordDialog() {
        val emailInput = androidx.appcompat.widget.AppCompatEditText(this)
        emailInput.hint = "Introduce tu correo"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Restablecer contraseña")
            .setMessage("Introduce tu correo para recibir un email de recuperación")
            .setView(emailInput)
            .setPositiveButton("Enviar") { _, _ ->
                val email = emailInput.text.toString().trim()
                if (email.isNotEmpty()) {
                    sendPasswordResetEmail(email)
                } else {
                    showError("Debes introducir un correo válido")
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    // Función para enviar email de recuperación
    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Correo de recuperación enviado", Snackbar.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                showError(e.localizedMessage ?: "Error al enviar el correo")
            }
    }
}
