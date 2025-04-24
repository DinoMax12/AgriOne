package com.project.agrione.view.ecommerce

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
import com.project.agrione.PrePaymentFragment
import com.project.agrione.R
import com.project.agrione.adapter.CartItemsAdapter
import com.project.agrione.utilities.CartItemBuy
import com.project.agrione.viewmodel.EcommViewModel
import kotlinx.android.synthetic.main.fragment_cart.*
import kotlin.collections.HashMap

class CartFragment : Fragment(), CartItemBuy {

    private var param1: String? = null
    private var param2: String? = null
    var isOpened: Boolean = false
    var totalCount = 0
    var totalPrice = 0
    var items = HashMap<String, Object>()
    lateinit var ecommViewModel: EcommViewModel

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
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CartFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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

                    items = dataSnapshot.value as HashMap<String, Object>

                    var totalCartPrice = 0
                    for ((key, value) in items) {

                        val currVal = value as Map<String, Object>
                        Log.d("Total Items", key.toString())
                        Log.d("Total Items", value.toString())
                        ecommViewModel.getSpecificItem("${key}")
                            .observe(viewLifecycleOwner, Observer {
                                totalCartPrice += currVal.get("quantity").toString()
                                    .toInt() * it.get("price").toString().toInt() + it.get("delCharge").toString().toInt()
                                Log.d("Total Price", currVal.get("quantity").toString())
                                Log.d("Total Price", it.get("price").toString())
                                Log.d("Total Price - 2", (currVal.get("quantity").toString().toInt()*it.get("price").toString().toInt()).toString())
                                totalItemsValue.text = items.size.toString()
                                totalCostValue.text = "\u20B9" + totalCartPrice.toString()
                            })
                        Log.d("Total Price - 3", key.toString())
                    }

                    if (isOpened == true) {
                        totalItemsValue.text = items.size.toString()
                        totalCostValue.text = "\u20B9" + totalCartPrice.toString()
                    }


                    val adapter =
                        CartItemsAdapter(this@CartFragment, items, this@CartFragment)
                    recyclerCart.adapter = adapter
                    recyclerCart.layoutManager = LinearLayoutManager(activity!!.applicationContext)
                    progress_cart.visibility = View.GONE
                    loadingTitleText.visibility = View.GONE

                } else {
                    Toast.makeText(
                        activity!!.applicationContext,
                        "Item Not Exist",
                        Toast.LENGTH_SHORT
                    ).show()
                    progress_cart.visibility = View.GONE
                    loadingTitleText.visibility = View.GONE
                }
            }
        }

        cartRef.addValueEventListener(postListener)

        buyAllBtn.setOnClickListener {
            // Placeholder for payment fragment code
        }
    }

    override fun addToOrders(productId: String, quantity: Int, itemCost: Int, deliveryCost: Int) {
        Intent(activity!!.applicationContext, RazorPayActivity::class.java).also {
            it.putExtra("productId", productId)
            it.putExtra("itemCost", itemCost.toString())
            it.putExtra("quantity", quantity.toString())
            it.putExtra("deliveryCost", deliveryCost.toString())
            startActivity(it)
        }
    }
}
