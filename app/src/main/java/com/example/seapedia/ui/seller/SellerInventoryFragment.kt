package com.example.seapedia.ui.seller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.seapedia.databinding.FragmentGuestHomeBinding
import com.example.seapedia.databinding.FragmentSellerDashboardBinding
import com.example.seapedia.databinding.FragmentSellerInventoryBinding


class SellerInventoryFragment : Fragment() {
    private var _binding: FragmentSellerInventoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}