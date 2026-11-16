package com.cali.core.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ImageCompressorTest {

    // --- phần thuần ---

    @Test fun `cạnh dài thu về 1024, giữ tỉ lệ`() {
        assertEquals(1024 to 768, ImageSizing.fit(4000, 3000, 1024))
        assertEquals(768 to 1024, ImageSizing.fit(3000, 4000, 1024))
        assertEquals(1024 to 576, ImageSizing.fit(1920, 1080, 1024))
    }

    @Test fun `ảnh nhỏ hơn giới hạn giữ nguyên`() {
        assertEquals(800 to 600, ImageSizing.fit(800, 600, 1024))
        assertEquals(1024 to 1024, ImageSizing.fit(1024, 1024, 1024))
    }

    @Test fun `cạnh ngắn không về 0`() {
        assertEquals(1024 to 1, ImageSizing.fit(10000, 2, 1024))
    }

    @Test fun `inSampleSize là luỹ thừa 2 lớn nhất mà vẫn không nhỏ hơn đích`() {
        assertEquals(2, ImageSizing.sampleSize(4000, 3000, 1024))
        assertEquals(1, ImageSizing.sampleSize(1500, 1000, 1024))
        assertEquals(4, ImageSizing.sampleSize(8000, 6000, 1024))
        assertEquals(1, ImageSizing.sampleSize(800, 600, 1024))
    }

    @Test fun `góc xoay theo EXIF`() {
        assertEquals(0, ImageSizing.rotationDegrees(ExifInterface.ORIENTATION_NORMAL))
        assertEquals(90, ImageSizing.rotationDegrees(ExifInterface.ORIENTATION_ROTATE_90))
        assertEquals(180, ImageSizing.rotationDegrees(ExifInterface.ORIENTATION_ROTATE_180))
        assertEquals(270, ImageSizing.rotationDegrees(ExifInterface.ORIENTATION_ROTATE_270))
        assertEquals(0, ImageSizing.rotationDegrees(ExifInterface.ORIENTATION_UNDEFINED))
    }

    @Test fun `đọc DateTimeOriginal`() {
        assertEquals(LocalDateTime.of(2026, 11, 13, 7, 10, 22), ImageSizing.parseExifDate("2026:11:13 07:10:22"))
        assertNull(ImageSizing.parseExifDate(null))
        assertNull(ImageSizing.parseExifDate("0000:00:00 00:00:00"))
        assertNull(ImageSizing.parseExifDate("rác"))
    }

    // --- giải mã thật (Robolectric native graphics) ---

    @Test fun `nén ảnh có EXIF xoay 90, lấy giờ chụp, xoá EXIF`() = runBlocking {
        val src = jpegWithExif(2000, 1500, ExifInterface.ORIENTATION_ROTATE_90)
        val out = ImageCompressor().compress(src)

        assertEquals(LocalDateTime.of(2026, 11, 13, 7, 10, 22), out.takenAt)
        // Xoay 90° → dọc; cạnh dài 1024.
        assertEquals(768, out.width)
        assertEquals(1024, out.height)
        val decoded = BitmapFactory.decodeByteArray(out.jpeg, 0, out.jpeg.size)
        assertEquals(768, decoded.width)
        assertEquals(1024, decoded.height)

        val exif = ExifInterface(ByteArrayInputStream(out.jpeg))
        assertNull(exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL))
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE))
        assertNull(exif.getAttribute(ExifInterface.TAG_MAKE))

        val thumb = BitmapFactory.decodeByteArray(out.thumbnailJpeg, 0, out.thumbnailJpeg.size)
        assertEquals(256, maxOf(thumb.width, thumb.height))
        assertEquals(192, thumb.width)
    }

    @Test fun `ảnh không EXIF thì takenAt null`() = runBlocking {
        val bmp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.RED) }
        val bytes = ByteArrayOutputStream().also { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }.toByteArray()
        val out = ImageCompressor().compress(bytes)
        assertNull(out.takenAt)
        assertEquals(640, out.width)
        assertEquals(480, out.height)
    }

    @Test fun `dữ liệu không phải ảnh thì ném lỗi rõ ràng`() {
        val r = runCatching { runBlocking { ImageCompressor().compress(byteArrayOf(1, 2, 3)) } }
        assertTrue(r.exceptionOrNull() is IllegalArgumentException)
    }

    private fun jpegWithExif(w: Int, h: Int, orientation: Int): ByteArray {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }
        val file = File.createTempFile("cali", ".jpg")
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        ExifInterface(file.absolutePath).apply {
            setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, "2026:11:13 07:10:22")
            setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
            setAttribute(ExifInterface.TAG_GPS_LATITUDE, "10/1,46/1,0/1")
            setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N")
            setAttribute(ExifInterface.TAG_MAKE, "Cali")
            saveAttributes()
        }
        return file.readBytes().also { file.delete() }
    }
}
