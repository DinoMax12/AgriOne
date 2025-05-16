package com.example.agrione.view.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.example.agrione.R
import com.example.agrione.databinding.ActivityLoginBinding
import com.example.agrione.view.dashboard.DashboardActivity
import com.example.agrione.viewmodel.AuthListener
import com.example.agrione.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity(), AuthListener {

    // TEMPORARY TESTING MODE - Set to true to bypass authentication
    // TODO: Set this back to false when done testing
    private val TEMP_BYPASS_AUTH = false

    private lateinit var googleSignInClient: GoogleSignInClient
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var viewModel: AuthViewModel
    private lateinit var binding: ActivityLoginBinding
    private var isNavigating = false
    private var isFromDashboard = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TEMPORARY TESTING MODE - Bypass authentication
        if (TEMP_BYPASS_AUTH) {
            Log.d("LoginActivity", "TEMP_BYPASS_AUTH: Bypassing authentication")
            navigateToDashboard()
            return
        }

        // Check if we're coming from DashboardActivity
        isFromDashboard = intent.getBooleanExtra("from_dashboard", false)

        // Initialize View Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        binding.authViewModel = viewModel
        viewModel.authListener = this

        // Only check for logged in user if we're not coming from DashboardActivity
        // Add a flag to shared preferences to prevent loops
        val sharedPreferences = getSharedPreferences("AgrionePrefs", Context.MODE_PRIVATE)
        val isLoginAttempted = sharedPreferences.getBoolean("login_attempted", false)

        if (!isFromDashboard && firebaseAuth.currentUser != null && !isNavigating && !isLoginAttempted) {
            // Set flag to prevent loops
            sharedPreferences.edit().putBoolean("login_attempted", true).apply()
            Log.d("LoginActivity", "User already logged in, navigating to Dashboard.")
            navigateToDashboard()
            return
        }

        // Reset login_attempted flag if we're explicitly on the login screen
        if (isFromDashboard) {
            sharedPreferences.edit().putBoolean("login_attempted", false).apply()
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.createaccountText.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.signGoogleBtnLogin.setOnClickListener {
            signIn()
        }

        binding.forgotPasswdTextLogin.setOnClickListener {
            val userEmail = binding.emailEditLogin.text.toString()
            if (userEmail.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter your Email", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.sendPasswordResetEmail(userEmail)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Email Sent", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun navigateToDashboard() {
        if (!isNavigating) {
            isNavigating = true
            Log.d("LoginActivity", "Navigating to Dashboard.")
            val intent = Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            // Clear login_attempted flag when explicitly navigating
            getSharedPreferences("AgrionePrefs", Context.MODE_PRIVATE)
                .edit().putBoolean("login_attempted", false).apply()
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.returnActivityResult(requestCode, resultCode, data)
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onStarted() {
        binding.progressLogin.visibility = android.view.View.VISIBLE
    }

    override fun onSuccess(authRepo: LiveData<String>) {
        authRepo.observe(this, Observer {
            binding.progressLogin.visibility = android.view.View.GONE
            if (it.toString() == "Success") {
                Toast.makeText(this, "Logged In", Toast.LENGTH_LONG).show()
                navigateToDashboard()
            }
        })
    }

    override fun onFailure(message: String) {
        binding.progressLogin.visibility = android.view.View.GONE
        Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show()
    }
}
