package com.cali

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cali.core.ui.theme.CaliTheme
import com.cali.feature.main.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @javax.inject.Inject lateinit var deepLinks: com.cali.feature.main.DeepLinks

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handle(intent)
        setContent {
            CaliTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    AppNavHost()
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    private fun handle(intent: android.content.Intent?) {
        intent?.getStringExtra(EXTRA_OPEN_WEEKLY)?.let { deepLinks.weekly.value = it }
    }

    companion object {
        /** Thông báo tổng kết tuần → S35 của tuần đó. */
        const val EXTRA_OPEN_WEEKLY = "open_weekly"
    }
}
