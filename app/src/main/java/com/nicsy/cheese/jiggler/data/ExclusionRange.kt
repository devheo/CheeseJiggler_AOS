package com.nicsy.cheese.jiggler.data

data class ExclusionRange(
    val id: Long,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
)
