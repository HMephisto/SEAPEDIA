package com.example.seapedia.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seapedia.R
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.AddressRepository
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.ActivityAddressBinding
import com.example.seapedia.ui.buyer.adapter.AddressAdapter

class AddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddressBinding
    private lateinit var loadingDialog: LoadingDialog
    private val addressAdapter = AddressAdapter()

    private val viewModel: AddressViewModel by viewModels {
        val sm = SessionManager(this)
        val apiService = ApiClient.create { sm.getToken() }
        AddressViewModelFactory(AddressRepository(apiService))
    }

    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) viewModel.refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Addresses"
    }

    private fun setupRecyclerView() {
        binding.rvAddresses.apply {
            adapter = addressAdapter
            layoutManager = LinearLayoutManager(this@AddressActivity)
        }

        addressAdapter.onEditClick = { address ->
            val intent = Intent(this, AddressFormActivity::class.java).apply {
                putExtra(AddressFormActivity.EXTRA_ADDRESS, address)
            }
            formLauncher.launch(intent)
        }

        addressAdapter.onDeleteClick = { address ->
            AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteAddress(address.id)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        addressAdapter.onSetDefaultClick = { address ->
            viewModel.setDefaultAddress(address.id)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(ContextCompat.getColor(this@AddressActivity, R.color.primary))
            setOnRefreshListener { viewModel.refresh() }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddAddress.setOnClickListener {
            formLauncher.launch(Intent(this, AddressFormActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.addresses.observe(this) { addresses ->
            addressAdapter.submitList(addresses)
            binding.layoutEmpty.visibility = if (addresses.isEmpty()) View.VISIBLE else View.GONE
            binding.rvAddresses.visibility = if (addresses.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isRefreshing.observe(this) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }

        viewModel.state.observe(this) { state ->
            when (state) {
                is AddressState.Idle -> { }
                is AddressState.Loading -> loadingDialog.show()
                is AddressState.Deleted -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Address deleted", Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
                is AddressState.DefaultSet -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Default address updated", Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
                is AddressState.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
                else -> { }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}