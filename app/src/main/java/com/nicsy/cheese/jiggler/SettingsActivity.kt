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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nicsy.cheese.jiggler.layout.AppPreferences
import com.nicsy.cheese.jiggler.layout.JigglerGridLayout

class SettingsActivity : ComponentActivity() {

    private lateinit var prefs: AppPreferences

    private lateinit var settingsRootLayout: View
    private lateinit var btnBack: ImageButton

    private lateinit var tvSpeedTitle: TextView
    private lateinit var sbSpeed: SeekBar

    private lateinit var tvBrightnessTitle: TextView
    private lateinit var sbBrightness: SeekBar

    private lateinit var rgTimerType: RadioGroup
    private lateinit var rbTypeInfinite: RadioButton
    private lateinit var rbTypeDuration: RadioButton
    private lateinit var rbTypeTargetTime: RadioButton
    private lateinit var rbTypeTimeRange: RadioButton

    private lateinit var layoutDuration: LinearLayout
    private lateinit var etDurationMinutes: EditText

    private lateinit var timePicker: TimePicker

    private lateinit var layoutStealth: LinearLayout
    private lateinit var etStealthActive: EditText
    private lateinit var etStealthRest: EditText

    private lateinit var layoutTimeRange: LinearLayout
    private lateinit var timePickerStart: TimePicker
    private lateinit var timePickerEnd: TimePicker

    private lateinit var rgTileType: RadioGroup
    private lateinit var rbTileBasic: RadioButton
    private lateinit var rbTileGrid: RadioButton
    private lateinit var rbTileVStripe: RadioButton
    private lateinit var rbTileDStripe: RadioButton
    private lateinit var rbTileDots: RadioButton

    private lateinit var rgMovePattern: RadioGroup

    private lateinit var btnSave: Button

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

        // Status Bar 처리
        ViewCompat.setOnApplyWindowInsetsListener(settingsRootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack.setOnClickListener { finish() }

        tvSpeedTitle = findViewById(R.id.tvSpeedTitle)
        sbSpeed = findViewById(R.id.sbSpeed)

        tvBrightnessTitle = findViewById(R.id.tvBrightnessTitle)
        sbBrightness = findViewById(R.id.sbBrightness)

        rgTimerType = findViewById(R.id.rgTimerType)
        rbTypeInfinite = findViewById(R.id.rbTypeInfinite)
        rbTypeDuration = findViewById(R.id.rbTypeDuration)
        rbTypeTargetTime = findViewById(R.id.rbTypeTargetTime)
        rbTypeTimeRange = findViewById(R.id.rbTypeTimeRange)

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
        timePickerStart.setIs24HourView(true)
        timePickerEnd.setIs24HourView(true)

        rgTileType = findViewById(R.id.rgTileType)
        rbTileBasic = findViewById(R.id.rbTileBasic)
        rbTileGrid = findViewById(R.id.rbTileGrid)
        rbTileVStripe = findViewById(R.id.rbTileVStripe)
        rbTileDStripe = findViewById(R.id.rbTileDStripe)
        rbTileDots = findViewById(R.id.rbTileDots)

        val tileButtons = listOf(rbTileBasic, rbTileGrid, rbTileVStripe, rbTileDStripe, rbTileDots)
        tileButtons.forEach { rb ->
            rb.setOnClickListener {
                tileButtons.forEach { it.isChecked = it == rb }
            }
        }

        rgMovePattern = findViewById(R.id.rgMovePattern)

        btnSave = findViewById(R.id.btnSave)

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
        }

        loadSettings()

        btnSave.setOnClickListener { saveSettings() }
    }

    private fun loadSettings() {
        val speed = prefs.speedMultiplier
        sbSpeed.progress = (speed * 10).toInt()
        tvSpeedTitle.text = getString(R.string.settings_speed_format, speed)

        val brightness = prefs.brightnessPercent
        sbBrightness.progress = brightness
        tvBrightnessTitle.text = getString(R.string.settings_brightness_format, brightness)

        when (prefs.timerType) {
            "DURATION" -> rbTypeDuration.isChecked = true
            "TARGET_TIME" -> rbTypeTargetTime.isChecked = true
            "TIME_RANGE" -> rbTypeTimeRange.isChecked = true
            else -> rbTypeInfinite.isChecked = true
        }

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
        when (prefs.tileType) {
            JigglerGridLayout.TileType.GRID_COMPLEX -> rbTileGrid.isChecked = true
            JigglerGridLayout.TileType.STRIPE_VERTICAL -> rbTileVStripe.isChecked = true
            JigglerGridLayout.TileType.STRIPE_DIAGONAL -> rbTileDStripe.isChecked = true
            JigglerGridLayout.TileType.DOT_PATTERN -> rbTileDots.isChecked = true
            else -> rbTileBasic.isChecked = true
        }

        // 움직임 패턴 로드
        when (prefs.jiggleMode) {
            JigglerGridLayout.JiggleMode.CIRCLE -> findViewById<RadioButton>(R.id.rbPatternCircle).isChecked = true
            JigglerGridLayout.JiggleMode.ZIGZAG -> findViewById<RadioButton>(R.id.rbPatternZigzag).isChecked = true
            JigglerGridLayout.JiggleMode.MICRO -> findViewById<RadioButton>(R.id.rbPatternMicro).isChecked = true
            else -> findViewById<RadioButton>(R.id.rbPatternBasic).isChecked = true
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
            rbTileVStripe.isChecked -> JigglerGridLayout.TileType.STRIPE_VERTICAL
            rbTileDStripe.isChecked -> JigglerGridLayout.TileType.STRIPE_DIAGONAL
            rbTileDots.isChecked -> JigglerGridLayout.TileType.DOT_PATTERN
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
