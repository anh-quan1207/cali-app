package com.cali.core.data.prefs

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cali.core.calc.MealBoundary
import com.cali.core.calc.MealTypeResolver
import com.cali.core.calc.model.MacroPct
import com.cali.core.calc.model.MealType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalTime

class SettingsRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun TestScopeHolder.repo(file: File) =
        SettingsRepository(PreferenceDataStoreFactory.create(scope = scope) { file })

    @Test fun `gia tri mac dinh dung 04 §3`() = runTestWithScope {
        val s = repo(tmp.newFile("a.preferences_pb").also { it.delete() }).current()
        assertFalse(s.onboardingDone)
        assertEquals("neuralwatt", s.aiProvider)
        assertEquals("kimi-k3.1-pro", s.aiModelVision)
        assertEquals("deepseek-v4.1-flash", s.aiModelChat)
        assertNull(s.systemPromptOverride)
        assertEquals(1, s.systemPromptDefaultVersion)
        assertEquals("", s.personalNotes)
        assertNull(s.macroPctOverride)
        assertNull(s.kcalOverride)
        assertTrue(s.waterEnabled)
        assertEquals(LocalTime.of(7, 0), s.waterStart)
        assertEquals(LocalTime.of(22, 0), s.waterEnd)
        assertEquals(90, s.waterIntervalMin)
        assertEquals(250, s.waterCupSmallMl)
        assertEquals(500, s.waterCupLargeMl)
        assertEquals(MealTypeResolver.DEFAULT, s.mealTimes)
        assertTrue(s.saveThumbnails)
        assertTrue(s.weeklyNotifyEnabled)
        assertEquals(LocalTime.of(8, 0), s.weeklyNotifyTime)
        assertTrue(s.weeklyAiAuto)
    }

    @Test fun `ghi roi doc lai, ke ca JSON va xoa ghi de`() = runTestWithScope {
        val file = tmp.newFile("b.preferences_pb").also { it.delete() }
        val repo = repo(file)
        val meals = listOf(MealBoundary(LocalTime.of(6, 0), MealType.BREAKFAST), MealBoundary(LocalTime.of(20, 0), MealType.SNACK))
        val r = repo.update {
            it.copy(
                onboardingDone = true,
                kcalOverride = 1400,
                macroPctOverride = MacroPct(30, 40, 30),
                mealTimes = meals,
                waterEnd = LocalTime.of(21, 30),
            )
        }
        assertTrue(r.isSuccess)

        val s = repo.current()
        assertTrue(s.onboardingDone)
        assertEquals(1400, s.kcalOverride)
        assertEquals(MacroPct(30, 40, 30), s.macroPctOverride)
        assertEquals(meals, s.mealTimes)
        assertEquals(LocalTime.of(21, 30), s.waterEnd)

        repo.update { it.copy(kcalOverride = null, macroPctOverride = null) }
        assertNull(repo.current().kcalOverride)
        assertNull(repo.current().macroPctOverride)
    }

    @Test fun `loi trong update tra Result failure, khong nem ra ngoai`() = runTestWithScope {
        val repo = repo(tmp.newFile("c.preferences_pb").also { it.delete() })
        val r = repo.update { error("hỏng") }
        assertTrue(r.isFailure)
    }

    @Test fun `ban cai cu da luu model Claude thi doc ra model Neuralwatt`() = runTestWithScope {
        val file = tmp.newFile("d.preferences_pb").also { it.delete() }
        val store = PreferenceDataStoreFactory.create(scope = scope) { file }
        store.edit {
            it[stringPreferencesKey("ai_provider")] = "anthropic"
            it[stringPreferencesKey("ai_model_vision")] = "claude-sonnet-5-5"
            it[stringPreferencesKey("ai_model_chat")] = "claude-haiku-5-5"
        }
        val s = SettingsRepository(store).current()
        assertEquals("neuralwatt", s.aiProvider)
        assertEquals("kimi-k3.1-pro", s.aiModelVision)
        assertEquals("deepseek-v4.1-flash", s.aiModelChat)
    }

    @Test fun `model Neuralwatt nguoi dung chon thi giu nguyen`() = runTestWithScope {
        val repo = repo(tmp.newFile("e.preferences_pb").also { it.delete() })
        repo.update { it.copy(aiModelVision = "qwen3.6-35b") }.getOrThrow()
        assertEquals("qwen3.6-35b", repo.current().aiModelVision)
    }
}

/** runTest với scope riêng cho DataStore, huỷ sau test. */
class TestScopeHolder(val scope: kotlinx.coroutines.CoroutineScope)

fun runTestWithScope(block: suspend TestScopeHolder.() -> Unit) = runTest {
    val job = kotlinx.coroutines.SupervisorJob()
    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + job)
    try {
        TestScopeHolder(scope).block()
    } finally {
        job.cancel()
    }
}
