package com.nicsy.cheese.jiggler.layout

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nicsy.cheese.jiggler.data.Bookmark
import com.nicsy.cheese.jiggler.data.ExclusionRange

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("jiggler_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_JIGGLE_MODE = "key_jiggle_mode"
        private const val KEY_SPEED = "key_speed"
        private const val KEY_BRIGHTNESS = "key_brightness"
        private const val KEY_TIMER_TYPE = "key_timer_type"
        private const val KEY_DURATION_MINUTES = "key_duration_minutes"
        private const val KEY_TARGET_HOUR = "key_target_hour"
        private const val KEY_TARGET_MINUTE = "key_target_minute"
        private const val KEY_STEALTH_ENABLED = "key_stealth_enabled"
        private const val KEY_STEALTH_ACTIVE_SEC = "key_stealth_active_sec"
        private const val KEY_STEALTH_REST_SEC = "key_stealth_rest_sec"
        private const val KEY_START_HOUR = "key_start_hour"
        private const val KEY_START_MINUTE = "key_start_minute"
        private const val KEY_END_HOUR = "key_end_hour"
        private const val KEY_END_MINUTE = "key_end_minute"
        private const val KEY_TILE_TYPE = "key_tile_type"
        private const val KEY_IS_FIRST_RUN = "key_is_first_run"
        private const val KEY_BOOKMARKS = "key_bookmarks"
        private const val KEY_EXCLUSION_RANGES = "key_exclusion_ranges"
    }

    private val gson = Gson()

    var isFirstRun: Boolean
        get() = prefs.getBoolean(KEY_IS_FIRST_RUN, true)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_FIRST_RUN, value).apply()
        }

    var jiggleMode: JigglerGridLayout.JiggleMode
        get() {
            val modeName = prefs.getString(KEY_JIGGLE_MODE, JigglerGridLayout.JiggleMode.BASIC.name)
            return try {
                JigglerGridLayout.JiggleMode.valueOf(modeName ?: JigglerGridLayout.JiggleMode.BASIC.name)
            } catch (e: Exception) {
                JigglerGridLayout.JiggleMode.BASIC
            }
        }
        set(value) {
            prefs.edit().putString(KEY_JIGGLE_MODE, value.name).apply()
        }

    var tileType: JigglerGridLayout.TileType
        get() {
            val typeName = prefs.getString(KEY_TILE_TYPE, JigglerGridLayout.TileType.BASIC.name)
            return try {
                JigglerGridLayout.TileType.valueOf(typeName ?: JigglerGridLayout.TileType.BASIC.name)
            } catch (e: Exception) {
                JigglerGridLayout.TileType.BASIC
            }
        }
        set(value) {
            prefs.edit().putString(KEY_TILE_TYPE, value.name).apply()
        }

    var speedMultiplier: Float
        get() = prefs.getFloat(KEY_SPEED, 0.5f)
        set(value) {
            prefs.edit().putFloat(KEY_SPEED, value).apply()
        }

    var brightnessPercent: Int
        get() = prefs.getInt(KEY_BRIGHTNESS, 50)
        set(value) {
            prefs.edit().putInt(KEY_BRIGHTNESS, value).apply()
        }

    var timerType: String
        get() = prefs.getString(KEY_TIMER_TYPE, "INFINITE") ?: "INFINITE"
        set(value) {
            prefs.edit().putString(KEY_TIMER_TYPE, value).apply()
        }

    var durationMinutes: Int
        get() = prefs.getInt(KEY_DURATION_MINUTES, 30)
        set(value) {
            prefs.edit().putInt(KEY_DURATION_MINUTES, value).apply()
        }

    var targetHour: Int
        get() = prefs.getInt(KEY_TARGET_HOUR, 18)
        set(value) {
            prefs.edit().putInt(KEY_TARGET_HOUR, value).apply()
        }

    var targetMinute: Int
        get() = prefs.getInt(KEY_TARGET_MINUTE, 0)
        set(value) {
            prefs.edit().putInt(KEY_TARGET_MINUTE, value).apply()
        }

    var isStealthEnabled: Boolean
        get() = prefs.getBoolean(KEY_STEALTH_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_STEALTH_ENABLED, value).apply()
        }

    var stealthActiveSec: Int
        get() = prefs.getInt(KEY_STEALTH_ACTIVE_SEC, 10)
        set(value) {
            prefs.edit().putInt(KEY_STEALTH_ACTIVE_SEC, value).apply()
        }

    var stealthRestSec: Int
        get() = prefs.getInt(KEY_STEALTH_REST_SEC, 30)
        set(value) {
            prefs.edit().putInt(KEY_STEALTH_REST_SEC, value).apply()
        }

    var startHour: Int
        get() = prefs.getInt(KEY_START_HOUR, 9)
        set(value) {
            prefs.edit().putInt(KEY_START_HOUR, value).apply()
        }

    var startMinute: Int
        get() = prefs.getInt(KEY_START_MINUTE, 0)
        set(value) {
            prefs.edit().putInt(KEY_START_MINUTE, value).apply()
        }

    var endHour: Int
        get() = prefs.getInt(KEY_END_HOUR, 18)
        set(value) {
            prefs.edit().putInt(KEY_END_HOUR, value).apply()
        }

    var endMinute: Int
        get() = prefs.getInt(KEY_END_MINUTE, 0)
        set(value) {
            prefs.edit().putInt(KEY_END_MINUTE, value).apply()
        }

    fun getBookmarks(): List<Bookmark> {
        val json = prefs.getString(KEY_BOOKMARKS, null) ?: return emptyList()
        val type = object : TypeToken<List<Bookmark>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveBookmarks(bookmarks: List<Bookmark>) {
        val json = gson.toJson(bookmarks)
        prefs.edit().putString(KEY_BOOKMARKS, json).apply()
    }

    fun addBookmark(bookmark: Bookmark) {
        val list = getBookmarks().toMutableList()
        list.add(bookmark)
        saveBookmarks(list)
    }

    fun removeBookmark(id: Long) {
        val list = getBookmarks().toMutableList()
        list.removeAll { it.id == id }
        saveBookmarks(list)
    }

    fun getExclusionRanges(): List<ExclusionRange> {
        val json = prefs.getString(KEY_EXCLUSION_RANGES, null) ?: return emptyList()
        val type = object : TypeToken<List<ExclusionRange>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveExclusionRanges(ranges: List<ExclusionRange>) {
        val json = gson.toJson(ranges)
        prefs.edit().putString(KEY_EXCLUSION_RANGES, json).apply()
    }

    fun addExclusionRange(range: ExclusionRange) {
        val list = getExclusionRanges().toMutableList()
        list.add(range)
        saveExclusionRanges(list)
    }

    fun removeExclusionRange(id: Long) {
        val list = getExclusionRanges().toMutableList()
        list.removeAll { it.id == id }
        saveExclusionRanges(list)
    }
}
