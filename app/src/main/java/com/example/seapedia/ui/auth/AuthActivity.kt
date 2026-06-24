package com.example.seapedia.ui.auth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.seapedia.R
import com.example.seapedia.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    companion object {
        const val EXTRA_START_DESTINATION = "extra_start_destination"

        const val DESTINATION_LOGIN = "login"
        const val DESTINATION_REGISTER = "register"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        handleStartDestination()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    private fun handleStartDestination() {
        val destination = intent.getStringExtra(EXTRA_START_DESTINATION)
        if (destination == DESTINATION_REGISTER) {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.auth_nav_host_fragment) as NavHostFragment
            navHostFragment.navController.navigate(R.id.registerFragment)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}