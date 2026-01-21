package com.metalshard.hyperion.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleEvent(
    @SerialName("Time") val time: Long,
    @SerialName("Duration") val duration: Long,
    @SerialName("EventType") val eventType: String,
    @SerialName("Trainer") val trainer: String? = null,
    @SerialName("Notes") val notes: String? = null,
    @SerialName("TrainingID") val uuid: String? = null,
    @SerialName("EventColor") val eventColor: List<Int>? = null,
    @SerialName("TrainerId") val trainerId: Long? = null,
    @SerialName("TrainerCommsId") val discordId: String? = null
)