package vn.navitaximap

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.util.Locale
import vn.navitaximap.data.DemoOfflinePack
import org.maplibre.android.style.sources.VectorSource
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth

/**
 * Phase 1/2 proof of concept. No fake routes, traffic, speed limits or offline coverage.
 * Online street styles are provisional; independent offline map packs are not ready.
 */
class MainActivity : Activity(), LocationListener {
    private lateinit var mapView: MapView
    private var liveMap: MapLibreMap? = null
    private lateinit var locationText: TextView
    private lateinit var statusText: TextView
    private lateinit var titleBar: LinearLayout
    private lateinit var bottomBar: LinearLayout
    private lateinit var themeButton: Button
    private lateinit var locateButton: Button
    private lateinit var offlineButton: Button
    private var offlineDemo = false
    private var trackingRequested = false
    private var darkChrome = false
    private var locationStarted = false

    private val locationManager by lazy {
        getSystemService(LOCATION_SERVICE) as LocationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(15, 83, 102)
        window.navigationBarColor = Color.rgb(11, 44, 55)
        MapLibre.getInstance(this)

        val root = FrameLayout(this)
        mapView = MapView(this)
        root.addView(mapView, FrameLayout.LayoutParams(-1, -1))
        buildChrome(root)
        setContentView(root)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            liveMap = map
            if (intent.getBooleanExtra(EXTRA_OFFLINE_DEMO, false)) {
                showOfflineDemo(map)
            } else {
                showOnline(map)
            }
        }
    }

    private fun buildChrome(root: FrameLayout) {
        titleBar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = rounded(Color.WHITE)
            elevation = dp(6).toFloat()
        }
        val heading = TextView(this).apply {
            text = "◉ NaviTaxiMap"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(0, 113, 126))
        }
        statusText = TextView(this).apply {
            text = "Đang tải bản đồ thử nghiệm..."
            textSize = 12f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(6), 0, 0)
        }
        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, 0)
        }
        val searchButton = actionButton("Tìm kiếm")
        searchButton.setOnClickListener {
            explain("Tìm kiếm địa điểm", "Chức năng đang trong giai đoạn xây dựng. Ứng dụng chưa có cơ sở dữ liệu POI hoặc kết quả tìm kiếm được xác minh.")
        }
        themeButton = actionButton("Giao diện tối")
        themeButton.setOnClickListener { darkChrome = !darkChrome; updateChrome() }
        actions.addView(searchButton, LinearLayout.LayoutParams(0, dp(44), 1f))
        actions.addView(themeButton, LinearLayout.LayoutParams(0, dp(44), 1f).apply {
            marginStart = dp(8)
        })
        titleBar.addView(heading)
        titleBar.addView(statusText)
        titleBar.addView(actions)
        root.addView(titleBar, FrameLayout.LayoutParams(-1, -2, Gravity.TOP).apply {
            setMargins(dp(12), dp(12), dp(12), 0)
        })

        bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = rounded(Color.WHITE)
            elevation = dp(6).toFloat()
        }
        locationText = TextView(this).apply {
            text = "GPS chưa được bật • chỉ xin quyền khi bạn chọn Vị trí"
            textSize = 14f
            setTextColor(Color.rgb(25, 49, 59))
        }
        locateButton = actionButton("◎ Vị trí")
        locateButton.setOnClickListener { explainLocationPrivacy() }
        val dataButton = actionButton("Dữ liệu bản đồ")
        dataButton.setOnClickListener {
            explain("Trạng thái dữ liệu", "Bản đồ đường phố trực tuyến do OpenFreeMap cung cấp (OpenStreetMap), chưa có SLA. PMTiles, tìm đường, giao thông, dữ liệu 34 tỉnh/thành và hồ sơ biển đảo chưa được tích hợp hoặc phê duyệt. Không dùng màn hình thử nghiệm để điều hướng khi lái xe.")
        }
        val bottomActions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(10), 0, 0)
        }
        bottomActions.addView(locateButton, LinearLayout.LayoutParams(0, dp(46), 1f))
        bottomActions.addView(dataButton, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            marginStart = dp(8)
        })
        offlineButton = actionButton("PMTiles offline (mẫu)")
        offlineButton.setOnClickListener {
            liveMap?.let { map ->
                if (offlineDemo) showOnline(map) else showOfflineDemo(map)
            }
        }
        bottomBar.addView(locationText)
        bottomBar.addView(bottomActions)
        bottomBar.addView(offlineButton, LinearLayout.LayoutParams(-1, dp(46)).apply {
            topMargin = dp(8)
        })
        root.addView(bottomBar, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM).apply {
            setMargins(dp(12), 0, dp(12), dp(14))
        })
    }

    private fun showOnline(map: MapLibreMap) {
        offlineDemo = false
        offlineButton.text = "PMTiles offline (mẫu)"
        val url = if (darkChrome) STREET_STYLE_DARK else STREET_STYLE_LIGHT
        map.setStyle(Style.Builder().fromUri(url)) {
            statusText.text = "OpenFreeMap / OpenStreetMap • trực tuyến • không phải offline"
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(21.273, 106.196), 8.0))
    }

    /**
     * Diagnostic mode only: one synthetic z0 MVT tile. Contains no real streets, routing
     * graph or geographical names. Source URI is file-only with no remote style assets.
     */
    private fun showOfflineDemo(map: MapLibreMap) {
        try {
            val archive = DemoOfflinePack.install(this)
            val styleJson = """{"version":8,"name":"NaviTaxiMap local PMTiles diagnostic",
                "sources":{},"layers":[{"id":"offline-background","type":"background",
                "paint":{"background-color":"#f0f7fa"}}]}"""
            map.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                val source = VectorSource(
                    "synthetic-pmtiles",
                    "pmtiles://file://${archive.absolutePath}"
                )
                style.addSource(source)
                val lines = LineLayer("synthetic-test-lines", "synthetic-pmtiles")
                lines.setSourceLayer("synthetic")
                lines.setProperties(lineColor(Color.rgb(11, 126, 147)), lineWidth(5f))
                style.addLayer(lines)
                offlineDemo = true
                offlineButton.text = "Quay về bản đồ online"
                statusText.text = "PMTiles offline MẪU • đường tổng hợp • không dùng dẫn đường"
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 1.0))
            }
        } catch (error: Exception) {
            statusText.text = "Không thể mở gói PMTiles: ${error.message}"
            explain("Lỗi dữ liệu offline", "Gói PMTiles thử nghiệm chưa được xác minh thành công.")
        }
    }

    private fun explainLocationPrivacy() {
        AlertDialog.Builder(this)
            .setTitle("Quyền vị trí và bản đồ trực tuyến")
            .setMessage("Khi bạn chọn Vị trí, ứng dụng sẽ dùng GPS đưa bản đồ đến khu vực của bạn. Nhà cung cấp bản đồ trực tuyến có thể nhận yêu cầu ô bản đồ của vùng này. Ứng dụng không có máy chủ vị trí riêng. Bạn đồng ý tiếp tục?")
            .setPositiveButton("Đồng ý") { _, _ -> requestLocationOnDemand() }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun requestLocationOnDemand() {
        trackingRequested = true
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), LOCATION_REQUEST)
        } else {
            startLocation()
        }
    }

    private fun startLocation() {
        if (!trackingRequested || locationStarted) return
        val fine = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return
        val provider = when {
            fine && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                LocationManager.NETWORK_PROVIDER
            else -> null
        }
        if (provider == null) {
            locationText.text = "Chưa có nhà cung cấp vị trí. Hãy bật định vị của Android."
            return
        }
        try {
            locationManager.requestLocationUpdates(provider, 2000L, 3f, this)
            locationStarted = true
            locationText.text = "Đang chờ tín hiệu vị trí thực..."
        } catch (error: SecurityException) {
            locationText.text = "Quyền vị trí đã bị thu hồi."
        } catch (error: IllegalArgumentException) {
            locationText.text = "Không thể khởi tạo nhà cung cấp GPS."
        }
    }

    private fun stopLocation() {
        if (locationStarted) {
            locationManager.removeUpdates(this)
            locationStarted = false
        }
    }

    override fun onLocationChanged(location: Location) {
        val speed = if (location.hasSpeed()) {
            String.format(Locale.getDefault(), " • %.0f km/h", location.speed * 3.6f)
        } else ""
        locationText.text = String.format(
            Locale.getDefault(),
            "GPS %.5f, %.5f • ±%.0f m%s",
            location.latitude, location.longitude, location.accuracy, speed
        )
        if (!offlineDemo) {
            liveMap?.animateCamera(CameraUpdateFactory.newLatLng(
                LatLng(location.latitude, location.longitude)
            ))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST) {
            if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) startLocation()
            else locationText.text = "Không có quyền vị trí. Bản đồ vẫn dùng được khi trực tuyến."
        }
    }

    private fun updateChrome() {
        val bg = if (darkChrome) Color.rgb(17, 34, 46) else Color.WHITE
        val fg = if (darkChrome) Color.WHITE else Color.rgb(25, 49, 59)
        titleBar.background = rounded(bg)
        bottomBar.background = rounded(bg)
        statusText.setTextColor(fg)
        locationText.setTextColor(fg)
        themeButton.text = if (darkChrome) "Giao diện sáng" else "Giao diện tối"
        if (offlineDemo) return
        val style = if (darkChrome) STREET_STYLE_DARK else STREET_STYLE_LIGHT
        liveMap?.setStyle(Style.Builder().fromUri(style)) {
            statusText.text = "OpenFreeMap / OpenStreetMap • trực tuyến • chưa có offline"
        }
    }

    private fun actionButton(label: String): Button = Button(this).apply {
        text = label
        textSize = 13f
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = rounded(Color.rgb(0, 119, 133))
        setPadding(dp(6), 0, dp(6), 0)
        stateListAnimator = null
    }

    private fun explain(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton("Đóng", null).show()
    }

    private fun rounded(color: Int): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(16).toFloat()
    }

    private fun dp(n: Int): Int = (n * resources.displayMetrics.density + 0.5f).toInt()

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume(); startLocation() }
    override fun onPause() { stopLocation(); mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onDestroy() { stopLocation(); mapView.onDestroy(); super.onDestroy() }
    override fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val LOCATION_REQUEST = 101
        private const val EXTRA_OFFLINE_DEMO = "vn.navitaximap.OFFLINE_DEMO"
        private const val STREET_STYLE_LIGHT = "https://tiles.openfreemap.org/styles/liberty"
        private const val STREET_STYLE_DARK = "https://tiles.openfreemap.org/styles/dark"
    }
}
