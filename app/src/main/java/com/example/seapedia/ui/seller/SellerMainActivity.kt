package com.example.seapedia.ui.seller

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.seapedia.R
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.StoreRepository
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.ActivityBuyerMainBinding
import com.example.seapedia.databinding.ActivitySellerBinding

class SellerMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySellerBinding
    private lateinit var navController: NavController
    private lateinit var loadingDialog: LoadingDialog

    private val viewModel: StoreSetupViewModel by viewModels {
        val sm = SessionManager(this)
        val apiService = ApiClient.create { sm.getToken() }
        val repository = StoreRepository(apiService)
        StoreSetupViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)

        setupNavigation()
        observeStoreCheck()

        viewModel.checkStore()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.sellerDashboardFragment,
                R.id.sellerInventoryFragment,
                R.id.sellerOrdersFragment,
                R.id.sellerSettingsFragment
            )
        )

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    private fun observeStoreCheck() {
        viewModel.checkState.observe(this) { state ->
            when (state) {
                is StoreCheckState.Loading -> {
                    loadingDialog.show("Loading...")
                }
                is StoreCheckState.HasStore -> {
                    loadingDialog.dismiss()
                }
                is StoreCheckState.NoStore -> {
                    loadingDialog.dismiss()
                    val intent = Intent(this, StoreSetupActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                is StoreCheckState.Error -> {
                    loadingDialog.dismiss()
                    val intent = Intent(this, StoreSetupActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}