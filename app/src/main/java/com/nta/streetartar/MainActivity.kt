package com.nta.streetartar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val sharedViewModel : SharedViewModel by viewModels ()

    private var mMap: GoogleMap? = null
    var locationManager: LocationManager? = null
    private val REQUEST_LOCATION_PERMISSION = 1
    var marker: Marker? = null
    var locationListener: LocationListener? = null

    private lateinit var rxPermissions: RxPermissions

    private lateinit var disposable: Disposable

    lateinit var fragment: ArFragment

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()

        rxPermissions = RxPermissions(this)
        askPermissions()

        setupObservers()
        initMap()

        val navController = Navigation.findNavController(this, R.id.fragment_container)

        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {
                R.id.arExampleFragment -> {
                    sceneform_fragment.view?.visibility = View.VISIBLE
                }
                else -> {
                    sceneform_fragment.view?.visibility = View.GONE
                }
            }
        }

    }

    private fun askPermissions(){
        disposable = rxPermissions.request(Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA)
            .subscribe { granted: Boolean ->
                if (!granted) {
                    Toast.makeText(this, "Sorry, Request Not Granted", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun initMap(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val latitude: Double = location.getLatitude()
                    val longitude: Double = location.getLongitude()

                    val geocoder = Geocoder(applicationContext)
                    try {
                        val addresses: List<Address> =
                            geocoder.getFromLocation(latitude, longitude, 1)
                        var result: String = addresses[0].getLocality().toString() + ":"
                        result += addresses[0].getCountryName()
                        val latLng = LatLng(latitude, longitude)
                        if (marker != null) {
                            marker!!.remove()
                            marker = mMap!!.addMarker(MarkerOptions().position(latLng).title(result))
                            //mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f))

                        } else {
                            marker = mMap?.addMarker(MarkerOptions().position(latLng).title(result))
                            mMap?.setMaxZoomPreference(20f)
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 21.0f))
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                override fun onStatusChanged(
                    provider: String,
                    status: Int,
                    extras: Bundle
                ) {
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            locationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0f,
                locationListener
            )
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )
        }
    }

    private fun addObject(parse: Uri) {
        val frame = fragment.arSceneView.arFrame
        val point = getScreenCenter()
        if (frame != null) {
            val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    placeObject(fragment, hit.createAnchor(), parse)
                    break
                }
            }
        }
    }

    private fun placeObject(fragment: ArFragment, createAnchor: Anchor, model: Uri) {
        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenAccept {
                addNodeToScene(fragment, createAnchor, it)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message)
                    .setTitle("error!")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }
    }

    private fun addNodeToScene(fragment: ArFragment, createAnchor: Anchor, renderable: ModelRenderable) {
        val anchorNode = AnchorNode(createAnchor)
        val rotatingNode = RotatingNode()

        val transformableNode = TransformableNode(fragment.transformationSystem)

        rotatingNode.renderable = renderable

        rotatingNode.addChild(transformableNode)
        rotatingNode.setParent(anchorNode)

        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }

    private fun getScreenCenter(): android.graphics.Point {
        val vw = findViewById<View>(android.R.id.content)
        return Point(vw.width / 2, vw.height / 2)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){

            locationListener = object : LocationListener {

                override fun onLocationChanged(location : Location) {
                    val latitude = location.getLatitude();
                    val longitude = location.getLongitude();

                    val geocoder = Geocoder(getApplicationContext());
                    try {
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        var result = addresses.get(0).getLocality()+":";
                        result += addresses.get(0).getCountryName();
                        val latLng = LatLng(latitude, longitude);
                        if (marker != null){
                            marker?.remove();
                            marker = mMap?.addMarker(MarkerOptions().position(latLng).title(result));
                            mMap?.setMaxZoomPreference(20f);
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
                        }
                        else{
                            marker = mMap?.addMarker(MarkerOptions().position(latLng).title(result));
                            mMap?.setMaxZoomPreference(20f);
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 21.0f));
                        }


                    } catch (e : IOException) {
                        e.printStackTrace();
                    }
                }

                override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onProviderEnabled(p0: String?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onProviderDisabled(p0: String?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }


            };
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener);
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener);
        }
    }



    private fun setupObservers(){
        sharedViewModel.startAuthLiveEvent.observe(this, Observer {
            if (it){
                signIn()
            }
        })

        sharedViewModel.addObjectLiveEvent.observe(this, Observer {
            if (it) {
                fragment =
                    supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment
                addObject(Uri.parse("Heart.sfb"))

//                ModelRenderable.builder()
//                    .setSource(this, Uri.parse("Heart.sfb"))
//                    .build()

            }
        })
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {

            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    sharedViewModel.uidMutableLiveData.value = auth.currentUser?.uid
                    sharedViewModel.successfulAuthLiveEvent.value = true
                } else {
                    Toast.makeText(baseContext, "Some error has occurred.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOut() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener(this) {
            // Todo update ui
        }
    }

    private fun revokeAccess() {
        auth.signOut()
        googleSignInClient.revokeAccess().addOnCompleteListener(this) {
            // Todo update ui
        }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
    }

    override fun onStop() {
        super.onStop()
        locationManager?.removeUpdates(locationListener);
    }
}
