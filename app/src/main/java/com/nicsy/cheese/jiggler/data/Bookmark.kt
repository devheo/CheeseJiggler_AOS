package com.nicsy.cheese.jiggler.data

data class Bookmark(
    val id: Long,
    val name: String,
    val speedMultiplier: Float,
    val brightnessPercent: Int,
    val timerType: String,
    val durationMinutes: Int,
    val targetHour: Int,
    val targetMinute: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val jiggleMode: String,
    val tileType: String,
    val isStealthEnabled: Boolean,
    val stealthActiveSec: Int,
    val stealthRestSec: Int
)
