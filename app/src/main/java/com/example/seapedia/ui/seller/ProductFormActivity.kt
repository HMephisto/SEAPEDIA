package com.example.seapedia.ui.seller

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.seapedia.R
import com.example.seapedia.data.model.Product
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.StoreRepository
import com.example.seapedia.data.utils.Constants
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.data.utils.uriToFile
import com.example.seapedia.databinding.ActivityProductFormBinding
import com.example.seapedia.ui.product.ProductFormState
import com.example.seapedia.ui.product.ProductFormViewModel
import com.example.seapedia.ui.product.ProductFormViewModelFactory
import java.io.File

class ProductFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductFormBinding
    private lateinit var loadingDialog: LoadingDialog

    private var selectedImageFile: File? = null
    private var editProduct: Product? = null
    private var storeId: Int = -1

    companion object {
        const val EXTRA_STORE_ID = "extra_store_id"
        const val EXTRA_PRODUCT = "extra_product"
        const val RESULT_SAVED = "result_saved"
    }

    private val viewModel: ProductFormViewModel by viewModels {
        val sm = SessionManager(this)
        val apiService = ApiClient.create { sm.getToken() }
        val repository = StoreRepository(apiService)
        ProductFormViewModelFactory(repository)
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Glide.with(this).load(it).into(binding.ivProductImage)
            selectedImageFile = uriToFile(this, it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)
        storeId = intent.getIntExtra(EXTRA_STORE_ID, -1)
        editProduct = intent.getParcelableExtra(EXTRA_PRODUCT)

        setupToolbar()
        prefillIfEditing()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (editProduct != null) "Edit Product" else "Add Product"
    }

    private fun prefillIfEditing() {
        editProduct?.let { product ->
            binding.etName.setText(product.name)
            binding.etDescription.setText(product.description)
            binding.etPrice.setText(product.price)
            binding.etStock.setText(product.stock.toString())

            if (product.imageUrl.isNotBlank()) {
                Glide.with(this)
                    .load(Constants.IMAGE_URL + product.imageUrl)
                    .into(binding.ivProductImage)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnPickImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            viewModel.saveProduct(
                storeId = storeId,
                productId = editProduct?.id,
                name = binding.etName.text.toString().trim(),
                description = binding.etDescription.text.toString().trim(),
                price = binding.etPrice.text.toString().trim(),
                stock = binding.etStock.text.toString().trim(),
                imageFile = selectedImageFile
            )
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is ProductFormState.Idle -> { }
                is ProductFormState.Loading -> {
                    loadingDialog.show(
                        if (editProduct != null) "Updating product..." else "Creating product..."
                    )
                    binding.btnSave.isEnabled = false
                }
                is ProductFormState.Success -> {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this,
                        if (editProduct != null) "Product updated!" else "Product created!",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK, Intent().putExtra(RESULT_SAVED, true))
                    finish()
                }
                is ProductFormState.Error -> {
                    loadingDialog.dismiss()
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
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