package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var marker: Marker? = null

    companion object {
        private const val EMPTY = ""
        private const val DEFAULT = "Default"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        binding.btnSave.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        marker?.let { poiMarker ->
            viewModel.apply {
                val poi =
                    PointOfInterest(poiMarker.position, poiMarker.id, poiMarker.title ?: EMPTY)
                Log.d("HaiNM18", "${poiMarker.position}, ${poiMarker.id}, ${poiMarker.title}")
                selectedPOI.value = poi
                reminderSelectedLocationStr.value = poi.name
                latitude.value = poi.latLng.latitude
                longitude.value = poi.latLng.longitude
            }
            findNavController().popBackStack()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.normal_map -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            R.id.hybrid_map -> {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            R.id.satellite_map -> {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
            R.id.terrain_map -> {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
            else -> map.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val latitude = 21.02828772973878
        val longitude = 105.83565846916703
        val zoomIn = 15f

        val myHome = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myHome, zoomIn))

        setPoiClick(map)
        setMapClick(map)
        setMapStyle(map)
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            binding.btnSave.apply {
                isEnabled = true
                alpha = 1f
            }
            marker?.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            marker?.showInfoWindow()
        }
    }

    private fun setMapClick(map: GoogleMap) {
        map.setOnMapClickListener { poi ->
            binding.btnSave.apply {
                isEnabled = true
                alpha = 1f
            }
            marker?.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(poi)
                    .title(DEFAULT)
            )
            marker?.showInfoWindow()
        }
    }
}
