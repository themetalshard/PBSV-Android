package com.metalshard.hyperion.data

import com.metalshard.hyperion.model.ScheduleEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup

class ScheduleRepository {
    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun fetchSchedule(): Map<String, List<ScheduleEvent>> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect("https://pinewoodbuilders.org/info/schedule").get()
            val scriptElement = doc.selectFirst("script#schedule-data") ?: return@withContext emptyMap()
            val rawJson = scriptElement.data()
            val rawMap: Map<String, Map<String, ScheduleEvent>> = jsonConfig.decodeFromString(rawJson)
            rawMap.mapValues { it.value.values.toList() }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}