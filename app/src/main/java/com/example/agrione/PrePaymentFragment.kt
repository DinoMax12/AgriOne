package com.example.agrione

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.agrione.databinding.FragmentPrePaymentBinding
import com.example.agrione.view.ecommerce.RazorPayActivity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 * Use the [PrePaymentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PrePaymentFragment : Fragment() {
    private var _binding: FragmentPrePaymentBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PrePaymentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrePaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get the product details from arguments
        val productId = arguments?.getString("productId")
        val itemCost = arguments?.getString("itemCost")?.toIntOrNull()
        val quantity = arguments?.getString("quantity")?.toIntOrNull()
        val deliveryCost = arguments?.getString("deliveryCost")?.toIntOrNull()

        if (productId != null && itemCost != null && quantity != null && deliveryCost != null) {
            val intent = Intent(requireContext(), RazorPayActivity::class.java).apply {
                putExtra("productId", productId)
                putExtra("itemCost", itemCost.toString())
                putExtra("quantity", quantity.toString())
                putExtra("deliveryCost", deliveryCost.toString())
            }
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Missing product details", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
