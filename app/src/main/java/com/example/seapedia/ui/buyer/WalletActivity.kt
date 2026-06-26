package com.example.seapedia.ui.buyer

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seapedia.R
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.WalletRepository
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.ActivityWalletBinding
import com.example.seapedia.ui.buyer.adapter.TransactionAdapter
import java.text.NumberFormat
import java.util.Locale

class WalletActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWalletBinding
    private lateinit var loadingDialog: LoadingDialog
    private val transactionAdapter = TransactionAdapter()

    private val viewModel: WalletViewModel by viewModels {
        val sm = SessionManager(this)
        val apiService = ApiClient.create { sm.getToken() }
        WalletViewModelFactory(WalletRepository(apiService))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)

        setupToolbar()
        setupRecyclerView()
        setupChips()
        setupClickListeners()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Wallet"
    }

    private fun setupRecyclerView() {
        binding.rvTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(this@WalletActivity)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupChips() {
        // quick amount chips fill the input field
        binding.chip10k.setOnClickListener {
            binding.etAmount.setText("10000")
        }
        binding.chip50k.setOnClickListener {
            binding.etAmount.setText("50000")
        }
        binding.chip100k.setOnClickListener {
            binding.etAmount.setText("100000")
        }
        binding.chip250k.setOnClickListener {
            binding.etAmount.setText("250000")
        }
    }

    private fun setupClickListeners() {
        binding.btnTopUp.setOnClickListener {
            val amount = binding.etAmount.text.toString().trim()
            viewModel.topUp(amount)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(ContextCompat.getColor(this@WalletActivity, R.color.primary))
            setOnRefreshListener { viewModel.refresh() }
        }
    }

    private fun observeViewModel() {
        viewModel.wallet.observe(this) { wallet ->
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvBalance.text = format.format(wallet.balance)
        }

        viewModel.transactions.observe(this) { transactions ->
            transactionAdapter.submitList(transactions)
            binding.tvEmptyTransactions.visibility =
                if (transactions.isEmpty()) View.VISIBLE else View.GONE
            binding.rvTransactions.visibility =
                if (transactions.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isRefreshing.observe(this) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }

        viewModel.state.observe(this) { state ->
            when (state) {
                is WalletState.Idle -> { }
                is WalletState.Loading -> {
                    loadingDialog.show("Processing top up...")
                    binding.btnTopUp.isEnabled = false
                }
                is WalletState.TopUpSuccess -> {
                    loadingDialog.dismiss()
                    binding.btnTopUp.isEnabled = true
                    binding.etAmount.text?.clear()
                    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                    Toast.makeText(
                        this,
                        "Top up successful! New balance: ${format.format(state.newBalance)}",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetState()
                }
                is WalletState.Error -> {
                    loadingDialog.dismiss()
                    binding.btnTopUp.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
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