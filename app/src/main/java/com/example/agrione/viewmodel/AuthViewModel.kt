package com.example.agrione.viewmodel

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.example.agrione.model.AuthRepository

class AuthViewModel : ViewModel() {

    var name: String? = null
    var mobNo: String? = null
    var email: String? = null
    var city: String? = null
    var password: String? = null
    var confPassword: String? = null
    var userType: String? = "normal"
    var authListener: AuthListener? = null

    // Login fields
    var loginmail: String? = null
    var loginpwd: String? = null

    lateinit var authRepository: AuthRepository
    lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    val userPosts = arrayListOf<String>()

    fun signupButtonClicked(view: View) {
        authListener?.onStarted()

        if (name.isNullOrEmpty() || mobNo.isNullOrEmpty() || mobNo!!.length != 10 ||
            password.isNullOrEmpty() || confPassword.isNullOrEmpty() || city.isNullOrEmpty()
        ) {
            authListener?.onFailure("Please fill all details correctly.")
            return
        }

        val data = hashMapOf(
            "name" to name,
            "mobNo" to mobNo,
            "email" to email,
            "city" to city,
            "userType" to userType,
            "posts" to userPosts,
            "profileImage" to ""
        )

        val authRepo = AuthRepository().signInWithEmail(email!!, password!!, data)
        authListener?.onSuccess(authRepo)
    }

    fun returnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        authListener?.onStarted()

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    val dataMap = hashMapOf(
                        "userType" to userType,
                        "posts" to userPosts,
                        "name" to account.displayName.toString(),
                        "profileImage" to account.photoUrl.toString()
                    )
                    authRepository = AuthRepository()
                    val result = authRepository.signInToGoogle(
                        account.idToken!!,
                        account.email.toString(),
                        dataMap
                    )
                    Log.d("AuthViewModel", result.value.toString())
                    authListener?.onSuccess(result)
                } catch (e: ApiException) {
                    authListener?.onFailure(e.message.toString())
                }
            }
        }
    }

    fun loginButtonClicked(view: View) {
        authListener?.onStarted()

        if (loginmail.isNullOrEmpty() || loginpwd.isNullOrEmpty()) {
            authListener?.onFailure("Login credentials are missing.")
            return
        }

        val authRepo = AuthRepository().logInWithEmail(loginmail!!, loginpwd!!)
        authListener?.onSuccess(authRepo)
    }
}
