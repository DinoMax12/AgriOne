package com.example.agrione.view.auth

import android.content.Intent
import android.os.Bundle
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
import com.example.agrione.databinding.ActivitySignupBinding
import com.example.agrione.view.dashboard.DashboardActivity
import com.example.agrione.viewmodel.AuthListener
import com.example.agrione.viewmodel.AuthViewModel

class SignupActivity : AppCompatActivity(), AuthListener {

    lateinit var googleSignInClient: GoogleSignInClient
    val firebaseAuth = FirebaseAuth.getInstance()
    lateinit var viewModel: AuthViewModel
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding initialization
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup)

        // Initialize ViewModel and set the AuthListener
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        binding.authViewModel = viewModel
        viewModel.authListener = this

        // Set up click listeners for the buttons
        binding.loginRedirectTextSignup.setOnClickListener {
            Intent(this, LoginActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.signGoogleBtnSignup.setOnClickListener {
            signIn()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.returnActivityResult(requestCode, resultCode, data)
    }

    fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onStarted() {
        // Use the ViewBinding to show progress
        binding.progressSignup.visibility = android.view.View.VISIBLE
    }

    override fun onSuccess(authRepo: LiveData<String>) {
        authRepo.observe(this, Observer {
            binding.progressSignup.visibility = android.view.View.GONE
            if (it.toString() == "Success") {
                toast("Account Created")
                Intent(this, DashboardActivity::class.java).also {
                    startActivity(it)
                }
            }
        })
    }

    override fun onFailure(message: String) {
        binding.progressSignup.visibility = android.view.View.GONE
        toast("Failure")
    }

    // Custom toast extension function
    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
