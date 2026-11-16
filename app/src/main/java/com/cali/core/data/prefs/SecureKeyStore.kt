package com.cali.core.data.prefs

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/** Nguồn khóa AES; tách ra để unit test dùng khóa JCE thường thay cho Android Keystore. */
fun interface KeyProvider {
    fun key(): SecretKey
}

/** Khóa AES-256-GCM trong Android Keystore, không xuất được (06 §5). */
class AndroidKeystoreKeyProvider @Inject constructor() : KeyProvider {
    override fun key(): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (ks.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        gen.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return gen.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "cali_api_key"
    }
}

/**
 * Lưu API key đã mã hoá: ciphertext + IV (Base64) trong DataStore `secure_prefs`.
 * Chỉ giải mã ngay trước khi gọi API; không log, không giữ trong biến toàn cục.
 */
@Singleton
class SecureKeyStore @Inject constructor(
    @SecureStore private val store: DataStore<Preferences>,
    private val keyProvider: KeyProvider,
) {
    /** Cho phép test chọn provider JCE cụ thể; app dùng provider mặc định. */
    internal var newCipher: () -> Cipher = { Cipher.getInstance(TRANSFORMATION) }

    val hasKey: Flow<Boolean> = store.data.map { it[CIPHERTEXT] != null }

    suspend fun save(apiKey: String): Result<Unit> = runCatching {
        require(apiKey.isNotBlank()) { "API key rỗng" }
        val cipher = newCipher()
        cipher.init(Cipher.ENCRYPT_MODE, keyProvider.key())
        val ct = cipher.doFinal(apiKey.trim().toByteArray(Charsets.UTF_8))
        val b64 = Base64.getEncoder()
        store.edit {
            it[CIPHERTEXT] = b64.encodeToString(ct)
            it[IV] = b64.encodeToString(cipher.iv)
        }
    }

    /** null nếu chưa có key. */
    suspend fun read(): Result<String?> = runCatching {
        val prefs = store.data.first()
        val ct = prefs[CIPHERTEXT] ?: return@runCatching null
        val iv = prefs[IV] ?: return@runCatching null
        val b64 = Base64.getDecoder()
        val cipher = newCipher()
        cipher.init(Cipher.DECRYPT_MODE, keyProvider.key(), GCMParameterSpec(TAG_BITS, b64.decode(iv)))
        String(cipher.doFinal(b64.decode(ct)), Charsets.UTF_8)
    }

    suspend fun clear(): Result<Unit> = runCatching {
        store.edit {
            it.remove(CIPHERTEXT)
            it.remove(IV)
        }
    }

    internal companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128
        val CIPHERTEXT = stringPreferencesKey("api_key_ct")
        val IV = stringPreferencesKey("api_key_iv")
    }
}
