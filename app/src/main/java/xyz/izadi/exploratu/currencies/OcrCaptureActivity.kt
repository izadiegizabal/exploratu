package xyz.izadi.exploratu.currencies

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.ScaleGestureDetector
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.util.concurrent.ListenableFuture
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.ocr_capture.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.izadi.exploratu.MainActivity
import xyz.izadi.exploratu.R
import xyz.izadi.exploratu.currencies.camera.OcrAnalyzer
import xyz.izadi.exploratu.currencies.camera.ui.GraphicOverlay
import xyz.izadi.exploratu.currencies.camera.ui.OcrGraphic
import xyz.izadi.exploratu.currencies.data.RatesDatabase
import xyz.izadi.exploratu.currencies.data.api.ApiFactory
import xyz.izadi.exploratu.currencies.data.models.Currencies
import xyz.izadi.exploratu.currencies.data.models.Rates
import xyz.izadi.exploratu.currencies.others.Utils
import xyz.izadi.exploratu.currencies.others.Utils.getCurrencies
import xyz.izadi.exploratu.currencies.others.Utils.getCurrencyCodeFromDeviceLocale
import xyz.izadi.exploratu.currencies.others.Utils.getDetectedCurrency
import xyz.izadi.exploratu.currencies.others.Utils.isInternetAvailable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.
private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

private const val RATIO_4_3_VALUE = 4.0 / 3.0
private const val RATIO_16_9_VALUE = 16.0 / 9.0

class OcrCaptureActivity : AppCompatActivity(), CameraXConfig.Provider, CurrenciesListDialogFragment.Listener {
    private lateinit var viewFinder: PreviewView
    private var preview: Preview? = null
    private var graphicOverlay: GraphicOverlay<OcrGraphic>? = null
    private var camera: Camera? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var imageAnalyzer: ImageAnalysis? = null
    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    // Helper variables to check the state of the activity
    private val LOG_TAG = this.javaClass.simpleName
    private var ratesDB: RatesDatabase? = null
    private var currencies: Currencies? = null
    private var currencyRates: Rates? = null
    private var activeCurrencyIndex = 0
    private var selectingCurrencyIndex = -1
    private val activeCurCodes = ArrayList<String>()
    private lateinit var sharedPref: SharedPreferences

    // Control camera state
    private var isPreviewPaused = false
    private lateinit var isFlashOn: LiveData<Int>

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.ocr_capture)

        graphicOverlay = findViewById(R.id.graphicOverlay)
        viewFinder = findViewById(R.id.preview_view)
        sharedPref = getPreferences(Context.MODE_PRIVATE)

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        // Request camera permissions
        if (allPermissionsGranted()) {
            viewFinder.post {
                startCamera()
            }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraFab.setOnClickListener {
            if (!isPreviewPaused) {
                startPreviewPause(true)
            } else {
                startPreviewPause(false)
            }
        }

        // Initialise conversion data
        ratesDB = RatesDatabase.getInstance(applicationContext)

        currencies = getCurrencies(applicationContext)
        setPreferredCurrencies()
        updateRates()
        setUpOptionsListeners()
        setUpCurrencySelectorListeners()
        setUpNetworkChangeListener()
        setUpToolTips()

        showWarnModalIfRequired()
    }

    private fun showWarnModalIfRequired() {
        val hideArWarnModalPrefKey = "hideARWarnModal"

        if (!sharedPref.contains(hideArWarnModalPrefKey)) {
            with(sharedPref.edit()) {
                putBoolean(hideArWarnModalPrefKey, false)
                apply()
            }
        } else {
            if (sharedPref.getBoolean(hideArWarnModalPrefKey, false)) {
                // if pref is hide modal --> return
                return
            }
        }

        // TODO: show modal
        val actionButtonListener = DialogInterface.OnClickListener { dialog, which ->
            when(which) {
                (DialogInterface.BUTTON_POSITIVE) -> {
                    with(sharedPref.edit()) {
                        putBoolean(hideArWarnModalPrefKey, true)
                        apply()
                    }
                }
            }
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.ar_warning_title))
            .setMessage(getString(R.string.ar_warning_message))
            .setPositiveButton(getString(R.string.ar_warning_hide_next_time), actionButtonListener)
            .setNegativeButton(getString(R.string.ar_warning_dismiss), actionButtonListener)
            .show()
    }

    private fun setUpToolTips() {
        TooltipCompat.setTooltipText(ib_go_to_list, getString(R.string.tt_go_to_list))
        TooltipCompat.setTooltipText(ib_flash_toggle, getString(R.string.tt_flash_toggle))
        TooltipCompat.setTooltipText(
            ib_locate_from_currency,
            getString(R.string.tt_locate_from_currency)
        )
        TooltipCompat.setTooltipText(
            ib_reverse_currencies,
            getString(R.string.tt_reverse_currencies)
        )
        TooltipCompat.setTooltipText(ll_currency_from, getString(R.string.tt_currency_from))
        TooltipCompat.setTooltipText(ll_currency_to, getString(R.string.tt_currency_to))
        TooltipCompat.setTooltipText(cameraFab, getString(R.string.tt_camera_fab))
    }

    private fun setUpOptionsListeners() {
        ib_reverse_currencies.setOnClickListener {
            reverseCurrencies()
        }

        ib_locate_from_currency.setOnClickListener {
            locateFromCurrency()
        }

        ib_flash_toggle.setOnClickListener {
            turnOnOffFlash()
        }

        ib_go_to_list.setOnClickListener {
            goToListView(it)
        }
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
            activeCurCodes.add(getCurrencyCodeFromDeviceLocale() ?: defaultFromCurrencyCode)
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

        // key values
        val currencySymbolKey = "currency_to_symbol"

        // update tv
        val curr = currencies?.getCurrency(code)
        val currSign = curr?.sign?.split("/")!![0]
        val flagPath = "file:///android_asset/flags/${code}.png"
        val transformation = RoundedCornersTransformation(32, 0)
        when (listPos) {
            0 -> {
                Picasso
                    .get()
                    .load(flagPath)
                    .placeholder(R.drawable.ic_dollar_placeholder)
                    .transform(transformation)
                    .into(iv_currency_from_flag)
                tv_currency_from_code.text = code

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
                tv_currency_to_code.text = code

                getPreferences(Context.MODE_PRIVATE) ?: return
                with(sharedPref.edit()) {
                    putString("currency_code_to", code)
                    putString(currencySymbolKey, currSign)
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

    private fun locateFromCurrency() {
        val fromCode = getDetectedCurrency(applicationContext)
        activeCurCodes[0] = fromCode ?: activeCurCodes[0]
        loadCurrencyTo(activeCurCodes[0], 0)
    }

    private fun reverseCurrencies() {
        val aux = activeCurCodes[0]
        activeCurCodes[0] = activeCurCodes[1]
        activeCurCodes[1] = aux

        loadCurrencyTo(activeCurCodes[0], 0)
        loadCurrencyTo(activeCurCodes[1], 1)
    }

    private fun goToListView(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
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
    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }

    private fun startCamera() {
        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        Log.d(LOG_TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        Log.d(LOG_TAG, "Preview aspect ratio: $screenAspectRatio")

        val rotation = viewFinder.display.rotation

        // Bind the CameraProvider to the LifeCycleOwner
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {

            // CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                // We request aspect ratio but no resolution
                .setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation
                .setTargetRotation(rotation)
                .build()

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(viewFinder.createSurfaceProvider(null))

            // ImageAnalysis
            imageAnalyzer = ImageAnalysis.Builder()
                // We request aspect ratio but no resolution
                .setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                .setTargetRotation(rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Build the image analysis use case and instantiate our analyzer
            val isDarkMode = Utils.isDarkTheme(this)
            val inputStream = if (isDarkMode) {
                assets.open("priceTag_material_dark.png")
            } else {
                assets.open("priceTag_material.png")
            }
            val icon: Bitmap = BitmapFactory.decodeStream(inputStream)
            if (graphicOverlay != null) {
                val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
                Log.d("OCRCapture", "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")
                graphicOverlay!!.setCameraInfo(
                    metrics.widthPixels,
                    metrics.heightPixels,
                    CameraSelector.LENS_FACING_BACK
                )
                graphicOverlay!!.clear()
            }
            val analyzerUseCase = imageAnalyzer?.apply {
                setAnalyzer(cameraExecutor, OcrAnalyzer(
                    applicationContext,
                    graphicOverlay,
                    icon,
                    sharedPref,
                    isDarkMode)
                )
            }

            // Must unbind the use-cases before rebinding them
            cameraProvider.unbindAll()

            try {
                // A variable number of use-cases can be passed here -
                // camera provides access to CameraControl & CameraInfo
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, analyzerUseCase,  preview)

                setUpPinchToZoom()

                // set up livedata for camera info
                isFlashOn = camera?.cameraInfo?.torchState!!
                isPreviewPaused = false
            } catch(exc: Exception) {
                Log.e(LOG_TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    /**
     *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = width.coerceAtLeast(height).toDouble() / width.coerceAtMost(height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    override fun onDestroy() {
        super.onDestroy()

        // Shut down our background executor
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        if (isPreviewPaused){
            // TODO: maintain preview image after resuming
            startPreviewPause(false)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpPinchToZoom() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio: Float = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
                val delta = detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(this, listener)

        viewFinder.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    private fun startPreviewPause(pauseIt: Boolean) {
        if (pauseIt) {
            isPreviewPaused = true
            cameraProviderFuture.get().unbindAll()
            cameraFab.setImageResource(R.drawable.ic_play_arrow)
        } else {
            startCamera()
            cameraFab.setImageResource(R.drawable.ic_twotone_pause)
        }
    }

    private fun turnOnOffFlash() {
        if (isFlashOn.value == 0) {
            camera?.cameraControl?.enableTorch(true)
            ib_flash_toggle.setImageResource(R.drawable.ic_flash_on)
        } else {
            camera?.cameraControl?.enableTorch(false)
            ib_flash_toggle.setImageResource(R.drawable.ic_flash_off)
        }
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                // TODO: handle not having permission better
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

}
