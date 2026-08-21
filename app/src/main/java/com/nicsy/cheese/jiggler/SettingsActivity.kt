package com.nicsy.cheese.jiggler

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nicsy.cheese.jiggler.data.Bookmark
import com.nicsy.cheese.jiggler.layout.AppPreferences
import com.nicsy.cheese.jiggler.layout.JigglerGridLayout

class SettingsActivity : ComponentActivity() {

    private lateinit var prefs: AppPreferences

    private lateinit var settingsRootLayout: View
    private lateinit var btnBack: ImageButton
    private lateinit var btnBookmarks: ImageButton

    private lateinit var tvSpeedTitle: TextView
    private lateinit var sbSpeed: SeekBar

    private lateinit var tvBrightnessTitle: TextView
    private lateinit var sbBrightness: SeekBar

    private lateinit var rgTimerType: RadioGroup
    private lateinit var rbTypeInfinite: RadioButton
    private lateinit var rbTypeDuration: RadioButton
    private lateinit var rbTypeTargetTime: RadioButton
    private lateinit var rbTypeTimeRange: RadioButton
    private lateinit var rbTypeExcludeRange: RadioButton

    private lateinit var layoutDuration: LinearLayout
    private lateinit var etDurationMinutes: EditText

    private lateinit var timePicker: TimePicker

    private lateinit var layoutStealth: LinearLayout
    private lateinit var etStealthActive: EditText
    private lateinit var etStealthRest: EditText

    private lateinit var layoutTimeRange: LinearLayout
    private lateinit var timePickerStart: TimePicker
    private lateinit var timePickerEnd: TimePicker
    private lateinit var btnManageExclude: Button
    private lateinit var layoutRemoteControl: View

    private lateinit var rgTileType: RadioGroup
    private lateinit var rbTileBasic: RadioButton
    private lateinit var rbTileGrid: RadioButton
    private lateinit var rbTileVStripe: RadioButton
    private lateinit var rbTileDStripe: RadioButton
    private lateinit var rbTileDots: RadioButton
    private lateinit var rbTileWideStripes: RadioButton

    private lateinit var rgMovePattern: RadioGroup

    private lateinit var btnSave: Button
    private lateinit var btnSavePreset: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = AppPreferences(this)

        settingsRootLayout = findViewById(R.id.settingsRootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(settingsRootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack = findViewById(R.id.btnBack)
        btnBookmarks = findViewById(R.id.btnBookmarks)
        
        btnBack.setOnClickListener { finish() }
        btnBookmarks.setOnClickListener {
            startActivity(android.content.Intent(this, BookmarkActivity::class.java))
        }

        tvSpeedTitle = findViewById(R.id.tvSpeedTitle)
        sbSpeed = findViewById(R.id.sbSpeed)

        tvBrightnessTitle = findViewById(R.id.tvBrightnessTitle)
        sbBrightness = findViewById(R.id.sbBrightness)

        rgTimerType = findViewById(R.id.rgTimerType)
        rbTypeInfinite = findViewById(R.id.rbTypeInfinite)
        rbTypeDuration = findViewById(R.id.rbTypeDuration)
        rbTypeTargetTime = findViewById(R.id.rbTypeTargetTime)
        rbTypeTimeRange = findViewById(R.id.rbTypeTimeRange)
        rbTypeExcludeRange = findViewById(R.id.rbTypeExcludeRange)

        layoutDuration = findViewById(R.id.layoutDuration)
        etDurationMinutes = findViewById(R.id.etDurationMinutes)

        timePicker = findViewById(R.id.timePicker)
        timePicker.setIs24HourView(true)

        layoutStealth = findViewById(R.id.layoutStealth)
        etStealthActive = findViewById(R.id.etStealthActive)
        etStealthRest = findViewById(R.id.etStealthRest)

        layoutTimeRange = findViewById(R.id.layoutTimeRange)
        timePickerStart = findViewById(R.id.timePickerStart)
        timePickerEnd = findViewById(R.id.timePickerEnd)
        btnManageExclude = findViewById(R.id.btnManageExclude)
        layoutRemoteControl = findViewById(R.id.layoutRemoteControl)
        timePickerStart.setIs24HourView(true)
        timePickerEnd.setIs24HourView(true)

        rgTileType = findViewById(R.id.rgTileType)
        rbTileBasic = findViewById(R.id.rbTileBasic)
        rbTileGrid = findViewById(R.id.rbTileGrid)
        rbTileVStripe = findViewById(R.id.rbTileVStripe)
        rbTileDStripe = findViewById(R.id.rbTileDStripe)
        rbTileDots = findViewById(R.id.rbTileDots)
        rbTileWideStripes = findViewById(R.id.rbTileWideStripes)

        val tileButtons = listOf(rbTileBasic, rbTileGrid, rbTileVStripe, rbTileDStripe, rbTileDots, rbTileWideStripes)
        tileButtons.forEach { rb ->
            rb.setOnClickListener {
                tileButtons.forEach { it.isChecked = it == rb }
            }
        }

        rgMovePattern = findViewById(R.id.rgMovePattern)

        btnSave = findViewById(R.id.btnSave)
        btnSavePreset = findViewById(R.id.btnSavePreset)

        sbSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val speed = if (progress < 1) 0.1f else progress / 10f
                tvSpeedTitle.text = getString(R.string.settings_speed_format, speed)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvBrightnessTitle.text = getString(R.string.settings_brightness_format, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        rgTimerType.setOnCheckedChangeListener { _, checkedId ->
            layoutDuration.visibility = if (checkedId == R.id.rbTypeDuration) View.VISIBLE else View.GONE
            timePicker.visibility = if (checkedId == R.id.rbTypeTargetTime) View.VISIBLE else View.GONE
            layoutTimeRange.visibility = if (checkedId == R.id.rbTypeTimeRange) View.VISIBLE else View.GONE
            btnManageExclude.visibility = if (checkedId == R.id.rbTypeExcludeRange) View.VISIBLE else View.GONE
            
            // Immediately persist selection so it's not lost when navigating away
            prefs.timerType = when (checkedId) {
                R.id.rbTypeDuration -> "DURATION"
                R.id.rbTypeTargetTime -> "TARGET_TIME"
                R.id.rbTypeTimeRange -> "TIME_RANGE"
                R.id.rbTypeExcludeRange -> "EXCLUDE_RANGE"
                else -> "INFINITE"
            }
        }

        btnSave.setOnClickListener { saveSettings() }
        btnSavePreset.setOnClickListener { showSavePresetDialog() }
        btnManageExclude.setOnClickListener {
            startActivity(android.content.Intent(this, ExclusionSettingsActivity::class.java))
        }
        layoutRemoteControl.setOnClickListener {
            startActivity(android.content.Intent(this, RemoteControlActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
    }

    private fun showSavePresetDialog() {
        val editText = EditText(this)
        editText.hint = getString(R.string.bookmark_name_hint)
        AlertDialog.Builder(this)
            .setTitle(R.string.bookmark_save_title)
            .setView(editText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = editText.text.toString()
                if (name.isNotEmpty()) {
                    saveAsPreset(name)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun saveAsPreset(name: String) {
        val speed = if (sbSpeed.progress < 1) 0.1f else sbSpeed.progress / 10f
        val timerType = when (rgTimerType.checkedRadioButtonId) {
            R.id.rbTypeDuration -> "DURATION"
            R.id.rbTypeTargetTime -> "TARGET_TIME"
            R.id.rbTypeTimeRange -> "TIME_RANGE"
            R.id.rbTypeExcludeRange -> "EXCLUDE_RANGE"
            else -> "INFINITE"
        }
        val tileType = when {
            rbTileGrid.isChecked -> JigglerGridLayout.TileType.GRID_COMPLEX
            rbTileVStripe.isChecked -> JigglerGridLayout.TileType.STRIPE_HORIZONTAL
            rbTileDStripe.isChecked -> JigglerGridLayout.TileType.STRIPE_DIAGONAL
            rbTileDots.isChecked -> JigglerGridLayout.TileType.DOT_PATTERN
            rbTileWideStripes.isChecked -> JigglerGridLayout.TileType.WIDE_STRIPES
            else -> JigglerGridLayout.TileType.BASIC
        }
        val movePattern = when (rgMovePattern.checkedRadioButtonId) {
            R.id.rbPatternCircle -> JigglerGridLayout.JiggleMode.CIRCLE
            R.id.rbPatternZigzag -> JigglerGridLayout.JiggleMode.ZIGZAG
            R.id.rbPatternMicro -> JigglerGridLayout.JiggleMode.MICRO
            else -> JigglerGridLayout.JiggleMode.BASIC
        }

        val bookmark = Bookmark(
            id = System.currentTimeMillis(),
            name = name,
            speedMultiplier = speed,
            brightnessPercent = sbBrightness.progress,
            timerType = timerType,
            durationMinutes = etDurationMinutes.text.toString().toIntOrNull() ?: 30,
            targetHour = timePicker.hour,
            targetMinute = timePicker.minute,
            startHour = timePickerStart.hour,
            startMinute = timePickerStart.minute,
            endHour = timePickerEnd.hour,
            endMinute = timePickerEnd.minute,
            jiggleMode = movePattern.name,
            tileType = tileType.name,
            isStealthEnabled = prefs.isStealthEnabled,
            stealthActiveSec = etStealthActive.text.toString().toIntOrNull() ?: 10,
            stealthRestSec = etStealthRest.text.toString().toIntOrNull() ?: 30
        )

        prefs.addBookmark(bookmark)
        Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
    }

    private fun loadSettings() {
        val speed = prefs.speedMultiplier
        sbSpeed.progress = (speed * 10).toInt()
        tvSpeedTitle.text = getString(R.string.settings_speed_format, speed)

        val brightness = prefs.brightnessPercent
        sbBrightness.progress = brightness
        tvBrightnessTitle.text = getString(R.string.settings_brightness_format, brightness)

        when (prefs.timerType) {
            "DURATION" -> rgTimerType.check(R.id.rbTypeDuration)
            "TARGET_TIME" -> rgTimerType.check(R.id.rbTypeTargetTime)
            "TIME_RANGE" -> rgTimerType.check(R.id.rbTypeTimeRange)
            "EXCLUDE_RANGE" -> rgTimerType.check(R.id.rbTypeExcludeRange)
            else -> rgTimerType.check(R.id.rbTypeInfinite)
        }
        
        // Timer layout visibility update
        layoutDuration.visibility = if (rbTypeDuration.isChecked) View.VISIBLE else View.GONE
        timePicker.visibility = if (rbTypeTargetTime.isChecked) View.VISIBLE else View.GONE
        layoutTimeRange.visibility = if (rbTypeTimeRange.isChecked) View.VISIBLE else View.GONE
        btnManageExclude.visibility = if (rbTypeExcludeRange.isChecked) View.VISIBLE else View.GONE

        etDurationMinutes.setText(prefs.durationMinutes.toString())
        timePicker.hour = prefs.targetHour
        timePicker.minute = prefs.targetMinute
        
        etStealthActive.setText(prefs.stealthActiveSec.toString())
        etStealthRest.setText(prefs.stealthRestSec.toString())

        timePickerStart.hour = prefs.startHour
        timePickerStart.minute = prefs.startMinute
        timePickerEnd.hour = prefs.endHour
        timePickerEnd.minute = prefs.endMinute

        // 타일 타입 로드
        val tileType = prefs.tileType
        rbTileBasic.isChecked = tileType == JigglerGridLayout.TileType.BASIC
        rbTileGrid.isChecked = tileType == JigglerGridLayout.TileType.GRID_COMPLEX
        rbTileVStripe.isChecked = tileType == JigglerGridLayout.TileType.STRIPE_HORIZONTAL
        rbTileDStripe.isChecked = tileType == JigglerGridLayout.TileType.STRIPE_DIAGONAL
        rbTileDots.isChecked = tileType == JigglerGridLayout.TileType.DOT_PATTERN
        rbTileWideStripes.isChecked = tileType == JigglerGridLayout.TileType.WIDE_STRIPES

        // 움직임 패턴 로드
        val patternGroup = findViewById<RadioGroup>(R.id.rgMovePattern)
        when (prefs.jiggleMode) {
            JigglerGridLayout.JiggleMode.CIRCLE -> patternGroup.check(R.id.rbPatternCircle)
            JigglerGridLayout.JiggleMode.ZIGZAG -> patternGroup.check(R.id.rbPatternZigzag)
            JigglerGridLayout.JiggleMode.MICRO -> patternGroup.check(R.id.rbPatternMicro)
            else -> patternGroup.check(R.id.rbPatternBasic)
        }
    }

    private fun saveSettings() {
        val speed = if (sbSpeed.progress < 1) 0.1f else sbSpeed.progress / 10f
        prefs.speedMultiplier = speed

        prefs.brightnessPercent = sbBrightness.progress

        val timerType = when (rgTimerType.checkedRadioButtonId) {
            R.id.rbTypeDuration -> "DURATION"
            R.id.rbTypeTargetTime -> "TARGET_TIME"
            R.id.rbTypeTimeRange -> "TIME_RANGE"
            R.id.rbTypeExcludeRange -> "EXCLUDE_RANGE"
            else -> "INFINITE"
        }
        prefs.timerType = timerType

        prefs.durationMinutes = etDurationMinutes.text.toString().toIntOrNull() ?: 30
        prefs.targetHour = timePicker.hour
        prefs.targetMinute = timePicker.minute
        
        prefs.stealthActiveSec = etStealthActive.text.toString().toIntOrNull() ?: 10
        prefs.stealthRestSec = etStealthRest.text.toString().toIntOrNull() ?: 30

        prefs.startHour = timePickerStart.hour
        prefs.startMinute = timePickerStart.minute
        prefs.endHour = timePickerEnd.hour
        prefs.endMinute = timePickerEnd.minute

        // 타일 타입 저장
        val tileType = when {
            rbTileGrid.isChecked -> JigglerGridLayout.TileType.GRID_COMPLEX
            rbTileVStripe.isChecked -> JigglerGridLayout.TileType.STRIPE_HORIZONTAL
            rbTileDStripe.isChecked -> JigglerGridLayout.TileType.STRIPE_DIAGONAL
            rbTileDots.isChecked -> JigglerGridLayout.TileType.DOT_PATTERN
            rbTileWideStripes.isChecked -> JigglerGridLayout.TileType.WIDE_STRIPES
            else -> JigglerGridLayout.TileType.BASIC
        }
        prefs.tileType = tileType

        // 움직임 패턴 저장
        val movePattern = when (rgMovePattern.checkedRadioButtonId) {
            R.id.rbPatternCircle -> JigglerGridLayout.JiggleMode.CIRCLE
            R.id.rbPatternZigzag -> JigglerGridLayout.JiggleMode.ZIGZAG
            R.id.rbPatternMicro -> JigglerGridLayout.JiggleMode.MICRO
            else -> JigglerGridLayout.JiggleMode.BASIC
        }
        prefs.jiggleMode = movePattern

        Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
        finish()
    }
}
