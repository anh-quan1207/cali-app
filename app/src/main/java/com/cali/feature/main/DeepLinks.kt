package com.cali.feature.main

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Mở màn từ thông báo: tuần cần mở ở S35 (ISO yyyy-MM-dd), AppNavHost đọc rồi xoá. */
@Singleton
class DeepLinks @Inject constructor() {
    val weekly = MutableStateFlow<String?>(null)
}
