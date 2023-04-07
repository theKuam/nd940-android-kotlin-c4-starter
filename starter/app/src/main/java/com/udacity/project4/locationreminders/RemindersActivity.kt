package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseActivity
import com.udacity.project4.common.AuthenticationViewModel
import com.udacity.project4.databinding.ActivityRemindersBinding

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : BaseActivity<ActivityRemindersBinding>(R.layout.activity_reminders) {

    companion object {
        private val TAG = RemindersActivity::class.simpleName
        private const val LOCATION_INTERVAL = 10000L
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val COARSE_LOCATION_PERMISSION_INDEX = 1
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 0
    }

    private val runningOnQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    override fun initObserver() {
        authenticationViewModel.authenticationState.observe(this) { authenticationState ->
            when(authenticationState) {
                AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    val authenticationIntent = Intent(this, AuthenticationActivity::class.java)
                    this.startActivity(authenticationIntent)
                    finish()
                }
                AuthenticationViewModel.AuthenticationState.INVALID_AUTHENTICATION -> {
                    Toast.makeText(applicationContext, "Log out failed", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    override fun initAction() {}

    override fun initView() {}

    override fun onStart() {
        super.onStart()
        checkPermissionsAndStartGeofencing()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                navHostFragment.navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            grantResults[COARSE_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                    && grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED)
        ) {
            Snackbar.make(
                binding.activityMapsReminders,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", applicationContext.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_LOW_POWER,
            LOCATION_INTERVAL
        ).build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        this@RemindersActivity,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.activityMapsReminders,
                    R.string.location_required_error,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
    }

    private fun checkPermissionsAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        val foreGroundPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION
        var resultCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        ActivityCompat.requestPermissions(
            this@RemindersActivity,
            arrayOf(foreGroundPermission, coarseLocationPermission),
            resultCode
        )
        if (runningOnQOrLater) {
            val backgroundPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION
            resultCode = REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            ActivityCompat.requestPermissions(
                this@RemindersActivity,
                arrayOf(backgroundPermission),
                resultCode
            )
        }
        Log.d(TAG, "Requests for location permission")
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                )

        val backgroundLocationApproved =
            if (runningOnQOrLater) {
                (
                        PackageManager.PERMISSION_GRANTED ==
                                ActivityCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                )
                        )
            } else true
        return foregroundLocationApproved && backgroundLocationApproved
    }
}
