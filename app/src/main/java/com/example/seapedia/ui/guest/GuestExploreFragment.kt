package com.example.seapedia.ui.guest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.seapedia.R
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.GuestRepository
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.FragmentGuestExploreBinding
import com.example.seapedia.ui.guest.adapter.GuestExploreViewModel
import com.example.seapedia.ui.guest.adapter.GuestExploreViewModelFactory
import com.example.seapedia.ui.guest.adapter.ProductGridAdapter
import com.example.seapedia.ui.product.ProductDetailActivity

class GuestExploreFragment : Fragment() {
    private var _binding: FragmentGuestExploreBinding? = null
    private val binding get() = _binding!!

    private val productGridAdapter = ProductGridAdapter()

    private val viewModel: GuestExploreViewModel by viewModels {
        val sessionManager = SessionManager(requireContext())
        val apiService = ApiClient.create { sessionManager.getToken() }
        val repository = GuestRepository(apiService)
        GuestExploreViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuestExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchBar()
        setupSwipeRefresh()
        observeViewModel()

        productGridAdapter.onItemClick = { product ->
            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        binding.rvProducts.apply {
            adapter = productGridAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onSearchQuery(s?.toString() ?: "")
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.primary)
            )
            setOnRefreshListener {
                val currentQuery = binding.etSearch.text?.toString()
                viewModel.refresh(currentQuery?.ifBlank { null })
            }
        }
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            productGridAdapter.submitList(products)

            val query = binding.etSearch.text?.toString()
            binding.tvResultCount.text = if (!query.isNullOrBlank()) {
                "${products.size} result(s) for \"$query\""
            } else {
                "${products.size} products available"
            }

            binding.layoutEmpty.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
            binding.rvProducts.visibility = if (products.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.tvResultCount.text = "Searching..."
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}