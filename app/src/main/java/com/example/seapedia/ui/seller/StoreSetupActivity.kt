package com.example.seapedia.ui.seller

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.seapedia.R
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.StoreRepository
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.ActivityStoreSetupBinding

class StoreSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoreSetupBinding
    private lateinit var loadingDialog: LoadingDialog

    private val viewModel: StoreSetupViewModel by viewModels {
        val sm = SessionManager(this)
        val apiService = ApiClient.create { sm.getToken() }
        val repository = StoreRepository(apiService)
        StoreSetupViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoreSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)

        setupTermsText()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupTermsText() {
        val fullText = "By creating a store, you agree to Seapedia's Seller Terms of Service."
        val spannable = SpannableString(fullText)
        val linkColor = ContextCompat.getColor(this, R.color.primary)
        val start = fullText.indexOf("Seller Terms of Service")
        val end = start + "Seller Terms of Service".length
        spannable.setSpan(
            ForegroundColorSpan(linkColor),
            start, end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvTerms.text = spannable
    }

    private fun setupClickListeners() {
        binding.btnCreateStore.setOnClickListener {
            val storeName = binding.etStoreName.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            viewModel.createStore(storeName, description, address)
        }
    }

    private fun observeViewModel() {
        viewModel.setupState.observe(this) { state ->
            when (state) {
                is StoreSetupState.Idle -> { }
                is StoreSetupState.Loading -> {
                    loadingDialog.show("Creating your store...")
                    binding.btnCreateStore.isEnabled = false
                }
                is StoreSetupState.Created -> {
                    loadingDialog.dismiss()
                    val intent = Intent(this, SellerMainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                is StoreSetupState.Error -> {
                    loadingDialog.dismiss()
                    binding.btnCreateStore.isEnabled = true
                    binding.tilStoreName.error = state.message
                }
            }
        }
    }
}