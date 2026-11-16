package com.cali.core.data.prefs

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

/** Logic mã hoá/lưu trữ với khóa JCE thường; khóa Android Keystore thật kiểm ở androidTest. */
class SecureKeyStoreTest {
    @get:Rule val tmp = TemporaryFolder()

    private val key = KeyGenerator.getInstance("AES", "SunJCE").apply { init(256) }.generateKey()
    private val fakeKey = "sk-test-0000-khong-phai-key-that"

    @Test fun `luu, doc lai, xoa`() = runTestWithScope {
        val file = tmp.newFile("s.preferences_pb").also { it.delete() }
        val store = PreferenceDataStoreFactory.create(scope = scope) { file }
        val ks = SecureKeyStore(store, KeyProvider { key }).sunJce()

        assertNull(ks.read().getOrThrow())
        assertFalse(ks.hasKey.first())

        assertTrue(ks.save("  $fakeKey \n").isSuccess)
        assertTrue(ks.hasKey.first())
        assertEquals(fakeKey, ks.read().getOrThrow())

        // Không lưu bản rõ.
        val raw = store.data.first()[stringPreferencesKey("api_key_ct")]!!
        assertFalse(raw.contains(fakeKey))
        assertFalse(file.readBytes().toString(Charsets.ISO_8859_1).contains(fakeKey))

        ks.clear()
        assertNull(ks.read().getOrThrow())
    }

    @Test fun `khoa khac thi doc that bai bang Result, khong nem`() = runTestWithScope {
        val file = tmp.newFile("t.preferences_pb").also { it.delete() }
        val store = PreferenceDataStoreFactory.create(scope = scope) { file }
        SecureKeyStore(store, KeyProvider { key }).sunJce().save(fakeKey)

        val other = KeyGenerator.getInstance("AES", "SunJCE").apply { init(256) }.generateKey()
        assertTrue(SecureKeyStore(store, KeyProvider { other }).sunJce().read().isFailure)
    }

    @Test fun `key rong bi tu choi`() = runTestWithScope {
        val file = tmp.newFile("u.preferences_pb").also { it.delete() }
        val ks = SecureKeyStore(PreferenceDataStoreFactory.create(scope = scope) { file }, KeyProvider { key }).sunJce()
        assertTrue(ks.save("   ").isFailure)
    }
}

/** Robolectric nạp provider không ký vào cùng JVM; Oracle JDK từ chối nên test chỉ định SunJCE. */
private fun SecureKeyStore.sunJce() = apply { newCipher = { Cipher.getInstance(SecureKeyStore.TRANSFORMATION, "SunJCE") } }
