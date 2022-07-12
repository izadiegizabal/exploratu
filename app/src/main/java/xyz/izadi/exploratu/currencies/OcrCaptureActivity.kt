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
import android.util.Log
import android.view.ScaleGestureDetector
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import xyz.izadi.exploratu.MainActivity
import xyz.izadi.exploratu.R
import xyz.izadi.exploratu.currencies.camera.OcrAnalyzer
import xyz.izadi.exploratu.currencies.camera.OcrGraphic
import xyz.izadi.exploratu.currencies.data.models.Currencies
import xyz.izadi.exploratu.currencies.data.models.Rates
import xyz.izadi.exploratu.currencies.others.Utils
import xyz.izadi.exploratu.currencies.others.loadFlag
import xyz.izadi.exploratu.databinding.OcrCaptureBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA

@AndroidEntryPoint
class OcrCaptureActivity :
    AppCompatActivity(),
    CurrenciesListDialogFragment.Listener {

    private lateinit var binding: OcrCaptureBinding

    private val vm by viewModels<CurrenciesViewModel>()

    private var camera: Camera? = null
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    // Helper variables to check the state of the activity
    private val mTAG = this.javaClass.simpleName
    private var currencyRates: Rates? = null
    private var currencies: Currencies? = null
    private var activeCurrencyIndex = 0
    private var selectingCurrencyIndex = -1
    private val activeCurCodes = mutableListOf<String>()
    private lateinit var sharedPref: SharedPreferences
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    // Control camera state
    private var isPreviewPaused = false
    private lateinit var isFlashOn: LiveData<Int>

    // Initializes the UI and creates the detector pipeline.
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        binding = OcrCaptureBinding.inflate(layoutInflater)
        sharedPref = getPreferences(Context.MODE_PRIVATE)

        // Initialize our background executor
        cameraExecutor = Executors.newFixedThreadPool(4)

        binding.apply {
            requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    previewView.post { binding.startCamera() }
                } else {
                    // TODO: manage better denied permission
                }
            }

            when {
                allPermissionsGranted() -> binding.previewView.post { binding.startCamera() }
                else -> requestPermissionLauncher.launch(REQUIRED_PERMISSION)
            }

            cameraFab.setOnClickListener {
                startPreviewPause(!isPreviewPaused)
            }

            setPreferredCurrencies()
            setUpOptionsListeners()
            setUpCurrencySelectorListeners()
            setUpNetworkChangeListener()
            setUpToolTips()

            showWarnModalIfRequired()

            vm.rates.onEach {
                currencyRates = it
                makeConversions()
            }.launchIn(lifecycleScope)

            vm.currencies.onEach {
                currencies = it
                setPreferredCurrencies()
            }.launchIn(lifecycleScope)
        }

        setContentView(binding.root)
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

        val actionButtonListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                (DialogInterface.BUTTON_POSITIVE) -> {
                    with(sharedPref.edit()) {
                        putBoolean(hideArWarnModalPrefKey, true)
                        apply()
                    }
                }
            }
        }

        MaterialAlertDialogBuilder(this@OcrCaptureActivity)
            .setTitle(getString(R.string.ar_warning_title))
            .setMessage(getString(R.string.ar_warning_message))
            .setPositiveButton(getString(R.string.ar_warning_hide_next_time), actionButtonListener)
            .setNegativeButton(getString(R.string.ar_warning_dismiss), actionButtonListener)
            .show()
    }

    private fun OcrCaptureBinding.setUpToolTips() {
        TooltipCompat.setTooltipText(ibGoToList, getString(R.string.tt_go_to_list))
        TooltipCompat.setTooltipText(ibFlashToggle, getString(R.string.tt_flash_toggle))
        TooltipCompat.setTooltipText(
            ibLocateFromCurrency,
            getString(R.string.tt_locate_from_currency)
        )
        TooltipCompat.setTooltipText(
            ibReverseCurrencies,
            getString(R.string.tt_reverse_currencies)
        )
        TooltipCompat.setTooltipText(llCurrencyFrom, getString(R.string.tt_currency_from))
        TooltipCompat.setTooltipText(llCurrencyTo, getString(R.string.tt_currency_to))
        TooltipCompat.setTooltipText(cameraFab, getString(R.string.tt_camera_fab))
    }

    private fun OcrCaptureBinding.setUpOptionsListeners() {
        ibReverseCurrencies.setOnClickListener { reverseCurrencies() }
        ibLocateFromCurrency.setOnClickListener { locateFromCurrency() }
        ibFlashToggle.setOnClickListener { turnOnOffFlash() }
        ibGoToList.setOnClickListener { goToListView() }
    }

    private fun OcrCaptureBinding.setUpCurrencySelectorListeners() {
        fun onClick(index: Int) {
            selectingCurrencyIndex = index
            CurrenciesListDialogFragment.newInstance(currencies)
                .show(supportFragmentManager, "dialog")
        }

        llCurrencyFrom.setOnClickListener {
            onClick(0)
        }
        llCurrencyTo.setOnClickListener {
            onClick(1)
        }
    }

    private fun OcrCaptureBinding.setPreferredCurrencies() {
        // read preferences to load
        val fromKey = "currency_code_from"
        val toKey = "currency_code_to"
        val defaultFromCurrencyCode = vm.detectedCurrency ?: "EUR"
        val defaultToCurrencyCode = vm.localeCurrency ?: "USD"

        (sharedPref.getString(fromKey, defaultFromCurrencyCode) ?: defaultFromCurrencyCode).let {
            activeCurCodes.add(it)
            loadCurrencyTo(it, 0)
        }

        (sharedPref.getString(toKey, defaultToCurrencyCode) ?: defaultToCurrencyCode).let {
            activeCurCodes.add(it)
            loadCurrencyTo(it, 1)
        }
    }

    private fun OcrCaptureBinding.loadCurrencyTo(code: String, listPos: Int) {
        // change global variable
        activeCurCodes[listPos] = code

        // key values
        val currencySymbolKey = "currency_to_symbol"

        // update tv
        val curr = currencies?.getCurrency(code) ?: return
        val currSign = curr.sign.split("/").getOrNull(0)
        when (listPos) {
            0 -> {
                ivCurrencyFromFlag.loadFlag(curr)
                tvCurrencyFromCode.text = code

                getPreferences(Context.MODE_PRIVATE) ?: return
                with(sharedPref.edit()) {
                    putString("currency_code_from", code)
                    apply()
                }
            }
            1 -> {
                ivCurrencyToFlag.loadFlag(curr)
                tvCurrencyToCode.text = code

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
        binding.loadCurrencyTo(code, selectingCurrencyIndex)
        calculateConversions()
    }

    private fun setUpNetworkChangeListener() {
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    lazy { vm.syncRates() }
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
            val rateIndex = rates?.convertFloat(1f, from, activeCurCodes[1]) ?: 1f
            getPreferences(Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putFloat("currency_conversion_rate_AR", rateIndex)
                apply()
            }
        }
    }

    private fun locateFromCurrency() {
        val fromCode = vm.detectedCurrency
        activeCurCodes[0] = fromCode ?: activeCurCodes[0]
        binding.loadCurrencyTo(activeCurCodes[0], 0)
    }

    private fun reverseCurrencies() {
        val aux = activeCurCodes[0]
        activeCurCodes[0] = activeCurCodes[1]
        activeCurCodes[1] = aux

        binding.loadCurrencyTo(activeCurCodes[0], 0)
        binding.loadCurrencyTo(activeCurCodes[1], 1)
    }

    private fun goToListView() {
        vm.showAd(this) {
            if (isTaskRoot) {
                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                }
            } else {
                finish()
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //    CAMERA STUFF    ////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    private fun OcrCaptureBinding.startCamera() {
        // Bind the CameraProvider to the LifeCycleOwner
        val cameraSelector = CameraSelector.Builder().build()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this@OcrCaptureActivity)
        cameraProviderFuture.addListener({

            // CameraProvider
            val cameraProvider = cameraProviderFuture.get().also {
                // Must unbind the use-cases before rebinding them
                it.unbindAll()
            }

            // Preview
            val preview = Preview.Builder().build()

            // Attach the previewView's surface provider to preview use case
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // Build the image analysis use case and instantiate our analyzer
            val isDarkMode = Utils.isDarkTheme(this@OcrCaptureActivity)
            val icon = BitmapFactory.decodeStream(
                if (isDarkMode) {
                    assets.open("priceTag_material_dark.png")
                } else {
                    assets.open("priceTag_material.png")
                }
            )
            graphicOverlay.apply {
                setCameraFacing(CameraSelector.LENS_FACING_BACK)
                clear()
            }
            // ImageAnalysis
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().apply {
                    setAnalyzer(
                        cameraExecutor,
                        OcrAnalyzer(applicationContext, graphicOverlay) { price, bufferSize ->
                            run {
                                val graphic =
                                    OcrGraphic(
                                        graphicOverlay,
                                        price.boundingBox,
                                        price.amount,
                                        icon,
                                        sharedPref,
                                        isDarkMode,
                                        bufferSize
                                    )
                                // Toast.makeText(applicationContext, word.text, Toast.LENGTH_SHORT).show()
                                graphicOverlay.add(graphic)
                            }
                        })
                }

            try {
                // A variable number of use-cases can be passed here -
                // camera provides access to CameraControl & CameraInfo
                camera = cameraProvider.bindToLifecycle(
                    this@OcrCaptureActivity,
                    cameraSelector,
                    imageAnalyzer,
                    preview
                )

                setUpPinchToZoom()

                // set up live data for camera info
                isFlashOn = camera?.cameraInfo?.torchState ?: liveData { TorchState.OFF }
                isPreviewPaused = false
            } catch (exc: Exception) {
                Log.e(mTAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this@OcrCaptureActivity))
    }

    override fun onDestroy() {
        super.onDestroy()

        // Shut down our background executor
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()

        vm.apply {
            syncRates()
            loadAd()
        }

        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun OcrCaptureBinding.setUpPinchToZoom() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio: Float = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
                val delta = detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(this@OcrCaptureActivity, listener)

        previewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    private fun OcrCaptureBinding.startPreviewPause(pauseIt: Boolean) {
        if (pauseIt) {
            isPreviewPaused = true
            previewView.bitmap?.let {
                showPausedPreview(it)
            }
            cameraProviderFuture.get().unbindAll()
            cameraFab.setImageResource(R.drawable.ic_play_arrow)
        } else {
            hidePausedPreview()
            startCamera()
            cameraFab.setImageResource(R.drawable.ic_twotone_pause)
        }
    }

    private fun OcrCaptureBinding.showPausedPreview(bitmap: Bitmap) {
        previewView.visibility = View.GONE
        pausedPreviewView.load(bitmap)
        pausedPreviewView.visibility = View.VISIBLE
    }

    private fun OcrCaptureBinding.hidePausedPreview() {
        previewView.visibility = View.VISIBLE
        pausedPreviewView.visibility = View.GONE
    }

    private fun OcrCaptureBinding.turnOnOffFlash() {
        if (isFlashOn.value == 0) {
            camera?.cameraControl?.enableTorch(true)
            ibFlashToggle.setImageResource(R.drawable.ic_flash_on)
        } else {
            camera?.cameraControl?.enableTorch(false)
            ibFlashToggle.setImageResource(R.drawable.ic_flash_off)
        }
    }

    // Check if all permission specified in the manifest have been granted
    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            baseContext,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

}
