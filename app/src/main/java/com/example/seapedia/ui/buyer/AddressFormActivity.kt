package com.example.seapedia.ui.buyer

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.seapedia.R
import com.example.seapedia.data.model.Address
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.AddressRepository
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.ActivityAddressFormBinding

class AddressFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddressFormBinding
    private lateinit var loadingDialog: LoadingDialog
    private var editAddress: Address? = null

    companion object {
        const val EXTRA_ADDRESS = "extra_address"
    }

    private val viewModel: AddressViewModel by viewModels {
        val sm = SessionManager(this)
        val apiService = ApiClient.create { sm.getToken() }
        AddressViewModelFactory(AddressRepository(apiService))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)
        editAddress = intent.getParcelableExtra(EXTRA_ADDRESS)

        setupToolbar()
        prefillIfEditing()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (editAddress != null) "Edit Address" else "Add Address"
    }

    private fun prefillIfEditing() {
        editAddress?.let { address ->
            binding.etRecipientName.setText(address.recipientName)
            binding.etPhone.setText(address.phone)
            binding.etAddressDetail.setText(address.addressDetail)
            binding.switchDefault.isChecked = address.isDefault
            if (address.isDefault) {
                binding.switchDefault.isEnabled = false
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            viewModel.saveAddress(
                id = editAddress?.id,
                recipientName = binding.etRecipientName.text.toString().trim(),
                phone = binding.etPhone.text.toString().trim(),
                addressDetail = binding.etAddressDetail.text.toString().trim(),
                isDefault = binding.switchDefault.isChecked
            )
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is AddressState.Idle -> { }
                is AddressState.Loading -> {
                    loadingDialog.show("Saving address...")
                    binding.btnSave.isEnabled = false
                }
                is AddressState.Saved -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this,
                        if (editAddress != null) "Address updated!" else "Address added!",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is AddressState.Error -> {
                    loadingDialog.dismiss()
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    Log.e("Error", state.message)
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