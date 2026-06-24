package com.example.seapedia.ui.guest

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.seapedia.R
import com.example.seapedia.databinding.FragmentGuestAccountBinding
import com.example.seapedia.databinding.FragmentGuestHomeBinding
import com.example.seapedia.ui.auth.AuthActivity


class GuestAccountFragment : Fragment() {
    private var _binding: FragmentGuestAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuestAccountBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }

        binding.btnCreateAccount.setOnClickListener {
            val intent = Intent(requireContext(), AuthActivity::class.java).apply {
                putExtra(AuthActivity.EXTRA_START_DESTINATION, AuthActivity.DESTINATION_REGISTER)
            }
            startActivity(intent)
        }
    }
}