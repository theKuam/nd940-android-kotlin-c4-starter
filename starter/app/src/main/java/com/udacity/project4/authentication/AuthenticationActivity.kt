package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.common.AuthenticationViewModel
import com.udacity.project4.base.BaseActivity
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity


class AuthenticationActivity :
    BaseActivity<ActivityAuthenticationBinding>(R.layout.activity_authentication) {

    companion object {
        private val TAG = AuthenticationActivity::class.simpleName
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        this.onSignInResult(result)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult?) {
        val response = result?.idpResponse
        if (result?.resultCode == Activity.RESULT_OK) {
            Log.i(
                TAG,
                "Successfully signed in user " +
                        "${FirebaseAuth.getInstance().currentUser?.displayName}!"
            )
        } else {
            Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            authenticationViewModel.signInFailed()
        }
    }

    override fun initObserver() {
        authenticationViewModel.authenticationState.observe(this) { authenticationState ->
            when(authenticationState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    val mainIntent = Intent(this, RemindersActivity::class.java)
                    this.startActivity(mainIntent)
                    finish()
                }
                AuthenticationViewModel.AuthenticationState.INVALID_AUTHENTICATION -> {
                    Toast.makeText(applicationContext, "Sign in failed", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    override fun initAction() {
        binding.btnLogin.setOnClickListener {
            launchSignInFlow()
        }
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        signInLauncher.launch(signInIntent)

    }

    override fun initView() {}

}