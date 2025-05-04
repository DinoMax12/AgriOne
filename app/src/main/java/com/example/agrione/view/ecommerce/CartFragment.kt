package com.example.agrione.view.ecommerce

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.agrione.PrePaymentFragment
import com.example.agrione.R
import com.example.agrione.adapter.CartItemsAdapter
import com.example.agrione.utilities.CartItemBuy
import com.example.agrione.viewmodel.EcommViewModel
import com.example.agrione.databinding.FragmentCartBinding
import kotlin.collections.HashMap

class CartFragment : Fragment(), CartItemBuy {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null
    var isOpened: Boolean = false
    var totalCount = 0
    var totalPrice = 0
    var items = HashMap<String, Any>()
    lateinit var ecommViewModel: EcommViewModel

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
    ): View? {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()
        val cartRef =
            firebaseDatabase.getReference("${firebaseAuth.currentUser!!.uid}").child("cart")

        (activity as AppCompatActivity).supportActionBar?.title = "Cart"
        isOpened = true

        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    items = dataSnapshot.value as HashMap<String, Any>

                    var totalCartPrice = 0
                    for ((key, value) in items) {
                        val currVal = value as Map<String, Any>
                        Log.d("Total Items", key.toString())
                        Log.d("Total Items", value.toString())
                        ecommViewModel.getSpecificItem("${key}")
                            .observe(viewLifecycleOwner, Observer {
                                totalCartPrice += currVal.get("quantity").toString()
                                    .toInt() * it.get("price").toString().toInt() + it.get("delCharge").toString().toInt()
                                Log.d("Total Price", currVal.get("quantity").toString())
                                Log.d("Total Price", it.get("price").toString())
                                Log.d("Total Price - 2", (currVal.get("quantity").toString().toInt()*it.get("price").toString().toInt()).toString())
                                binding.totalItemsValue.text = items.size.toString()
                                binding.totalCostValue.text = "\u20B9" + totalCartPrice.toString()
                            })
                        Log.d("Total Price - 3", key.toString())
                    }

                    if (isOpened == true) {
                        binding.totalItemsValue.text = items.size.toString()
                        binding.totalCostValue.text = "\u20B9" + totalCartPrice.toString()
                    }

                    val adapter =
                        CartItemsAdapter(this@CartFragment, items, this@CartFragment)
                    binding.recyclerCart.adapter = adapter
                    binding.recyclerCart.layoutManager = LinearLayoutManager(activity!!.applicationContext)
                    binding.progressCart.visibility = View.GONE
                    binding.loadingTitleText.visibility = View.GONE

                } else {
                    Toast.makeText(
                        activity!!.applicationContext,
                        "Item Not Exist",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressCart.visibility = View.GONE
                    binding.loadingTitleText.visibility = View.GONE
                }
            }
        }

        cartRef.addValueEventListener(postListener)

        binding.buyAllBtn.setOnClickListener {
            // Placeholder for payment fragment code
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
}
