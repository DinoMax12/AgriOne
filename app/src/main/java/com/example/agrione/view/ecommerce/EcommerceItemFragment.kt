package com.example.agrione.view.ecommerce

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.common.base.MoreObjects
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.example.agrione.R
import com.example.agrione.adapter.AttributesNormalAdapter
import com.example.agrione.adapter.AttributesSelectionAdapter
import com.example.agrione.adapter.ImageSliderAdapter
import com.example.agrione.model.data.CartItem
import com.example.agrione.utilities.CellClickListener
import com.example.agrione.viewmodel.EcommViewModel
import com.example.agrione.databinding.FragmentEcommerceItemBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EcommItemFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EcommerceItemFragment : Fragment(), CellClickListener {
    private var _binding: FragmentEcommerceItemBinding? = null
    private val binding get() = _binding!!

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private lateinit var viewmodel: EcommViewModel
    private var param2: String? = null
    private var selectionAttribute = mutableMapOf<String, Any>()
    private var currentItemId: Any? = null
    lateinit var realtimeDatabase: FirebaseDatabase
    lateinit var firebaseAuth: FirebaseAuth
    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        viewmodel = ViewModelProvider(requireActivity())
            .get(EcommViewModel::class.java)
        Toast.makeText(requireActivity().applicationContext, "Something" + tag, Toast.LENGTH_SHORT).show()

        realtimeDatabase = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEcommerceItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EcommItemFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EcommerceItemFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        (activity as AppCompatActivity).supportActionBar?.title = "E-Commerce"
        binding.loadingText.text = "Loading..."

        val color1Params = binding.color1.layoutParams
        val color2Params = binding.color2.layoutParams
        val color3Params = binding.color3.layoutParams
        val color4Params = binding.color4.layoutParams

        val density = resources.displayMetrics.density
        color1Params.width = (density * 40).toInt()
        color1Params.height = (density * 35).toInt()
        binding.color1.layoutParams = color1Params

        binding.color1.setOnClickListener {
            color1Params.width = (density * 40).toInt()
            color1Params.height = (density * 35).toInt()
            binding.color1.layoutParams = color1Params

            color3Params.width = (density * 30).toInt()
            color3Params.height = (density * 25).toInt()
            binding.color3.layoutParams = color3Params

            color4Params.width = (density * 30).toInt()
            color4Params.height = (density * 25).toInt()
            binding.color4.layoutParams = color4Params

            color2Params.width = (density * 30).toInt()
            color2Params.height = (density * 25).toInt()
            binding.color2.layoutParams = color2Params
        }

        binding.color2.setOnClickListener {
            color1Params.width = (density * 30).toInt()
            color1Params.height = (density * 25).toInt()
            binding.color1.layoutParams = color1Params

            color3Params.width = (density * 30).toInt()
            color3Params.height = (density * 25).toInt()
            binding.color3.layoutParams = color3Params

            color4Params.width = (density * 30).toInt()
            color4Params.height = (density * 25).toInt()
            binding.color4.layoutParams = color4Params

            color2Params.width = (density * 40).toInt()
            color2Params.height = (density * 35).toInt()
            binding.color2.layoutParams = color2Params
        }

        binding.increaseQtyBtn.setOnClickListener {
            binding.quantityCountEcomm.text = (binding.quantityCountEcomm.text.toString().toInt() + 1).toString()
        }

        binding.decreaseQtyBtn.setOnClickListener {
            if (binding.quantityCountEcomm.text.toString().toInt() != 1) {
                binding.quantityCountEcomm.text = (binding.quantityCountEcomm.text.toString().toInt() - 1).toString()
            }
        }

        // Add rating change listener
        binding.Rating.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                updateProductRating(rating)
            }
        }

        val allData = viewmodel.ecommLiveData.value
        val allDataLength = allData!!.size

        for (a in 0 until allDataLength) {
            if (allData[a].id == this.tag) {
                val specificData = allData[a]
                currentItemId = specificData.id!!

                binding.productTitle.text = specificData.getString("title")
                binding.productShortDescription.text = specificData.getString("shortDesc")
                val price = when (val priceValue = specificData.get("price")) {
                    is String -> priceValue.toDoubleOrNull() ?: 0.0
                    is Double -> priceValue
                    is Number -> priceValue.toDouble()
                    else -> 0.0
                }
                binding.productPrice.text = "₹$price"
                binding.productLongDesc.text = specificData.getString("longDesc")
                binding.howToUseText.text = specificData.getString("howtouse")
                binding.deliverycost.text = specificData.getString("delCharge")
                var attributes = specificData.get("attributes") as Map<String, Any>

                if (attributes.contains("Color")) {
                    binding.colorLinear.visibility = View.VISIBLE
                    binding.colorTitle.visibility = View.VISIBLE
                } else {
                    binding.colorLinear.visibility = View.GONE
                    binding.colorTitle.visibility = View.GONE
                }

                var allSelectionAttributes = mutableListOf<Map<String, Any>>()
                var allNormalAttributes = mutableListOf<Map<String, Any>>()
                for ((key, value) in attributes) {
                    if (value is ArrayList<*> && key.toString() != "Color") {
                        allSelectionAttributes.add(mapOf(key to value))
                    }
                    if (value is String) {
                        allNormalAttributes.add(mapOf(key to value))
                    }
                }

                val selectionAdapter = AttributesSelectionAdapter(requireActivity().applicationContext, allSelectionAttributes, this)
                binding.recyclerSelectionAttributes.adapter = selectionAdapter
                binding.recyclerSelectionAttributes.layoutManager = LinearLayoutManager(requireActivity().applicationContext)

                val normalAdapter = AttributesNormalAdapter(allNormalAttributes)
                binding.recyclerNormalAttributes.adapter = normalAdapter
                binding.recyclerNormalAttributes.layoutManager = LinearLayoutManager(requireActivity().applicationContext)

                binding.progressEcommItem.visibility = View.GONE
                binding.loadingText.visibility = View.GONE

                // Handle imageUrl that could be either String or List<String>
                val imageUrls = when (val imageUrlValue = specificData.get("imageUrl")) {
                    is String -> listOf(imageUrlValue)
                    is List<*> -> imageUrlValue.map { it.toString() }
                    else -> listOf()
                }
                val imageSliderAdapter = ImageSliderAdapter(imageUrls)
                binding.posterSlider.adapter = imageSliderAdapter
                binding.posterSlider.orientation = ViewPager2.ORIENTATION_HORIZONTAL

                // Set initial rating
                val currentRating = specificData.get("rating")?.toString()?.toFloatOrNull() ?: 0f
                binding.Rating.rating = currentRating
                binding.Rating.isEnabled = true  // Enable user interaction
            }
        }

        binding.addToCart.setOnClickListener {
            binding.addToCart.isClickable = false
            binding.progressEcommItem.visibility = View.VISIBLE
            binding.loadingText.text = "Adding to Cart..."
            binding.loadingText.visibility = View.GONE
            val currentDateTime = sdf.format(Date())
            // Create a reference to the user's cart
            val cartRef = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .child("cart")
                .child(currentItemId.toString())

            cartRef.setValue(CartItem(
                quantity = binding.quantityCountEcomm.text.toString().toInt(),
                time = currentDateTime
            ))
                .addOnCompleteListener {
                    Toast.makeText(requireActivity().applicationContext, "Item Added", Toast.LENGTH_SHORT).show()
                    binding.progressEcommItem.visibility = View.GONE
                    binding.loadingText.visibility = View.GONE
                    binding.addToCart.isClickable = true
                }.addOnFailureListener {
                    Toast.makeText(requireActivity().applicationContext, "Please Try Again!", Toast.LENGTH_SHORT).show()
                    binding.progressEcommItem.visibility = View.GONE
                    binding.loadingText.visibility = View.GONE
                    binding.addToCart.isClickable = true
                }
        }

        binding.buynow.setOnClickListener {
            val productPrice = binding.productPrice.text.toString().split("₹") as ArrayList<String>

            Intent(requireActivity().applicationContext, RazorPayActivity::class.java).also {
                it.putExtra("productId", currentItemId.toString())
                it.putExtra("itemCost", productPrice[1].toString())
                it.putExtra("quantity", binding.quantityCountEcomm.text.toString())
                it.putExtra("deliveryCost", binding.deliverycost.text.toString())
                startActivity(it)
            }
        }
    }

    private fun updateProductRating(newRating: Float) {
        if (currentItemId == null) return

        // Show loading state
        binding.progressEcommItem.visibility = View.VISIBLE
        binding.loadingText.text = "Updating rating..."
        binding.loadingText.visibility = View.VISIBLE

        // Get reference to the product in Firestore
        val productRef = FirebaseFirestore.getInstance().collection("products").document(currentItemId.toString())

        // Get current rating data
        productRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val currentRating = document.get("rating")?.toString()?.toFloatOrNull() ?: 0f
                val totalRatings = document.get("totalRatings")?.toString()?.toIntOrNull() ?: 0
                
                // Calculate new average rating
                val newTotalRatings = totalRatings + 1
                val newAverageRating = ((currentRating * totalRatings) + newRating) / newTotalRatings

                // Update the product document
                val updates = hashMapOf<String, Any>(
                    "rating" to newAverageRating,
                    "totalRatings" to newTotalRatings
                )

                productRef.update(updates)
                    .addOnSuccessListener {
                        // Update successful
                        binding.Rating.rating = newAverageRating
                        Toast.makeText(requireContext(), "Rating updated successfully!", Toast.LENGTH_SHORT).show()
                        binding.progressEcommItem.visibility = View.GONE
                        binding.loadingText.visibility = View.GONE
                    }
                    .addOnFailureListener { e ->
                        // Update failed
                        Toast.makeText(requireContext(), "Failed to update rating: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.progressEcommItem.visibility = View.GONE
                        binding.loadingText.visibility = View.GONE
                    }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.progressEcommItem.visibility = View.GONE
            binding.loadingText.visibility = View.GONE
        }
    }

    override fun onCellClickListener(name: String) {
        val selectionAttributeAllData = name.split(" ") as List<Any>

        Log.d("EcommItem", selectionAttributeAllData[0].toString())
        Log.d("EcommItem", selectionAttributeAllData[1].toString())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.cart_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cart_item -> {
                val cartFragment = CartFragment()
                val transaction = requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, cartFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("cart")
                    .commit()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
