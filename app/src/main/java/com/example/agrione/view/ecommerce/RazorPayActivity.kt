package com.example.agrione.view.ecommerce

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.agrione.R
import com.example.agrione.databinding.ActivityRazorPayBinding
import com.example.agrione.model.data.Orders
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject

class RazorPayActivity : AppCompatActivity(), PaymentResultListener {
    private lateinit var binding: ActivityRazorPayBinding
    
    lateinit var firebaseAuth: FirebaseAuth
    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")

    var postId: UUID? = null
    var name: String = ""
    var locality: String = ""
    var city: String = ""
    var state: String = ""
    var pincode: String = ""
    var mobile: String = ""
    var addressType: String = "Home" // Default address type
    var currentDate = sdf.format(Date())
    lateinit var realtimeDatabase: FirebaseDatabase
    var productId: String? = null
    var totalPrice = 0.0
    var itemCost: Double? = null
    var quantity: Int? = null
    var deliveryCost: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRazorPayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = UUID.randomUUID()

        firebaseAuth = FirebaseAuth.getInstance()
        productId = intent.getStringExtra("productId")
        itemCost = intent.getStringExtra("itemCost")?.toDoubleOrNull() ?: 0.0
        quantity = intent.getStringExtra("quantity")?.toIntOrNull() ?: 0
        deliveryCost = intent.getStringExtra("deliveryCost")?.toDoubleOrNull() ?: 0.0

        // If deliveryCost is 0, it means we're using "Buy All" and itemCost already includes delivery
        // Otherwise, we need to add delivery cost for single product purchase
        totalPrice = if (deliveryCost == 0.0) {
            itemCost ?: 0.0  // For Buy All, use the total directly
        } else {
            (itemCost ?: 0.0) + (deliveryCost ?: 0.0)  // For single product, add delivery cost
        }

        // Set up radio group listener with proper validation
        binding.addressTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.homerd -> {
                    addressType = "Home"
                    binding.homerd.isChecked = true
                }
                R.id.officerd -> {
                    addressType = "Office"
                    binding.officerd.isChecked = true
                }
                R.id.otherrd -> {
                    addressType = "Other"
                    binding.otherrd.isChecked = true
                }
            }
        }

        // Set default selection
        binding.homerd.isChecked = true
        addressType = "Home"

        binding.netValue.text = "₹ ${String.format("%.2f", totalPrice)}"

        binding.payNowBtn.setOnClickListener {
            if (validateInputs()) {
                startPayment()
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (binding.fullNamePrePay.text.toString().isEmpty()) {
            binding.fullNamePrePay.error = "Please enter your name"
            return false
        }
        if (binding.localityPrePay.text.toString().isEmpty()) {
            binding.localityPrePay.error = "Please enter your locality"
            return false
        }
        if (binding.cityPrePay.text.toString().isEmpty()) {
            binding.cityPrePay.error = "Please enter your city"
            return false
        }
        if (binding.statePrePay.text.toString().isEmpty()) {
            binding.statePrePay.error = "Please enter your state"
            return false
        }
        if (binding.pincodePrePay.text.toString().isEmpty() || binding.pincodePrePay.text.toString().length != 6) {
            binding.pincodePrePay.error = "Please enter a valid 6-digit pincode"
            return false
        }
        if (binding.mobileNumberPrePay.text.toString().isEmpty() || binding.mobileNumberPrePay.text.toString().length != 10) {
            binding.mobileNumberPrePay.error = "Please enter a valid 10-digit mobile number"
            return false
        }
        return true
    }

    private fun startPayment() {
        val activity: Activity = this
        val co = Checkout()

        try {
            val options = JSONObject()
            options.put("name", "Agrione")
            options.put("description", "Demoing Charges")
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("currency", "INR")

            // Convert total price to paise (multiply by 100)
            options.put("amount", "${(totalPrice * 100).toInt()}")

            val prefill = JSONObject()
            prefill.put("email", "${firebaseAuth.currentUser!!.email}")
            prefill.put("contact", "${mobile}")

            options.put("prefill", prefill)
            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        try {
            Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("failed", "Exception in onPaymentSuccess", e)
        }
    }

    override fun onPaymentSuccess(p0: String?) {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            realtimeDatabase = FirebaseDatabase.getInstance()

            var orderRef = realtimeDatabase.getReference("${firebaseAuth.currentUser!!.uid}").child("orders")
                .child("${postId}")
            val currDate = System.currentTimeMillis()

            var date1 = Date()
            val calendar = Calendar.getInstance()
            calendar.time = date1

            var randomDay = (0..12).shuffled().take(1) as List<Int>
            val sdf = SimpleDateFormat("dd/MM/yyyy")

            calendar.add(Calendar.DATE, randomDay[0])
            date1 = calendar.time

            orderRef.setValue(
                Orders(
                    name!!,
                    locality!!,
                    city!!,
                    state!!,
                    pincode!!,
                    mobile!!,
                    currentDate,
                    productId!!,
                    itemCost!!.toInt(),
                    quantity!!,
                    deliveryCost!!.toInt(),
                    "Arriving By: " + sdf.format(date1).toString()
                )
            ).addOnCompleteListener {
                Toast.makeText(this, "Payment Successful", Toast.LENGTH_LONG).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show()
                Toast.makeText(this, "Please Try Again", Toast.LENGTH_LONG).show()
                finish()
            }
        } catch (e: Exception) {
            Log.e("success", "Exception in onPaymentSuccess", e)
        }
    }
}
