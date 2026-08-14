package com.nicsy.cheese.jiggler

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nicsy.cheese.jiggler.R
import com.nicsy.cheese.jiggler.data.ExclusionRange
import com.nicsy.cheese.jiggler.layout.AppPreferences
import com.nicsy.cheese.jiggler.layout.JigglerGridLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private lateinit var mainRootLayout: View
    private lateinit var jigglerGridLayout: JigglerGridLayout
    private lateinit var btnSettings: ImageButton
    private lateinit var fabPlay: FloatingActionButton
    private lateinit var tvStatus: TextView
    private lateinit var swMainStealth: SwitchCompat
    private lateinit var layoutMainStealth: View

    private lateinit var prefs: AppPreferences

    private var isRunning = false
    private var lastBackPressedTime: Long = 0
    private var countDownTimer: CountDownTimer? = null

    private val rangeHandler = Handler(Looper.getMainLooper())
    private var rangeRunnable: Runnable? = null

    private val stealthHandler = Handler(Looper.getMainLooper())
    private var stealthRunnable: Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = AppPreferences(this)

        mainRootLayout = findViewById(R.id.mainRootLayout)
        jigglerGridLayout = findViewById(R.id.jigglerGridLayout)
        btnSettings = findViewById(R.id.btnSettings)
        fabPlay = findViewById(R.id.fabPlay)
        tvStatus = findViewById(R.id.tvStatus)
        swMainStealth = findViewById(R.id.swMainStealth)
        layoutMainStealth = findViewById(R.id.layoutMainStealth)

        swMainStealth.isChecked = prefs.isStealthEnabled
        swMainStealth.setOnCheckedChangeListener { _, isChecked ->
            prefs.isStealthEnabled = isChecked
        }

        ViewCompat.setOnApplyWindowInsetsListener(mainRootLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            mainRootLayout.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        fabPlay.setOnClickListener {
            if (isRunning) {
                stopJiggler(getString(R.string.stop_by_user))
                // 서서히 나타남 (Fade In)
                btnSettings.visibility = View.VISIBLE
                layoutMainStealth.visibility = View.VISIBLE
                btnSettings.alpha = 0f
                layoutMainStealth.alpha = 0f
                btnSettings.animate().alpha(1f).setDuration(300)
                layoutMainStealth.animate().alpha(1f).setDuration(300)
            } else {
                startJiggler()
                // 서서히 사라짐 (Fade Out)
                btnSettings.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction { btnSettings.visibility = View.GONE }
                layoutMainStealth.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction { layoutMainStealth.visibility = View.GONE }
            }
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // 뒤로가기 두 번 눌러 종료 처리
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (System.currentTimeMillis() - lastBackPressedTime < 2000) {
                    finish()
                } else {
                    lastBackPressedTime = System.currentTimeMillis()
                    val snackbar = Snackbar.make(mainRootLayout, getString(R.string.exit_double_tap), Snackbar.LENGTH_SHORT)
                    snackbar.setBackgroundTint(ContextCompat.getColor(this@MainActivity, R.color.cheese_primary))
                    snackbar.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                    
                    val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    snackbar.show()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onResume() {
        super.onResume()
        // 설정이나 북마크에서 돌아왔을 때 스텔스 스위치 상태 업데이트
        swMainStealth.isChecked = prefs.isStealthEnabled
    }

    private fun startJiggler() {
        isRunning = true
        fabPlay.setImageResource(R.drawable.ico_app_stop)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 화면 밝기 고정 (0.0 ~ 1.0)
        val params = window.attributes
        params.screenBrightness = prefs.brightnessPercent / 100f
        window.attributes = params

        when (prefs.timerType) {
            "DURATION" -> {
                val durationMs = prefs.durationMinutes * 60 * 1000L
                startTimer(durationMs)
                startWork()
            }
            "TARGET_TIME" -> {
                val now = Calendar.getInstance()
                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, prefs.targetHour)
                    set(Calendar.MINUTE, prefs.targetMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (target.before(now)) target.add(Calendar.DAY_OF_YEAR, 1)

                val durationMs = target.timeInMillis - now.timeInMillis
                startTimer(durationMs)
                startWork()
            }
            "TIME_RANGE" -> {
                tvStatus.visibility = View.VISIBLE
                runTimeRangeChecker()
            }
            "EXCLUDE_RANGE" -> {
                tvStatus.visibility = View.VISIBLE
                runTimeRangeChecker()
            }
            else -> { // INFINITE
                tvStatus.visibility = View.GONE
                startWork()
            }
        }
    }

    private fun startWork() {
        if (!isRunning) return
        if (swMainStealth.isChecked) {
            runStealthLoop(isPhaseActive = true)
        } else {
            startAnimation()
        }
    }

    private fun stopWork() {
        stealthRunnable?.let { stealthHandler.removeCallbacks(it) }
        stopAnimation()
    }

    // 4. 지정한 시간 범위 동안 작동 체크 루프
    private fun runTimeRangeChecker() {
        if (!isRunning) return

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        if (prefs.timerType == "EXCLUDE_RANGE") {
            val exclusionRanges = prefs.getExclusionRanges()
            var activeExclusion: ExclusionRange? = null
            
            for (range in exclusionRanges) {
                val start = range.startHour * 60 + range.startMinute
                val end = range.endHour * 60 + range.endMinute
                
                val isExcluded = if (start <= end) {
                    currentMinutes in start until end
                } else {
                    currentMinutes >= start || currentMinutes < end
                }
                
                if (isExcluded) {
                    activeExclusion = range
                    break
                }
            }

            if (activeExclusion != null) {
                tvStatus.text = getString(R.string.status_paused_exclude, 
                    activeExclusion.startHour, activeExclusion.startMinute, 
                    activeExclusion.endHour, activeExclusion.endMinute)
                stopWork()
            } else {
                tvStatus.text = getString(R.string.status_running_exclude, exclusionRanges.size)
                if (jigglerGridLayout.visibility != View.VISIBLE && stealthRunnable == null) {
                    startWork()
                }
            }
        } else {
            // Existing TIME_RANGE logic
            val startMinutes = prefs.startHour * 60 + prefs.startMinute
            val endMinutes = prefs.endHour * 60 + prefs.endMinute

            val isInRange = if (startMinutes <= endMinutes) {
                currentMinutes in startMinutes until endMinutes
            } else {
                // 자정을 넘기는 시간 설정인 경우 (예: 22:00 ~ 06:00)
                currentMinutes >= startMinutes || currentMinutes < endMinutes
            }

            if (isInRange) {
                tvStatus.text = getString(
                    R.string.status_running_range,
                    prefs.startHour, prefs.startMinute, prefs.endHour, prefs.endMinute
                )
                // 아직 작동 전이라면 시작
                if (jigglerGridLayout.visibility != View.VISIBLE && stealthRunnable == null) {
                    startWork()
                }
            } else {
                tvStatus.text = getString(
                    R.string.status_waiting_range,
                    prefs.startHour, prefs.startMinute
                )
                stopWork()
            }
        }

        rangeRunnable = Runnable { runTimeRangeChecker() }
        rangeHandler.postDelayed(rangeRunnable!!, 1000L)
    }

    private fun runStealthLoop(isPhaseActive: Boolean) {
        if (!isRunning) return

        // 시간 범위 모드인 경우 범위 밖이면 중단
        if (prefs.timerType == "TIME_RANGE") {
            val now = Calendar.getInstance()
            val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val startMinutes = prefs.startHour * 60 + prefs.startMinute
            val endMinutes = prefs.endHour * 60 + prefs.endMinute
            val isInRange = if (startMinutes <= endMinutes) {
                currentMinutes in startMinutes until endMinutes
            } else {
                currentMinutes >= startMinutes || currentMinutes < endMinutes
            }
            if (!isInRange) return
        }

        if (isPhaseActive) {
            tvStatus.text = getString(R.string.status_stealth_active, prefs.stealthActiveSec)
            startAnimation()

            stealthRunnable = Runnable {
                runStealthLoop(isPhaseActive = false)
            }
            stealthHandler.postDelayed(stealthRunnable!!, prefs.stealthActiveSec * 1000L)
        } else {
            tvStatus.text = getString(R.string.status_stealth_rest, prefs.stealthRestSec)
            stopAnimation()

            stealthRunnable = Runnable {
                runStealthLoop(isPhaseActive = true)
            }
            stealthHandler.postDelayed(stealthRunnable!!, prefs.stealthRestSec * 1000L)
        }
    }

    private fun startAnimation() {
        jigglerGridLayout.visibility = View.VISIBLE
        jigglerGridLayout.startJiggle(prefs.jiggleMode, prefs.speedMultiplier, prefs.tileType)
    }

    private fun stopAnimation() {
        jigglerGridLayout.stopJiggle()
        jigglerGridLayout.visibility = View.INVISIBLE
    }

    private fun startTimer(millisInFuture: Long) {
        tvStatus.visibility = View.VISIBLE
        countDownTimer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSec = millisUntilFinished / 1000
                val min = totalSec / 60
                val sec = totalSec % 60
                tvStatus.text = getString(R.string.status_remaining_time, min, sec)
            }

            override fun onFinish() {
                stopJiggler(getString(R.string.stop_timer_finished))
            }
        }.start()
    }

    private fun stopJiggler(message: String) {
        isRunning = false
        fabPlay.setImageResource(R.drawable.ico_app_play)
        // 화면 켜짐 유지 해제 (정지할 때만 풀림)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 화면 밝기 고정 해제 (시스템 설정값으로 복구)
        val params = window.attributes
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = params

        countDownTimer?.cancel()
        countDownTimer = null

        rangeRunnable?.let { rangeHandler.removeCallbacks(it) }
        stealthRunnable?.let { stealthHandler.removeCallbacks(it) }
        rangeRunnable = null
        stealthRunnable = null

        stopAnimation()
        tvStatus.visibility = View.GONE

        if (message.isNotEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopJiggler("")
    }
}
