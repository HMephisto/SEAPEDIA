package com.example.seapedia.ui.guest

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seapedia.R
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.GuestRepository
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.FragmentGuestHomeBinding
import com.example.seapedia.ui.auth.AuthActivity
import com.example.seapedia.ui.guest.adapter.ProductAdapter
import com.example.seapedia.ui.guest.adapter.ReviewAdapter
import com.example.seapedia.ui.product.ProductDetailActivity


class GuestHomeFragment : Fragment() {
    private var _binding: FragmentGuestHomeBinding? = null
    private val binding get() = _binding!!

    private val productAdapter = ProductAdapter()
    private val reviewAdapter = ReviewAdapter()
    private lateinit var loadingDialog: LoadingDialog

    private val viewModel: GuestHomeViewModel by viewModels {
        val sessionManager = SessionManager(requireContext())
        val apiService = ApiClient.create { sessionManager.getToken() }
        val repository = GuestRepository(apiService)
        GuestHomeViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuestHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        setupRecyclerViews()
        setupClickListeners()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        binding.rvProducts.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
        }

        binding.rvReviews.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
        }

        productAdapter.onItemClick = { product ->
            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
            startActivity(intent)
        }
    }

    private fun setupClickListeners() {
        val isLoggedIn = SessionManager(requireContext()).isLoggedIn()

        binding.tvSeeAll.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigationView
            ).selectedItemId = if (isLoggedIn) {
                R.id.buyerExploreFragment
            } else {
                R.id.guestExploreFragment
            }
        }

        if (isLoggedIn) {
            binding.btnSubmitReview.setOnClickListener {
                val rating = binding.ratingBarInput.rating.toInt()
                val comment = binding.etComment.text.toString().trim()
                viewModel.submitReview(rating, comment)
            }
        } else {
            binding.ratingBarInput.setIsIndicator(true)
            binding.etComment.isEnabled = false

            binding.btnSubmitReview.text = "Login to Submit a Review"
            binding.btnSubmitReview.setOnClickListener {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.black)
            )
            setOnRefreshListener {
                viewModel.refresh()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)
        }

        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            reviewAdapter.submitList(reviews)
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }

        viewModel.reviewSubmitState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ReviewSubmitState.Idle -> { }
                is ReviewSubmitState.Loading -> {
                    loadingDialog.show("Submitting review...")
                }
                is ReviewSubmitState.Success -> {
                    loadingDialog.dismiss()
                    binding.etComment.text?.clear()
                    binding.ratingBarInput.rating = 0f
                    Toast.makeText(requireContext(), "Review submitted!", Toast.LENGTH_SHORT).show()
                    viewModel.resetReviewState()
                }
                is ReviewSubmitState.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetReviewState()
                }
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