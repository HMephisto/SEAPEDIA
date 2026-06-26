package com.example.seapedia.ui.product

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.seapedia.data.model.ProductDetail
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.CartRepository
import com.example.seapedia.data.repositrory.GuestRepository
import com.example.seapedia.data.utils.Constants
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.ActivityProductDetailBinding
import com.example.seapedia.ui.auth.AuthActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var sessionManager: SessionManager

    private lateinit var cartRepository: CartRepository
    private lateinit var guestRepository: GuestRepository

    private val viewModel: ProductDetailViewModel by viewModels {
        val sm = SessionManager(this)
        val apiService = ApiClient.create { sm.getToken() }
        guestRepository = GuestRepository(apiService)
        cartRepository = CartRepository(apiService)
        ProductDetailViewModelFactory(guestRepository, cartRepository)
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        loadingDialog = LoadingDialog(this)

        setupToolbar()
        setupActionButtons()
        observeViewModel()



        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        if (productId == -1) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.loadProduct(productId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    private fun setupActionButtons() {
        val isLoggedIn = sessionManager.isLoggedIn()

        if (isLoggedIn) {
            binding.btnAddToCart.visibility = View.VISIBLE
            binding.layoutGuestNotice.visibility = View.GONE
            binding.btnLogin.visibility = View.GONE

            binding.btnAddToCart.setOnClickListener {
                val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
                viewModel.addToCart(productId)
            }
        } else {
            binding.layoutGuestNotice.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.VISIBLE
            binding.btnAddToCart.visibility = View.GONE

            binding.btnLogin.setOnClickListener {
                startActivity(Intent(this, AuthActivity::class.java))
            }
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is ProductDetailState.Loading -> {
                    loadingDialog.show("Loading product...")
                }
                is ProductDetailState.Success -> {
                    loadingDialog.dismiss()
                    bindProduct(state.product)
                }
                is ProductDetailState.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        viewModel.addToCartState.observe(this) { state ->
            when (state){
                is AddToCartUiState.Loading -> {
                    loadingDialog.show("Loading...")
                    binding.btnAddToCart.isEnabled = false
                }
                is AddToCartUiState.Success-> {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Added to cart!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is AddToCartUiState.Error -> {
                    loadingDialog.dismiss()
                    binding.btnAddToCart.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun bindProduct(product: ProductDetail) {
        Glide.with(this)
            .load(Constants.IMAGE_URL + product.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.ivProduct)

        binding.tvProductName.text = product.name
        binding.tvProductPrice.text = formatPrice(product.price)
        binding.tvStock.text = "${product.stock} items available"
        binding.tvDescription.text = product.description

        binding.tvStoreName.text = product.store.storeName
        binding.tvSellerName.text = "by ${product.store.seller.fullName}"
        binding.tvStoreAddress.text = product.store.addressDetail
        binding.tvStoreDescription.text = product.store.description

        val initials = product.store.seller.fullName
            .split(" ")
            .take(2)
            .joinToString("") { it.first().uppercase() }
        binding.tvSellerAvatar.text = initials

        binding.layoutSeller.setOnClickListener {
            // TODO: navigate to seller/store page later
            Toast.makeText(this, "Store page coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatPrice(price: String): String {
        val amount = price.toDoubleOrNull() ?: 0.0
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}