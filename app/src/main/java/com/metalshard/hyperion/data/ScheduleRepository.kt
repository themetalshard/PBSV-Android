package com.metalshard.hyperion.data

import com.metalshard.hyperion.model.ScheduleEvent
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class ScheduleRepository {
    private val url = "https://pbsv.themetalshard.space/schedule.json"
    private val gson = Gson()

    suspend fun fetchSchedule(): Map<String, List<ScheduleEvent>> = withContext(Dispatchers.IO) {
        try {
            val jsonText = URL(url).readText()

            // 1. Parse into a nested map: Group -> (UUID -> Event)
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Map<String, ScheduleEvent>>>() {}.type
            val nestedData: Map<String, Map<String, ScheduleEvent>> = gson.fromJson(jsonText, type)

            // 2. Flatten the inner map so it's just Group -> List of Events
            nestedData.mapValues { entry ->
                entry.value.values.toList().sortedBy { it.time }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }
}