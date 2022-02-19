package com.khoyac.map2

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat.LOG_TAG
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlin.random.Random

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {
    companion object {
        const val LOCATION_REQUEST_CODE = 0
    }

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val lat = 39.42283
    private val lon = -0.41542

    private var polylineOptions = PolylineOptions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createMapFragment()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                imprimirUbicacion(it)
            } else {
                Log.d("LOG_TAG", "No se pudo obtener la ubicación")
            }
        }
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                Log.d("LOG_TAG", "Se recibió una actualización")
                for (location in locationResult.locations) {
                    imprimirUbicacion(location)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


    fun imprimirUbicacion(ubicacion: Location) {
        Log.d("LOG_TAG", "Latitud es ${ubicacion.latitude} y la longitud es ${ubicacion.longitude}")
        if (ubicacion.latitude > (lat - 0.000040)
            && ubicacion.latitude < (lat + 0.000040)
            && ubicacion.longitude > (lon - 0.000040)
            && ubicacion.longitude < (lon + 0.000040)
        ) {
            Toast.makeText(this, "Has capturado el objetivo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun createMarker() {
        val favoritePlace = LatLng(lat, lon)
        map.addMarker(MarkerOptions()
                        .position(favoritePlace)
                        .title("Objetivo!")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag)))
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(favoritePlace, 18f),
            4000,
            null
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        //map.uiSettings.isRotateGesturesEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.cameraPosition.bearing
        createPolylines()
        createMarker()
        map.setOnMyLocationButtonClickListener(this)
        enableLocation()

        map.setOnMapLongClickListener {
            val markerOptions = MarkerOptions().position(it)
            createDynamicPolylines(markerOptions)
            map.addMarker(markerOptions)
            map.animateCamera(CameraUpdateFactory.newLatLng(it))
        }
    }

    private fun enableLocation() {
        if (!::map.isInitialized) return
        if (isPermissionsGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            LOCATION_REQUEST_CODE -> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                map.isMyLocationEnabled = true
            }else{
                Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Boton pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun createDynamicPolylines(markerOptions: MarkerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.flag))
            .anchor(Random.nextFloat(), Random.nextFloat())
            .title("Marcador " + Random.nextInt().toString())
            .flat(true)
        polylineOptions.add(LatLng(markerOptions.position.latitude, markerOptions.position.longitude))

        val polyline = map.addPolyline(polylineOptions)
    }

    private fun createPolylines(){
        val polylineOptions = PolylineOptions()
            .add(LatLng(39.42283,-0.41542))
            .add(LatLng( 39.42243, -0.41542))
            .add(LatLng( 39.42243, -0.41502))
            .add(LatLng( 39.42283, -0.41502))
        val polyline = map.addPolyline(polylineOptions)
    }


}