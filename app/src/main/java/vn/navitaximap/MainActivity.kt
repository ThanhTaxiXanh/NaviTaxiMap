package vn.navitaximap

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Base64InputStream
import android.util.Log
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
import java.util.concurrent.atomic.AtomicBoolean
import vn.navitaximap.data.DemoOfflinePack
import vn.navitaximap.data.HanoiRoadPilot
import vn.navitaximap.data.LocalPackStore
import vn.navitaximap.data.PmtilesHeader
import vn.navitaximap.data.Vietnam35Catalog
import vn.navitaximap.data.VietnamRegionSpec
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors
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
    private val packExecutor = Executors.newSingleThreadExecutor()
    private val packStore by lazy { LocalPackStore(java.io.File(filesDir, "offline-packs")) }

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
        // Keep interrupted staging files and one-sided promotions out of the catalog.
        packExecutor.execute {
            val report = runCatching {
                packStore.recoverInterruptedImports()
                HanoiRoadPilot.recover(this)
            }
            report.onFailure { Log.e("NaviTaxiMapRecovery", "Unable to recover incomplete packs", it) }
        }
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            if (intent.getBooleanExtra(EXTRA_DEBUG_IMPORT_FIXTURE, false)) {
                importDebugFixtureForSmokeTest()
            }
            if (intent.getBooleanExtra(EXTRA_DEBUG_PINNED_FETCH, false)) {
                downloadDebugPinnedPilotForSmokeTest()
            }
        }

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            liveMap = map
            if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 &&
                intent.getBooleanExtra(EXTRA_SHORTBREAD_PREVIEW, false)) {
                showDebugShortbreadPreview()
            } else if (intent.getBooleanExtra(EXTRA_OSM_ROADS_PILOT, false)) {
                showHanoiRoadPilot(map)
            } else if (intent.getBooleanExtra(EXTRA_OFFLINE_DEMO, false)) {
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
        dataButton.setOnClickListener { showDataMenu() }
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

    /**
     * ACTION_OPEN_DOCUMENT exposes only the document selected by the user.
     * It needs no broad storage permission. Imported packs are quarantined
     * from navigation and cannot be displayed without reviewed map styles.
     */
    private fun showDataMenu() {
        AlertDialog.Builder(this)
            .setTitle("Quản lý dữ liệu offline")
            .setItems(arrayOf(
                "Nhập tệp PMTiles từ thiết bị",
                "Các gói đã nhập",
                "Xem đường OSM Hà Nội offline (mẫu nhỏ)",
                "Quản lý / tải gói OSM Hà Nội",
                "Nguồn và giới hạn sử dụng",
                "35 gói Shortbread: xem offline sau khi nhập"
            )) { _, item ->
                when (item) {
                    0 -> launchPmtilesPicker(REQUEST_PM_TILES)
                    1 -> showLocalPacks()
                    2 -> liveMap?.let { showHanoiRoadPilot(it) }
                    3 -> showPilotCatalog()
                    5 -> showVietnam35Catalog()
                    else -> explain("Thông tin dữ liệu",
                        "Nguồn online: OpenFreeMap/OpenStreetMap. Gói PMTiles người dùng chọn sẽ chỉ được lưu trong ứng dụng, kiểm tra cấu trúc và SHA-256, chưa xác minh giấy phép hay tính chính xác địa lý. Chưa có giao diện vẽ gói tự nhập vì cần style/layer được duyệt. Gói đường OSM Hà Nội chỉ là một phần nhỏ có xuất xứ OSM, © OpenStreetMap contributors, ODbL 1.0; không phải bản đồ đầy đủ hoặc dữ liệu điều hướng. Gói tổng hợp dùng để thử bộ đọc, không dùng dẫn đường.")
                }
            }.show()
    }

    private fun launchPmtilesPicker(requestCode: Int) {
        val pick = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(pick, requestCode)
    }

    @Deprecated("Uses Activity result until Activity Result API is introduced")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val expectedRegion = Vietnam35Catalog.regionForPickerRequest(requestCode)
        if (requestCode != REQUEST_PM_TILES && expectedRegion == null) return
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        importUserFile(uri, expectedRegion)
    }

    private fun getDocumentName(uri: Uri): String {
        return try {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME),
                null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: "imported.pmtiles"
        } catch (_: Exception) {
            "imported.pmtiles"
        }
    }

    private fun getDocumentSize(uri: Uri): Long? = try {
        contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst() && !cursor.isNull(0)) {
                    cursor.getLong(0).takeIf { it >= 0L }
                } else null
            }
    } catch (_: Exception) {
        null // Some SAF providers do not expose sizes; streaming still checks exact bytes.
    }

    private fun importUserFile(uri: Uri, expectedRegion: VietnamRegionSpec? = null) {
        statusText.text = "Đang nhập PMTiles và tính SHA-256..."
        packExecutor.execute {
            val result = runCatching {
                val name = getDocumentName(uri)
                require(name.endsWith(".pmtiles", ignoreCase = true)) {
                    "Chỉ có thể nhập tệp .pmtiles, không phải tài liệu khác"
                }
                if (expectedRegion != null) {
                    require(Vietnam35Catalog.isExpectedDocumentName(expectedRegion, name)) {
                        "Đã chọn sai tệp. Vui lòng chọn chính xác " + expectedRegion.fileName
                    }
                }
                val region = Vietnam35Catalog.packages.firstOrNull { it.fileName == name }
                if (region != null) {
                    val declared = getDocumentSize(uri)
                    require(declared == null || declared == region.sizeBytes) {
                        "Dung lượng tài liệu không khớp gói Shortbread đã ghim"
                    }
                    val free = filesDir.usableSpace
                    require(free >= region.sizeBytes + 8L * 1024 * 1024) {
                        "Không đủ dung lượng trống để nhập gói Shortbread: " + region.label
                    }
                }
                val input = contentResolver.openInputStream(uri)
                    ?: error("Cannot open selected document")
                // For debug QA only: inspect the selected SAF STREAM, not the
                // separate /sdcard/Download path. A stale DocumentsUI URI may
                // expose different bytes even when that path's SHA is valid.
                // No URI, raw file content or personal data is emitted to logs.
                val guardedInput = if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                    val buffered = java.io.BufferedInputStream(input, 16 * 1024)
                    buffered.mark(8)
                    val magic = ByteArray(8)
                    val got = buffered.read(magic)
                    buffered.reset()
                    val validMagic = got == 8 && magic.contentEquals(
                        byteArrayOf(80, 77, 84, 105, 108, 101, 115, 3))
                    Log.i("NaviTaxiMapSAF", "SAF_SELECTED_STREAM " +
                        "pinned=" + (region?.code ?: "none") +
                        " correctExtension=" + name.endsWith(".pmtiles", true) +
                        " headerBytes=" + got + " isPmtilesV3=" + validMagic)
                    buffered
                } else input
                packStore.importPack(guardedInput, name, region?.sha256, region?.sizeBytes)
            }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                result.onSuccess { pack ->
                    val region = Vietnam35Catalog.packages.firstOrNull { it.matches(pack) }
                    statusText.text = if (region != null) {
                        "Đã nhận diện ${region.label}, SHA-256 và kích thước hợp lệ"
                    } else "Đã nhập gói: ${pack.name}"
                    explain("Đã lưu PMTiles",
                        "Đã lưu ${pack.name} • ${pack.sizeBytes / 1024} KiB • zoom ${pack.minZoom}-${pack.maxZoom}. SHA-256: ${pack.sha256}." +
                        if (region != null) " Danh mục Shortbread xác nhận byte/hash, nhưng KHÔNG duyệt dẫn đường, pháp lý hoặc phát hành."
                        else " Tệp chưa được xác minh nguồn/pháp lý; chưa kích hoạt để dẫn đường.")
                    // Direct catalog import rechecks original SHA before opening
                    // the same local-only preview; it never starts network download.
                    if (expectedRegion != null && region != null) {
                        showVerifiedShortbread(region, pack)
                    }
                }.onFailure { error ->
                    statusText.text = "Không thể nhập PMTiles."
                    explain("Nhập không thành công", error.message ?: "File không hợp lệ.")
                }
            }
        }
    }


    /**
     * A pinned *visual-preview* catalog, separate from arbitrary imported
     * UNVERIFIED PMTiles. This is not a navigation-data approval process.
     * Never start remote transfer without explicit user confirmation.
     */
    private fun showPilotCatalog() {
        statusText.text = "Đang kiểm tra phiên bản và SHA-256 của gói Hà Nội..."
        packExecutor.execute {
            val result = runCatching {
                HanoiRoadPilot.recover(this)
                HanoiRoadPilot.installed(this) to HanoiRoadPilot.versionInstalled(this)
            }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                result.onFailure {
                    statusText.text = "Không kiểm tra được danh mục bản đồ."
                    explain("Lỗi dữ liệu", it.message ?: "Không xác minh được gói.")
                }.onSuccess { (verified, version) ->
                    val spec = HanoiRoadPilot.spec
                    val state = when {
                        verified != null -> "Đã cài v${verified.version} • SHA-256 hợp lệ • ${verified.origin}"
                        version != null -> "Gói có sẵn nhưng không khớp bản v${spec.version}; cần cài lại"
                        else -> "Chưa cài (có bản đi kèm APK để dùng offline)"
                    }
                    AlertDialog.Builder(this)
                        .setTitle("Danh mục bản đồ • mẫu Hà Nội")
                        .setMessage(
                            "$state\nVùng thử nghiệm rất nhỏ • zoom ${spec.minZoom}-${spec.maxZoom}" +
                            "\n${spec.sizeBytes / 1024} KiB • SHA-256: ${spec.sha256}" +
                            "\nNguồn gốc OSM: ${spec.sourceXmlSha256}" +
                            "\n© OpenStreetMap contributors • ODbL 1.0" +
                            "\nCHƯA DUYỆT để dẫn đường hoặc xác định ranh giới."
                        )
                        .setItems(arrayOf(
                            "Cài / sửa gói từ APK (không cần mạng)",
                            "Tải qua HTTPS từ commit đã ghim",
                            "Kiểm tra lại SHA-256",
                            "Xóa bản sao khỏi ứng dụng"
                        )) { _, choice ->
                            when (choice) {
                                0 -> installPilotFromApk()
                                1 -> confirmPinnedPilotDownload()
                                2 -> verifyPilotCatalog()
                                else -> confirmDeletePilotCatalog()
                            }
                        }
                        .setNegativeButton("Đóng", null)
                        .show()
                }
            }
        }
    }

    private fun installPilotFromApk() {
        statusText.text = "Đang kiểm tra gói OSM đi kèm APK..."
        packExecutor.execute {
            val result = runCatching { HanoiRoadPilot.install(this) }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                result.onSuccess {
                    statusText.text = "Đã cài gói mẫu Hà Nội và kiểm tra SHA-256."
                    explain("Đã lưu bản thử nghiệm",
                        "Gói OSM mẫu từ APK đã được kiểm tra mã SHA-256, chỉ dành cho xem đường. Không dùng dẫn đường.")
                }.onFailure {
                    statusText.text = "Không cài được PMTiles mẫu."
                    explain("Không thể cài", it.message ?: "Lỗi dữ liệu.")
                }
            }
        }
    }

    private fun verifyPilotCatalog() {
        statusText.text = "Đang băm lại toàn bộ gói đã cài..."
        packExecutor.execute {
            val verified = runCatching { HanoiRoadPilot.installed(this) }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                statusText.text = if (verified.getOrNull() != null) {
                    "Gói OSM Hà Nội v1 khớp SHA-256 • chưa duyệt để dẫn đường"
                } else "Chưa cài hoặc gói OSM Hà Nội đã thay đổi; hãy cài lại."
            }
        }
    }

    private fun confirmDeletePilotCatalog() {
        AlertDialog.Builder(this)
            .setTitle("Xóa gói bản đồ mẫu?")
            .setMessage("Chỉ xóa bản sao PMTiles riêng trong ứng dụng. Dữ liệu OSM và tệp đi kèm APK không bị xóa. Khi xem lại, gói có thể được cài lại từ APK.")
            .setPositiveButton("Xóa") { _, _ ->
                packExecutor.execute {
                    val result = runCatching { HanoiRoadPilot.delete(this) }
                    runOnUiThread {
                        if (!isDestroyed) statusText.text =
                            if (result.getOrDefault(false)) "Đã xóa bản sao gói OSM Hà Nội"
                            else "Không tìm thấy bản sao hoặc không thể xóa"
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun confirmPinnedPilotDownload() {
        val spec = HanoiRoadPilot.spec
        AlertDialog.Builder(this)
            .setTitle("Tải đúng gói PMTiles đã ghim")
            .setMessage(
                "Sẽ tải một lần ${spec.sizeBytes / 1024} KiB qua HTTPS từ commit GitHub cố định." +
                "\nỨng dụng kiểm tra dung lượng, cấu trúc PMTiles và SHA-256 trước khi cài." +
                "\nKhông tải khi chưa đồng ý; không tải bộ ô bản đồ công cộng." +
                "\nGói vẫn CHƯA DUYỆT để dẫn đường. Tiếp tục?"
            )
            .setPositiveButton("Tải gói") { _, _ -> downloadPinnedPilot() }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun downloadPinnedPilot() {
        val cancelled = AtomicBoolean(false)
        val progress = AlertDialog.Builder(this)
            .setTitle("Tải gói OSM mẫu đã ghim")
            .setMessage("Đang kết nối qua HTTPS...")
            .setNegativeButton("Hủy tải") { _, _ -> cancelled.set(true) }
            .create()
        progress.show()
        packExecutor.execute {
            val result = runCatching {
                HanoiRoadPilot.download(
                    this,
                    cancelled = { cancelled.get() },
                    onProgress = { copied, expected ->
                        runOnUiThread {
                            if (!isDestroyed && progress.isShowing) {
                                progress.setMessage("Đã nhận $copied / $expected byte; đang kiểm tra SHA-256...")
                            }
                        }
                    }
                )
            }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                progress.dismiss()
                result.onSuccess { pack ->
                    statusText.text = "Gói Hà Nội v${pack.version} • SHA-256 hợp lệ • chỉ để xem"
                    explain("Đã xác minh gói bản đồ",
                        "Có ${pack.sizeBytes} byte từ nguồn ${pack.origin}; đã kiểm tra SHA-256. Nếu gói đã cài trước đó thì không tải lại. Không dùng để dẫn đường.")
                }.onFailure { error ->
                    statusText.text = if (cancelled.get()) "Đã hủy tải gói." else "Không thể tải / xác minh PMTiles."
                    explain("Chưa cài gói từ mạng", error.message ?: "Lỗi kết nối hoặc checksum.")
                }
            }
        }
    }

    private fun showLocalPacks() {
        packExecutor.execute {
            val packs = packStore.listPacks()
            val usage = packStore.storageUsage()
            val usageLabel = "Đã dùng ${usage.first / (1024 * 1024)} MiB; còn trống ${usage.second / (1024 * 1024)} MiB"
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                if (packs.isEmpty()) {
                    explain("Các gói offline", "Chưa có PMTiles nào được nhập.\\n$usageLabel")
                    return@runOnUiThread
                }
                val labels = packs.map { "${it.name} • ${it.sizeBytes / 1024} KiB" }.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle("PMTiles chưa duyệt • ${usage.first / (1024 * 1024)} MiB")
                    .setItems(labels) { _, index -> showPackOptions(packs[index]) }
                    .setNegativeButton("Đóng", null).show()
            }
        }
    }

    private fun showPackOptions(pack: LocalPackStore.Pack) {
        AlertDialog.Builder(this).setTitle(pack.name)
            .setMessage("SHA-256: ${pack.sha256}\nZoom: ${pack.minZoom}-${pack.maxZoom}\nNguồn: chưa xác minh. Chưa dùng để dẫn đường.")
            .setPositiveButton("Kiểm tra lại") { _, _ ->
                statusText.text = "Đang xác minh gói..."
                packExecutor.execute {
                    val good = packStore.verifyPack(pack.id)
                    runOnUiThread {
                        if (!isDestroyed) {
                            statusText.text = if (good) "SHA-256 của gói trùng bản nhập" else "Gói bị lỗi hoặc đã thay đổi"
                        }
                    }
                }
            }
            .setNeutralButton("Xóa gói") { _, _ ->
                AlertDialog.Builder(this).setTitle("Xóa gói PMTiles?")
                    .setMessage("Xóa tệp đã nhập trong ứng dụng. Tệp gốc bạn đã chọn không bị xóa.")
                    .setPositiveButton("Xóa") { _, _ ->
                        packExecutor.execute {
                            val removed = packStore.deletePack(pack.id)
                            runOnUiThread {
                                if (!isDestroyed) statusText.text =
                                    if (removed) "Đã xóa gói offline" else "Không thể xóa toàn bộ gói"
                            }
                        }
                    }
                    .setNegativeButton("Hủy", null).show()
            }
            .setNegativeButton("Đóng", null).show()
    }


    /** CI-only: real HTTPS code path, never silently fetched in release builds. */
    private fun downloadDebugPinnedPilotForSmokeTest() {
        packExecutor.execute {
            runCatching {
                val fetched = HanoiRoadPilot.download(this)
                check(HanoiRoadPilot.installed(this) != null)
                Log.i("NaviTaxiMapCatalog",
                    "DEBUG_PINNED_FETCH_PASS bytes=${fetched.sizeBytes} origin=${fetched.origin}")
            }.onFailure { error ->
                Log.e("NaviTaxiMapCatalog", "DEBUG_PINNED_FETCH_FAIL", error)
            }
        }
    }

    private fun importDebugFixtureForSmokeTest() {
        packExecutor.execute {
            runCatching {
                val input = Base64InputStream(assets.open("offline_demo.pmtiles.b64"), Base64.DEFAULT)
                val installed = packStore.importPack(input, "Mẫu PMTiles nội bộ")
                check(packStore.verifyPack(installed.id))
                Log.i("NaviTaxiMapImport", "DEBUG_IMPORT_PASS bytes=${installed.sizeBytes}")
            }.onFailure { Log.e("NaviTaxiMapImport", "DEBUG_IMPORT_FAIL", it) }
        }
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


    /**
     * A tiny REAL OSM road-only visual preview (about 1 km2 of Hanoi).
     * Source is a dated, hash-pinned API snapshot and ODbL 1.0; it is
     * NOT an approved routing database, legal boundary layer or city map.
     * This style has NO remote sprites, glyphs or URL assets.
     */
    private fun showHanoiRoadPilot(map: MapLibreMap) {
        try {
            val archive = HanoiRoadPilot.install(this)
            val styleJson = """{"version":8,"name":"NaviTaxiMap Hanoi OSM roads preview",
                "sources":{},"layers":[{"id":"pilot-background","type":"background",
                "paint":{"background-color":"#f3f7f3"}}]}"""
            map.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                val source = VectorSource(
                    "hanoi-pilot-vector", "pmtiles://file://${archive.absolutePath}"
                )
                style.addSource(source)
                fun addRoadLayer(id: String, layerName: String, color: Int, width: Float) {
                    val layer = LineLayer(id, "hanoi-pilot-vector")
                    layer.setSourceLayer(layerName)
                    layer.setProperties(lineColor(color), lineWidth(width))
                    style.addLayer(layer)
                }
                addRoadLayer("hanoi-paths", "road_pedestrian",
                    Color.rgb(128, 151, 143), 1.8f)
                addRoadLayer("hanoi-local-roads", "road_local",
                    Color.rgb(39, 94, 108), 3.2f)
                addRoadLayer("hanoi-major-roads", "road_major",
                    Color.rgb(211, 100, 52), 4.5f)
                offlineDemo = true
                offlineButton.text = "Quay về bản đồ online"
                statusText.text = "Offline Hà Nội • đường OSM thật (mẫu) • © OpenStreetMap contributors (ODbL) • KHÔNG dẫn đường"
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(21.0340, 105.8535), 16.2
                ))
            }
        } catch (error: Exception) {
            statusText.text = "Mẫu đường OSM offline không đọc được."
            explain("Lỗi gói đường mẫu", error.message ?: "Không xác minh được PMTiles.")
        }
    }


    /** No automatic download or unapproved border overlay. SHA checked again before drawing. */
    private fun showVietnam35Catalog() {
        statusText.text = "Đang kiểm tra danh mục Shortbread 35 gói..."
        packExecutor.execute {
            val imported = packStore.listPacks()
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                val entries = Vietnam35Catalog.packages
                val labels = entries.map { region ->
                    region.code + " · " + region.label + " · " +
                        String.format(Locale.US, "%.1f MiB", region.sizeBytes / 1048576.0) +
                        if (imported.any { region.matches(it) }) " · đã nhập" else " · chưa nhập"
                }.toTypedArray()
                AlertDialog.Builder(this)
                    // AlertDialog cannot render both a message panel and a list:
                    // using setMessage hid all 35 selectable entries on Android.
                    .setTitle("Shortbread 1.0 · 35 gói · KHÔNG dẫn đường")
                    .setItems(labels) { _, index ->
                        val region = entries[index]
                        val selected = imported.firstOrNull { region.matches(it) }
                        if (selected == null) {
                            AlertDialog.Builder(this)
                                .setTitle("Chọn PMTiles cho " + region.label)
                                .setMessage("Chỉ nhập tệp gốc đã lưu trên thiết bị: " +
                                    region.fileName + ". Kiểm tra SHA-256, số byte và zoom trước " +
                                    "khi xem offline. Không tải mạng, không dẫn đường.")
                                .setPositiveButton("Chọn tệp trên thiết bị") { _, _ ->
                                    launchPmtilesPicker(Vietnam35Catalog.pickerRequestCode(region))
                                }
                                .setNegativeButton("Hủy", null)
                                .show()
                        } else showVerifiedShortbread(region, selected)
                    }
                    .setNegativeButton("Đóng", null).show()
            }
        }
    }

    /** Debug-only real archive test; never silently install/download a 35-pack. */
    private fun showDebugShortbreadPreview() {
        packExecutor.execute {
            val code = intent.getStringExtra(EXTRA_SHORTBREAD_REGION) ?: "24"
            val region = Vietnam35Catalog.packages.firstOrNull { it.code == code }
            val selected = region?.let { spec ->
                packStore.listPacks().firstOrNull { spec.matches(it) }
            }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                if (selected == null) {
                    statusText.text = "SHORTBREAD_TEST_MISSING: " + code
                    Log.e("NaviTaxiMapShortbread", "SHORTBREAD_TEST_MISSING code=" + code)
                } else showVerifiedShortbread(region!!, selected)
            }
        }
    }

    private fun showVerifiedShortbread(region: VietnamRegionSpec, imported: LocalPackStore.Pack) {
        statusText.text = "Đang tính lại SHA-256: " + region.label
        packExecutor.execute {
            val checked = runCatching {
                require(region.matches(imported)) { "Gói không khớp danh mục 35." }
                check(packStore.verifyPack(imported.id)) { "SHA-256 / PMTiles bị thay đổi." }
                val directory = File(filesDir, "offline-packs").canonicalFile
                val archive = File(directory, imported.id + ".pmtiles").canonicalFile
                require(archive.parentFile == directory && archive.isFile) { "Đường dẫn không an toàn." }
                val header = PmtilesHeader.inspect(archive)
                require(header.minZoom == region.minZoom && header.maxZoom == region.maxZoom)
                val template = assets.open("shortbread_offline_style.json")
                    .bufferedReader().use { it.readText() }
                require(template.contains("\"__PMTILES_URL__\""))
                val uri = "pmtiles://file://" + archive.absolutePath
                val localStyle = template.replace("\"__PMTILES_URL__\"", JSONObject.quote(uri))
                Pair(localStyle, header)
            }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                checked.onFailure { failure ->
                    statusText.text = "Không mở được gói Shortbread: " + failure.message
                    Log.e("NaviTaxiMapShortbread", "SHORTBREAD_VERIFY_FAIL", failure)
                }.onSuccess { (localStyle, header) ->
                    liveMap?.setStyle(Style.Builder().fromJson(localStyle)) {
                        offlineDemo = true
                        offlineButton.text = "Quay về bản đồ online"
                        statusText.text = "Shortbread 1.0 · " + region.label +
                            " · © OpenStreetMap contributors (ODbL) · KHÔNG dẫn đường"
                        // A province's PMTiles bbox midpoint can fall in open sea,
                        // marshland or sparsely mapped terrain. Prefer grounded urban
                        // anchors ONLY for visual preview, never as an administrative
                        // centroid, routing waypoint or legal boundary assertion.
                        // City reference coordinates: UNGEGN/GNS (Da Nang, Ca Mau,
                        // Nha Trang, Rach Gia), GeoNames (Hue), UNT GeoNames (Hai Phong).
                        // Debug QA probes the nationwide 556MB archive at an
                        // explicitly chosen urban road location. Normal nationwide
                        // browsing retains the zoomed-out header-center overview.
                        val nationalDetailProbe = region.code == "VN" &&
                            (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 &&
                            intent.getBooleanExtra(EXTRA_SHORTBREAD_NATIONAL_DETAIL, false)
                        val center = if (nationalDetailProbe) LatLng(21.0285, 105.8542) else when (region.code) {
                            "24" -> LatLng(21.273, 106.196)
                            "01" -> LatLng(21.0285, 105.8542)
                            "79" -> LatLng(10.7769, 106.7009)
                            "31" -> LatLng(20.86481, 106.68345)     // Hải Phòng urban centre
                            "46" -> LatLng(16.46190, 107.59546)     // Huế urban centre
                            "48" -> LatLng(16.067778, 108.220833)   // Đà Nẵng urban centre
                            "56" -> LatLng(12.25, 109.183333)       // Nha Trang, Khánh Hòa
                            "91" -> LatLng(10.016667, 105.083333)   // Rạch Giá, An Giang
                            "96" -> LatLng(9.176944, 105.15)        // Cà Mau urban centre
                            else -> LatLng(header.centerLat, header.centerLon)
                        }
                        liveMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            center, if (region.code == "VN" && !nationalDetailProbe) 5.5 else 12.3
                        ))
                        Log.i("NaviTaxiMapShortbread", "SHORTBREAD_STYLE_LOADED " + region.fileName)
                    }
                }
            }
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
    override fun onDestroy() {
        stopLocation()
        packExecutor.shutdownNow()
        mapView.onDestroy()
        super.onDestroy()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val LOCATION_REQUEST = 101
        private const val REQUEST_PM_TILES = 102
        private const val EXTRA_DEBUG_IMPORT_FIXTURE = "vn.navitaximap.DEBUG_IMPORT_FIXTURE"
        private const val EXTRA_DEBUG_PINNED_FETCH = "vn.navitaximap.DEBUG_PINNED_CATALOG_FETCH"
        private const val EXTRA_OFFLINE_DEMO = "vn.navitaximap.OFFLINE_DEMO"
        private const val EXTRA_OSM_ROADS_PILOT = "vn.navitaximap.REAL_OSM_PILOT"
        private const val EXTRA_SHORTBREAD_PREVIEW = "vn.navitaximap.SHORTBREAD_PREVIEW"
        private const val EXTRA_SHORTBREAD_REGION = "vn.navitaximap.SHORTBREAD_REGION"
        private const val EXTRA_SHORTBREAD_NATIONAL_DETAIL = "vn.navitaximap.SHORTBREAD_NATIONAL_DETAIL"
        private const val STREET_STYLE_LIGHT = "https://tiles.openfreemap.org/styles/liberty"
        private const val STREET_STYLE_DARK = "https://tiles.openfreemap.org/styles/dark"
    }
}
