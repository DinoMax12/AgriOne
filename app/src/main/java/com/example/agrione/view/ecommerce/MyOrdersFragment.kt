package com.example.agrione.view.ecommerce

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.example.agrione.R
import com.example.agrione.adapter.MyOrdersAdapter
import com.example.agrione.utilities.CartItemBuy
import com.example.agrione.utilities.CellClickListener
import kotlin.collections.HashMap
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

// TODO: Rename parameter arguments, choose names that match
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyOrdersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyOrdersFragment : Fragment(), CellClickListener, CartItemBuy {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var myOrderRecycler: RecyclerView
    private lateinit var noOrdersLayout: ConstraintLayout
    private lateinit var exploreNowButton: MaterialButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private var orders = HashMap<String, Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_orders, container, false)
        myOrderRecycler = view.findViewById(R.id.myOrderRecycler)
        noOrdersLayout = view.findViewById(R.id.noOrdersLayout)
        exploreNowButton = view.findViewById(R.id.exploreNowButton)
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyOrdersFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyOrdersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Setup RecyclerView
        myOrderRecycler.layoutManager = LinearLayoutManager(context)

        // Setup explore now button
        exploreNowButton.setOnClickListener {
            // Navigate to e-commerce section
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, EcommerceFragment())
                .addToBackStack(null)
                .commit()
        }

        // Load orders
        loadOrders()
    }

    private fun loadOrders() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val orderRef = firebaseDatabase.getReference("${userId}/orders")

        orderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    orders = snapshot.value as HashMap<String, Any>
                    val myOrdersAdapter = MyOrdersAdapter(this@MyOrdersFragment, orders, this@MyOrdersFragment, this@MyOrdersFragment)
                    myOrderRecycler.adapter = myOrdersAdapter
                    myOrderRecycler.visibility = View.VISIBLE
                    noOrdersLayout.visibility = View.GONE
                } else {
                    myOrderRecycler.visibility = View.GONE
                    noOrdersLayout.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error loading orders: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCellClickListener(name: String) {
        val ecommerceItemFragment = EcommerceItemFragment()
        val transaction = requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, ecommerceItemFragment, name)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .setReorderingAllowed(true)
            .addToBackStack("ecommItem")
            .commit()
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
