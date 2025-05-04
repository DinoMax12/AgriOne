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
    var totalPrice = 0
    var itemCost: Int? = null
    var quantity: Int? = null
    var deliveryCost: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRazorPayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        postId = UUID.randomUUID()

        firebaseAuth = FirebaseAuth.getInstance()
        productId = intent.getStringExtra("productId")
        itemCost = intent.getStringExtra("itemCost")!!.toString().toInt()
        quantity = intent.getStringExtra("quantity")!!.toString().toInt()
        deliveryCost = intent.getStringExtra("deliveryCost")!!.toString().toInt()

        // Set up radio group listener
        binding.addressTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            addressType = when (checkedId) {
                R.id.homerd -> "Home"
                R.id.officerd -> "Office"
                R.id.otherrd -> "Other"
                else -> "Home"
            }
        }

        binding.orderNowBtn.setOnClickListener {
            name = binding.fullNamePrePay.text.toString()
            locality = binding.localityPrePay.text.toString()
            city = binding.cityPrePay.text.toString()
            state = binding.statePrePay.text.toString()
            pincode = binding.pincodePrePay.text.toString()
            mobile = binding.mobileNumberPrePay.text.toString()
            if (name.isNullOrEmpty() ||
                locality.isNullOrEmpty() ||
                city.isNullOrEmpty() ||
                state.isNullOrEmpty() ||
                pincode.isNullOrEmpty() ||
                mobile.isNullOrEmpty()
            ) {
                Toast.makeText(this, "Please Add all Fields", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Done", Toast.LENGTH_LONG).show()
                startPayment()
            }
        }

        binding.netValue.text = "Net Value: ₹ ${(itemCost!! * quantity!! + deliveryCost!!)}"
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

            totalPrice = itemCost!! * quantity!! + deliveryCost!!
            options.put("amount", "${totalPrice * 100}")

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
                    itemCost!!,
                    quantity!!,
                    deliveryCost!!,
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
