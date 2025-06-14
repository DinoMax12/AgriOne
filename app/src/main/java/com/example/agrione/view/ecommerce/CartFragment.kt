package com.example.agrione.view.ecommerce

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.agrione.PrePaymentFragment
import com.example.agrione.R
import com.example.agrione.adapter.CartItemsAdapter
import com.example.agrione.databinding.FragmentCartBinding
import com.example.agrione.model.data.CartItem
import com.example.agrione.utilities.CartItemBuy
import com.example.agrione.viewmodel.EcommViewModel
import kotlin.collections.HashMap

class CartFragment : Fragment(), CartItemBuy {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private var param1: String? = null
    private var param2: String? = null
    var isOpened: Boolean = false
    var items = HashMap<String, Any>()
    lateinit var ecommViewModel: EcommViewModel
    private var cartListener: ValueEventListener? = null

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CartFragment().apply {
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
        ecommViewModel = EcommViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove the listener when the fragment is destroyed
        cartListener?.let { listener ->
            FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .child("cart")
                .removeEventListener(listener)
        }
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.let { binding ->
            binding.progressCart.visibility = View.VISIBLE
            binding.loadingTitleText.visibility = View.VISIBLE

            val cartRef = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .child("cart")

            cartListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return // Check if fragment is still attached
                    
                    items.clear()
                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(CartItem::class.java)
                        item?.let {
                            val itemWithId = item.copy(id = item.id.ifEmpty { itemSnapshot.key ?: "" })
                            items[itemWithId.id] = itemWithId
                        }
                    }
                    updateTotalAmount()
                }

                override fun onCancelled(error: DatabaseError) {
                    if (_binding == null) return // Check if fragment is still attached
                    
                    Toast.makeText(context, "Failed to load cart items: ${error.message}", Toast.LENGTH_SHORT).show()
                    binding.progressCart.visibility = View.GONE
                }
            }

            cartRef.addValueEventListener(cartListener!!)

            binding.buyAllBtn.setOnClickListener {
                if (items.isEmpty()) {
                    Toast.makeText(context, "Your cart is empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                binding.progressCart.visibility = View.VISIBLE
                binding.loadingTitleText.visibility = View.VISIBLE
                binding.loadingTitleText.text = "Calculating total..."

                var processedItems = 0
                var totalItemCost = 0.0
                var totalDeliveryCost = 0.0
                var totalQuantity = 0
                var productIds = ""
                var finalTotalAmount = 0.0  // New variable to store final total

                for ((key, value) in items) {
                    val cartItem = value as CartItem
                    ecommViewModel.getSpecificItem(key).observe(viewLifecycleOwner, Observer { item ->
                        if (_binding == null) return@Observer // Check if fragment is still attached
                        
                        val itemCost = item["price"].toString().toDoubleOrNull() ?: 0.0
                        val deliveryCharge = item["delCharge"].toString().toDoubleOrNull() ?: 0.0

                        totalItemCost += itemCost * cartItem.quantity
                        totalDeliveryCost += deliveryCharge
                        totalQuantity += cartItem.quantity
                        productIds += "$key,"

                        processedItems++

                        if (processedItems == items.size) {
                            binding.progressCart.visibility = View.GONE
                            binding.loadingTitleText.visibility = View.GONE

                            // Store the final total amount
                            finalTotalAmount = totalItemCost + totalDeliveryCost

                            Intent(requireActivity().applicationContext, RazorPayActivity::class.java).also {
                                it.putExtra("productId", productIds.trimEnd(','))
                                it.putExtra("itemCost", finalTotalAmount.toString()) // Pass the final total
                                it.putExtra("quantity", totalQuantity.toString())
                                it.putExtra("deliveryCost", "0") // Set to 0 since total includes delivery
                                startActivity(it)
                            }
                        }
                    })
                }
            }
        }
    }

    override fun addToOrders(productId: String, quantity: Int, itemCost: Int, deliveryCost: Int) {
        Intent(requireActivity().applicationContext, RazorPayActivity::class.java).also {
            it.putExtra("productId", productId)
            it.putExtra("itemCost", itemCost.toString())
            it.putExtra("quantity", quantity.toString())
            it.putExtra("deliveryCost", deliveryCost.toString())
            startActivity(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cart_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.admin_verification -> {
                showAdminVerificationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAdminVerificationDialog() {
        try {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_admin_verification)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val adminCodeInput = dialog.findViewById<TextInputEditText>(R.id.adminCodeInput)
            val verifyButton = dialog.findViewById<Button>(R.id.verifyButton)
            val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)

            verifyButton.setOnClickListener {
                val adminCode = adminCodeInput.text.toString()
                if (adminCode == "1264") {
                    dialog.dismiss()
                    startAddProductActivity()
                } else {
                    Toast.makeText(context, "Invalid admin code", Toast.LENGTH_SHORT).show()
                }
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e("CartFragment", "Error showing admin dialog: ${e.message}")
            Toast.makeText(context, "Error showing admin dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAddProductActivity() {
        val intent = Intent(requireContext(), AddProductActivity::class.java)
        startActivity(intent)
    }

    private fun updateTotalAmount() {
        if (_binding == null) return // Check if fragment is still attached
        
        if (items.isEmpty()) {
            binding.totalItemsValue.text = "0"
            binding.totalCostValue.text = "₹0.00"
            binding.progressCart.visibility = View.GONE
            binding.loadingTitleText.visibility = View.GONE
            return
        }

        val adapter = CartItemsAdapter(this@CartFragment, items, this@CartFragment)
        adapter.setTotalUpdateListener(object : CartItemsAdapter.TotalUpdateListener {
            override fun onTotalUpdated(totals: Map<String, Double>) {
                if (_binding == null) return // Check if fragment is still attached
                
                val totalAmount = totals.values.sum()
                binding.totalItemsValue.text = items.size.toString()
                binding.totalCostValue.text = "₹" + String.format("%.2f", totalAmount)
                binding.progressCart.visibility = View.GONE
                binding.loadingTitleText.visibility = View.GONE
            }
        })
        
        binding.recyclerCart.adapter = adapter
        binding.recyclerCart.layoutManager = LinearLayoutManager(requireContext())
    }
}
