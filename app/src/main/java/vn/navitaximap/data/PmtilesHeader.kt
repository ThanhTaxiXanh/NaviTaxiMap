package vn.navitaximap.data

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Minimal PMTiles v3 structural preflight, NOT a full directory or tile decoder.
 * MapLibre remains responsible for decoding vector tiles.
 */
data class PmtilesHeader(
    val minZoom: Int,
    val maxZoom: Int,
    val tileType: Int,
    val centerLat: Double,
    val centerLon: Double
) {
    companion object {
        private const val SIZE = 127
        private val MAGIC = byteArrayOf(80, 77, 84, 105, 108, 101, 115, 3)

        fun inspect(file: File): PmtilesHeader = RandomAccessFile(file, "r").use { io ->
            val size = io.length()
            require(size >= SIZE) { "PMTiles header too short" }
            val bytes = ByteArray(SIZE)
            io.readFully(bytes)
            require(bytes.copyOfRange(0, 8).contentEquals(MAGIC)) { "Not a PMTiles v3 archive" }
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            fun section(offsetAt: Int, lengthAt: Int, name: String, canBeEmpty: Boolean = false): Pair<Long, Long> {
                val offset = buffer.getLong(offsetAt)
                val length = buffer.getLong(lengthAt)
                require(offset >= SIZE && offset <= size && length >= 0 && length <= size - offset) {
                    "Invalid $name range"
                }
                require(canBeEmpty || length > 0) { "Empty $name" }
                return offset to length
            }
            val root = section(8, 16, "root directory")
            require(root.first + root.second <= 16_384L) { "PMTiles root directory must fit in first 16 KiB" }
            section(24, 32, "JSON metadata")
            section(40, 48, "leaf directory", canBeEmpty = true)
            section(56, 64, "tile data")
            require((72..88 step 8).all { buffer.getLong(it) > 0 }) { "Empty PMTiles tile index" }
            val internalCompression = bytes[97].toInt() and 0xFF
            val tileCompression = bytes[98].toInt() and 0xFF
            require(internalCompression in 1..4 && tileCompression in 1..4) { "Unsupported compression flags" }
            val tileType = bytes[99].toInt() and 0xFF
            require(tileType == 1) { "Only MVT vector PMTiles can be imported in this stage" }
            val minZoom = bytes[100].toInt() and 0xFF
            val maxZoom = bytes[101].toInt() and 0xFF
            require(minZoom <= maxZoom && maxZoom <= 22) { "Invalid zoom range" }
            val minLon = buffer.getInt(102) / 10_000_000.0
            val minLat = buffer.getInt(106) / 10_000_000.0
            val maxLon = buffer.getInt(110) / 10_000_000.0
            val maxLat = buffer.getInt(114) / 10_000_000.0
            require(minLat >= -90 && maxLat <= 90 && minLat <= maxLat &&
                minLon >= -180 && maxLon <= 180 && minLon <= maxLon) { "Invalid bounds" }
            val centerLon = buffer.getInt(119) / 10_000_000.0
            val centerLat = buffer.getInt(123) / 10_000_000.0
            require(centerLon in -180.0..180.0 && centerLat in -90.0..90.0) {
                "Invalid center"
            }
            PmtilesHeader(minZoom, maxZoom, tileType, centerLat, centerLon)
        }
    }
}
