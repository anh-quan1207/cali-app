package com.cali.core.data.prefs

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Round-trip với khóa Android Keystore thật (06 §5). Chạy bằng connectedDebugAndroidTest. */
@RunWith(AndroidJUnit4::class)
class SecureKeyStoreDeviceTest {
    @Test fun roundTripWithAndroidKeystore() = runTest {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(ctx.cacheDir, "secure_test_${System.nanoTime()}.preferences_pb")
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        try {
            val ks = SecureKeyStore(PreferenceDataStoreFactory.create(scope = scope) { file }, AndroidKeystoreKeyProvider())
            ks.save("sk-test-device-0000").getOrThrow()
            assertEquals("sk-test-device-0000", ks.read().getOrThrow())
            ks.clear().getOrThrow()
        } finally {
            scope.cancel()
            file.delete()
        }
    }
}
