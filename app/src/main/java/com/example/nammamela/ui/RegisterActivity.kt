package com.example.nammamela.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.nammamela.MainActivity
import com.example.nammamela.R
import com.example.nammamela.databinding.ActivityRegisterBinding
import com.example.nammamela.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        viewModel.user.observe(this) { user ->
            if (user != null) {
                showSnackbar("Registration successful")
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                showSnackbar(error, isError = true)
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingLayout.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            setButtonsEnabled(!isLoading)
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewModel.register(name, email, password)
                } else {
                    Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnRegister.isEnabled = enabled
        binding.btnRegister.alpha = if (enabled) 1.0f else 0.5f
    }

    private fun showSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        if (isError) {
            snackbar.setBackgroundTint(getColor(R.color.theatre_red))
        } else {
            snackbar.setBackgroundTint(getColor(R.color.accent_gold))
            snackbar.setTextColor(getColor(R.color.background_dark))
        }
        snackbar.show()
    }
}
