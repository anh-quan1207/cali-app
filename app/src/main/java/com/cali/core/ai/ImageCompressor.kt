package com.cali.core.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Ảnh đã nén để gửi AI (03 §7): không còn EXIF, cạnh dài ≤ 1024 px, JPEG 80. */
class CompressedImage(
    val jpeg: ByteArray,
    val width: Int,
    val height: Int,
    /** `DateTimeOriginal` đọc trước khi xoá EXIF; dùng làm giờ ăn khi chọn từ thư viện (05 §4.1). */
    val takenAt: LocalDateTime?,
    /** Thumbnail cạnh dài 256 px để lưu nếu bật `saveThumbnails`. */
    val thumbnailJpeg: ByteArray,
)

/** Phần tính toán thuần của [ImageCompressor], tách ra để test không cần giải mã ảnh. */
object ImageSizing {
    const val MAX_EDGE = 1024
    const val THUMB_EDGE = 256
    const val QUALITY = 80

    private val EXIF_DATE = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

    /** Kích thước sau khi thu nhỏ để cạnh dài ≤ [maxEdge]; không phóng to. */
    fun fit(w: Int, h: Int, maxEdge: Int): Pair<Int, Int> {
        val longEdge = max(w, h)
        if (longEdge <= maxEdge) return w to h
        val scale = maxEdge.toDouble() / longEdge
        val nw = if (w >= h) maxEdge else (w * scale).roundToInt().coerceAtLeast(1)
        val nh = if (h > w) maxEdge else (h * scale).roundToInt().coerceAtLeast(1)
        return nw to nh
    }

    /** `inSampleSize` lớn nhất (luỹ thừa 2) mà cạnh dài sau giải mã vẫn ≥ [maxEdge], để tiết kiệm bộ nhớ. */
    fun sampleSize(w: Int, h: Int, maxEdge: Int): Int {
        var s = 1
        while (max(w, h) / (s * 2) >= maxEdge) s *= 2
        return s
    }

    fun rotationDegrees(orientation: Int): Int = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> 90
        ExifInterface.ORIENTATION_ROTATE_180, ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180
        ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> 270
        else -> 0
    }

    fun isMirrored(orientation: Int): Boolean = orientation in setOf(
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
        ExifInterface.ORIENTATION_FLIP_VERTICAL,
        ExifInterface.ORIENTATION_TRANSPOSE,
        ExifInterface.ORIENTATION_TRANSVERSE,
    )

    fun parseExifDate(s: String?): LocalDateTime? =
        s?.trim()?.let { runCatching { LocalDateTime.parse(it, EXIF_DATE) }.getOrNull() }
}

/**
 * Nén ảnh trước khi gửi (03 §7): đọc EXIF → xoay đúng chiều → thu nhỏ → mã hoá lại JPEG.
 * Mã hoá lại từ Bitmap nên toàn bộ EXIF (GPS, máy chụp…) bị bỏ. Không lưu ảnh gốc.
 */
class ImageCompressor @Inject constructor() {

    internal var dispatcher: CoroutineDispatcher = Dispatchers.Default

    /** @throws IllegalArgumentException nếu [bytes] không phải ảnh giải mã được. */
    suspend fun compress(bytes: ByteArray): CompressedImage = withContext(dispatcher) {
        val exif = runCatching { ExifInterface(ByteArrayInputStream(bytes)) }.getOrNull()
        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            ?: ExifInterface.ORIENTATION_NORMAL
        val takenAt = ImageSizing.parseExifDate(
            exif?.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) ?: exif?.getAttribute(ExifInterface.TAG_DATETIME),
        )

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Không đọc được ảnh" }

        val opts = BitmapFactory.Options().apply {
            inSampleSize = ImageSizing.sampleSize(bounds.outWidth, bounds.outHeight, ImageSizing.MAX_EDGE)
        }
        val decoded = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)) { "Không giải mã được ảnh" }

        val upright = orient(decoded, orientation)
        val (w, h) = ImageSizing.fit(upright.width, upright.height, ImageSizing.MAX_EDGE)
        val main = scale(upright, w, h)
        val (tw, th) = ImageSizing.fit(main.width, main.height, ImageSizing.THUMB_EDGE)
        val thumb = scale(main, tw, th)

        CompressedImage(
            jpeg = encode(main),
            width = main.width,
            height = main.height,
            takenAt = takenAt,
            thumbnailJpeg = encode(thumb),
        ).also {
            listOf(decoded, upright, main, thumb).distinct().forEach(Bitmap::recycle)
        }
    }

    private fun orient(src: Bitmap, orientation: Int): Bitmap {
        val degrees = ImageSizing.rotationDegrees(orientation)
        val mirror = ImageSizing.isMirrored(orientation)
        if (degrees == 0 && !mirror) return src
        val m = Matrix().apply {
            if (mirror) postScale(-1f, 1f)
            if (degrees != 0) postRotate(degrees.toFloat())
        }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }

    private fun scale(src: Bitmap, w: Int, h: Int): Bitmap =
        if (src.width == w && src.height == h) src else Bitmap.createScaledBitmap(src, w, h, true)

    private fun encode(b: Bitmap): ByteArray =
        ByteArrayOutputStream().also { b.compress(Bitmap.CompressFormat.JPEG, ImageSizing.QUALITY, it) }.toByteArray()
}

/** Lưu thumbnail 256 px trong bộ nhớ riêng của app (`filesDir/thumbnails`), trả đường dẫn cho `thumbnail_path`. */
@Singleton
class ThumbnailStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val dir: File get() = File(context.filesDir, "thumbnails").apply { mkdirs() }

    fun save(jpeg: ByteArray): Result<String> = runCatching {
        File(dir, "${UUID.randomUUID()}.jpg").apply { writeBytes(jpeg) }.absolutePath
    }

    fun delete(path: String): Boolean {
        val f = File(path)
        return f.parentFile?.canonicalPath == dir.canonicalPath && f.delete()
    }
}
