package com.example.seapedia.ui.buyer

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seapedia.R
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.CartRepository
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.FragmentBuyerCartBinding
import com.example.seapedia.databinding.FragmentGuestHomeBinding
import com.example.seapedia.ui.buyer.adapter.CartAdapter
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale


class BuyerCartFragment : Fragment() {
    private var _binding: FragmentBuyerCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var loadingDialog: LoadingDialog

    private val viewModel: BuyerCartViewModel by viewModels {
        val apiService = ApiClient.create { sessionManager.getToken() }
        CartViewModelFactory(CartRepository(apiService))
    }

    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuyerCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        loadingDialog = LoadingDialog(requireContext())

        setupAdapter()
        setupSwipeRefresh()
        setupClearCartButton()
        observeViewModel()
    }

    private fun setupAdapter() {
        cartAdapter = CartAdapter(
            onIncrease = { item -> viewModel.updateQuantity(item.id, item.quantity + 1) },
            onDecrease = { item ->
                if (item.quantity > 1) viewModel.updateQuantity(item.id, item.quantity - 1)
                else confirmRemove(item.id)
            },
            onRemove = { item -> confirmRemove(item.id) }
        )
        binding.rvCartItems.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadCart() }
    }

    private fun setupClearCartButton() {
        binding.btnClearCart.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear Cart")
                .setMessage("Remove all items from your cart?")
                .setPositiveButton("Clear") { _, _ -> viewModel.clearCart() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun confirmRemove(itemId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Item")
            .setMessage("Remove this item from your cart?")
            .setPositiveButton("Remove") { _, _ -> viewModel.removeItem(itemId) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state.isLoading

            binding.emptyState.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
            binding.cartContent.visibility = if (!state.isEmpty && !state.isLoading) View.VISIBLE else View.GONE

            cartAdapter.submitList(state.items)
            binding.tvStoreName.text = state.store?.storeName ?: ""
            binding.tvTotal.text = "Rp ${formatter.format(state.total)}"
            binding.btnClearCart.visibility = if (state.items.isNotEmpty()) View.VISIBLE else View.GONE

            state.error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            if (state.isLoading) loadingDialog.show("Updating cart...")
            else loadingDialog.dismiss()

            state.error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.resetActionState()
            }
        }

        viewModel.updatingItemId.observe(viewLifecycleOwner) { itemId ->
            cartAdapter.updatingItemId = itemId
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}