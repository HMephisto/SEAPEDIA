package com.example.seapedia.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.seapedia.MainActivity
import com.example.seapedia.R
import com.example.seapedia.data.model.RoleConstants
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.ActivitySplashBinding
import com.example.seapedia.ui.buyer.BuyerMainActivity
import com.example.seapedia.ui.guest.GuestMainActivity
import com.example.seapedia.ui.seller.SellerMainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        lifecycleScope.launch {
            delay(2000L)
            navigateToNextScreen()
        }
    }

    private fun navigateToNextScreen() {
        val destination = if (sessionManager.isLoggedIn()) {
            when (sessionManager.getActiveRole()) {
                RoleConstants.BUYER -> BuyerMainActivity::class.java
                RoleConstants.SELLER -> SellerMainActivity::class.java
                else                -> GuestMainActivity::class.java
            }
        } else {
            GuestMainActivity::class.java
        }

        startActivity(Intent(this, destination))
        finish()

    }


}