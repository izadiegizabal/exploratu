package xyz.izadi.exploratu.currencies.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.text.Text
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.ocr_capture.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.izadi.exploratu.R
import xyz.izadi.exploratu.currencies.CurrenciesListDialogFragment
import xyz.izadi.exploratu.currencies.camera.source.CameraSource
import xyz.izadi.exploratu.currencies.camera.source.CameraSourcePreview
import xyz.izadi.exploratu.currencies.camera.ui.GraphicOverlay
import xyz.izadi.exploratu.currencies.camera.ui.OcrGraphic
import xyz.izadi.exploratu.currencies.data.RatesDatabase
import xyz.izadi.exploratu.currencies.data.api.ApiFactory
import xyz.izadi.exploratu.currencies.data.models.Currencies
import xyz.izadi.exploratu.currencies.data.models.Rates
import xyz.izadi.exploratu.currencies.others.Utils.getCurrencies
import xyz.izadi.exploratu.currencies.others.Utils.getCurrencyCodeFromDeviceLocale
import xyz.izadi.exploratu.currencies.others.Utils.getDetectedCurrency
import xyz.izadi.exploratu.currencies.others.Utils.isInternetAvailable
import java.io.IOException
import java.security.Policy

/**
 * Activity for the Ocr Detecting app.  This app detects text and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 */
class OcrCaptureActivity : AppCompatActivity(), CurrenciesListDialogFragment.Listener {
    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay<OcrGraphic>? = null

    // Helper objects for detecting taps and pinches.
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null

    // Helper variables to check the state of the activity
    private val LOG_TAG = this.javaClass.simpleName
    private var ratesDB: RatesDatabase? = null
    private var currencies: Currencies? = null
    private var currencyRates: Rates? = null
    private var activeCurrencyIndex = 0
    private var selectingCurrencyIndex = -1
    private val activeCurCodes = ArrayList<String>()

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.ocr_capture)

        preview = findViewById(R.id.preview)
        graphicOverlay = findViewById(R.id.graphicOverlay)

        // Set good defaults for capturing text.
        val autoFocus = true
        val useFlash = false

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash)
        } else {
            requestCameraPermission()
        }

        gestureDetector = GestureDetector(this, CaptureGestureListener())
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        cameraFab.setOnClickListener {
            if (preview!!.isActive) {
                onPause()
            } else {
                startCameraSource()
            }
        }

        // Initialise conversion data
        ratesDB = RatesDatabase.getInstance(applicationContext)

        currencies = getCurrencies(applicationContext)
        setPreferredCurrencies()
        updateRates()
        setUpCurrencySelectorListeners()
        setUpNetworkChangeListener()
    }

    private fun setUpCurrencySelectorListeners() {
        ll_currency_from.setOnClickListener {
            selectingCurrencyIndex = 0
            CurrenciesListDialogFragment.newInstance(currencies)
                .show(supportFragmentManager, "dialog")
        }
        ll_currency_to.setOnClickListener {
            selectingCurrencyIndex = 1
            CurrenciesListDialogFragment.newInstance(currencies)
                .show(supportFragmentManager, "dialog")
        }
    }

    private fun setPreferredCurrencies() {
        // read preferences to load
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val fromKey = "currency_code_from"
        val toKey = "currency_code_to"
        val defaultFromCurrencyCode = "EUR"
        val defaultToCurrencyCode = "USD"

        if (!sharedPref.contains(fromKey)) {
            val fromCode = getDetectedCurrency(applicationContext)
            activeCurCodes.add(fromCode ?: defaultFromCurrencyCode)
        } else {
            activeCurCodes.add(sharedPref.getString(fromKey, defaultFromCurrencyCode) ?: return)
        }

        if (!sharedPref.contains(toKey)) {
            activeCurCodes.add(getCurrencyCodeFromDeviceLocale())
        } else {
            activeCurCodes.add(sharedPref.getString(toKey, defaultToCurrencyCode) ?: return)
        }

        // load them
        loadCurrencyTo(activeCurCodes[0], 0)
        loadCurrencyTo(activeCurCodes[1], 1)
    }

    private fun loadCurrencyTo(code: String, listPos: Int) {
        // change global variable
        activeCurCodes[listPos] = code

        // update tv
        val curr = currencies?.getCurrency(code)
        val flagPath = "file:///android_asset/flags/${curr?.code}.png"
        val transformation = RoundedCornersTransformation(32, 0)
        when (listPos) {
            0 -> {
                Picasso
                    .get()
                    .load(flagPath)
                    .placeholder(R.drawable.ic_dollar_placeholder)
                    .transform(transformation)
                    .into(iv_currency_from_flag)
                tv_currency_from_code.text = curr?.code

                val sharedPref = getPreferences(Context.MODE_PRIVATE)
                getPreferences(Context.MODE_PRIVATE) ?: return
                with(sharedPref.edit()) {
                    putString("currency_code_from", code)
                    apply()
                }
            }
            1 -> {
                Picasso
                    .get()
                    .load(flagPath)
                    .placeholder(R.drawable.ic_dollar_placeholder)
                    .transform(transformation)
                    .into(iv_currency_to_flag)
                tv_currency_to_code.text = curr?.code

                val sharedPref = getPreferences(Context.MODE_PRIVATE)
                getPreferences(Context.MODE_PRIVATE) ?: return
                with(sharedPref.edit()) {
                    putString("currency_code_to", code)
                    apply()
                }
            }
        }

        calculateConversions()
    }

    override fun onCurrencyClicked(code: String) {
        loadCurrencyTo(code, selectingCurrencyIndex)
        calculateConversions()
    }

    private fun setUpNetworkChangeListener() {
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    updateRates()
                }

                override fun onLost(network: Network?) {
                    //take action when network connection is lost
                }
            })
    }

    private fun calculateConversions() {
        // Get conversions rates an calculate rates
        if (activeCurrencyIndex != -1) {
            makeConversions()
        }
    }

    private fun makeConversions() {
        if (currencyRates != null) {
            val rates = currencyRates
            val from = activeCurCodes[activeCurrencyIndex]
            val rateIndex = rates?.convertFloat(1f, from, activeCurCodes[1])
            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            getPreferences(Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putFloat("currency_conversion_rate_AR", rateIndex!!)
                apply()
            }
        }
    }

    private fun updateRates() {
        GlobalScope.launch {
            // If there are no conversion rates or if they are older than today
            if (currencyRates == null || !DateUtils.isToday(currencyRates?.date?.time!!)) {
                // get the latest from db
                val latestRatesFromDB = ratesDB?.ratesDao()?.getLatestRates()
                // if there isn't any on db or if they are older than today
                if (latestRatesFromDB == null || !DateUtils.isToday(latestRatesFromDB.date.time)) {
                    // check for internet
                    if (isInternetAvailable(applicationContext)) {
                        // Try to fetch from the API
                        val response = ApiFactory.exchangeRatesAPI.getLatestRates()
                        withContext(Dispatchers.Main) {
                            try {
                                if (response.isSuccessful) {
                                    currencyRates = response.body()
                                    currencyRates?.rates?.resetEur()
                                    ratesDB?.ratesDao()?.insertRates(response.body()!!)
                                    runOnUiThread {
                                        makeConversions()
                                    }
                                } else {
                                    Log.d(
                                        LOG_TAG,
                                        "Error while getting new data: ${response.code()}"
                                    )
                                    // DB fallback in case of error, no connection...
                                    if (latestRatesFromDB == null) {
                                        saveRatesFallbackInDB()
                                    } else {
                                        currencyRates = latestRatesFromDB
                                    }
                                    runOnUiThread {
                                        makeConversions()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        // DB fallback in case of no connection...
                        if (latestRatesFromDB == null) {
                            saveRatesFallbackInDB()
                        } else {
                            currencyRates = latestRatesFromDB
                        }
                        runOnUiThread {
                            makeConversions()
                        }
                    }
                } else {
                    // if they are from today we just use them
                    currencyRates = latestRatesFromDB
                    runOnUiThread {
                        makeConversions()
                    }
                }
            }
        }
    }

    fun locateFromCurrency(view: View?) {
        val fromCode = getDetectedCurrency(applicationContext)
        activeCurCodes[0] = fromCode ?: activeCurCodes[0]
        loadCurrencyTo(activeCurCodes[0], 0)
    }

    fun turnOnOffFlash(view: View?) {
        if (cameraSource?.flashMode == Camera.Parameters.FLASH_MODE_OFF) {
            cameraSource?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            ib_flash_toggle.setImageResource(R.drawable.ic_flash_on)
        } else {
            cameraSource?.flashMode = Camera.Parameters.FLASH_MODE_OFF
            ib_flash_toggle.setImageResource(R.drawable.ic_flash_off)
        }
        Log.d(LOG_TAG, "The flash mode is: ${cameraSource?.flashMode}")
    }

    fun reverseCurrencies(view: View?) {
        val aux = activeCurCodes[0]
        activeCurCodes[0] = activeCurCodes[1]
        activeCurCodes[1] = aux

        loadCurrencyTo(activeCurCodes[0], 0)
        loadCurrencyTo(activeCurCodes[1], 1)
    }

    private suspend fun insertRateInDB(rates: Rates) {
        rates.rates.resetEur()
        ratesDB?.ratesDao()?.insertRates(rates)
    }

    private suspend fun saveRatesFallbackInDB() {
        currencyRates = currencies?.getRates() //fallback from JSON
        insertRateInDB(currencyRates!!)
    }
//////////////////////////////////////////////////////////////////////////////////////////
//    CAMERA STUFF ///////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private fun requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission")

        val permissions = arrayOf(Manifest.permission.CAMERA)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }

        val thisActivity = this

        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(
                thisActivity, permissions,
                RC_HANDLE_CAMERA_PERM
            )
        }

        Snackbar.make(
            graphicOverlay!!, "Access to the camera is needed for detection",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.ok, listener)
            .show()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val b = scaleGestureDetector!!.onTouchEvent(e)

        val c = gestureDetector!!.onTouchEvent(e)

        return b || c || super.onTouchEvent(e)
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     *
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private fun createCameraSource(autoFocus: Boolean, useFlash: Boolean) {
        val context = applicationContext

        // A text recognizer is created to find text.  An associated multi-processor instance
        // is set to receive the text recognition results, track the text, and maintain
        // graphics for each text block on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each text block.
        val textRecognizer = TextRecognizer.Builder(context).build()

        // Load bitmap that will be shown alongside the price
        val inputStream = assets.open("priceTag.png")
        val icon: Bitmap = BitmapFactory.decodeStream(inputStream)

        textRecognizer.setProcessor(
            OcrDetectorProcessor(
                graphicOverlay,
                icon,
                getPreferences(Context.MODE_PRIVATE)
            )
        )

        if (!textRecognizer.isOperational) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.")

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            val lowstorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = registerReceiver(null, lowstorageFilter) != null

            if (hasLowStorage) {
                Toast.makeText(this, "Not a lot of space", Toast.LENGTH_LONG).show()
                Log.w(TAG, "Not a lot of space")
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1280, 1024)
            .setRequestedFps(2.0f)
            .setFlashMode(if (useFlash) Camera.Parameters.FLASH_MODE_TORCH else null)
            .setFocusMode(if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO else null)
            .build()
    }

    /**
     * Restarts the camera.
     */
    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    /**
     * Stops the camera.
     */
    override fun onPause() {
        super.onPause()
        if (preview != null) {
            preview!!.stop()
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    override fun onDestroy() {
        super.onDestroy()
        if (preview != null) {
            preview!!.release()
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {link #requestPermissions(String[], int)}.
     *
     *
     * **Note:** It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     *
     *
     * @param requestCode  The request code passed in {link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [PackageManager.PERMISSION_GRANTED]
     * or [PackageManager.PERMISSION_DENIED]. Never null.
     * see #requestPermissions(String[], int)
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: $requestCode")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source")
            // we have permission, so create the camerasource
            val autoFocus = intent.getBooleanExtra(AutoFocus, true)
            val useFlash = intent.getBooleanExtra(UseFlash, false)
            createCameraSource(autoFocus, useFlash)
            return
        }

        Log.e(
            TAG, "Permission not granted: results len = " + grantResults.size +
                    " Result code = " + if (grantResults.isNotEmpty()) grantResults[0] else "(empty)"
        )

        val listener = DialogInterface.OnClickListener { dialog, id -> finish() }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Multitracker sample")
            .setMessage("No camera permission")
            .setPositiveButton("Ok", listener)
            .show()
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    @Throws(SecurityException::class)
    private fun startCameraSource() {
        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }

        if (cameraSource != null) {
            try {
                preview!!.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource!!.release()
                cameraSource = null
            }

        }
    }

    /**
     * onTap is called to speak the tapped TextBlock, if any, out loud.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the tap was on a TextBlock
     */
    private fun onTap(rawX: Float, rawY: Float): Boolean {
        val graphic = graphicOverlay!!.getGraphicAtLocation(rawX, rawY)
        var text: Text? = null
        if (graphic != null) {
            text = graphic.text
            // Text detected, do something
        } else {
            Log.d(TAG, "no text detected")
        }
        return text != null
    }

    private inner class CaptureGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return onTap(e.rawX, e.rawY) || super.onSingleTapConfirmed(e)
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return false
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         *
         *
         * Once a scale has ended, [ScaleGestureDetector.getFocusX]
         * and [ScaleGestureDetector.getFocusY] will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         */
        override fun onScaleEnd(detector: ScaleGestureDetector) {
            if (cameraSource != null) {
                cameraSource!!.doZoom(detector.scaleFactor)
            }
        }
    }

    companion object {
        private const val TAG = "OcrCaptureActivity"

        // Intent request code to handle updating play services if needed.
        private const val RC_HANDLE_GMS = 9001

        // Permission request codes need to be < 256
        private const val RC_HANDLE_CAMERA_PERM = 2

        // Constants used to pass extra data in the intent
        const val AutoFocus = "AutoFocus"
        const val UseFlash = "UseFlash"
        const val TextBlockObject = "String"
    }
}
