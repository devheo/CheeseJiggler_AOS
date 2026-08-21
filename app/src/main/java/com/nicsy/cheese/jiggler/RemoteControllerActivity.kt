package com.nicsy.cheese.jiggler

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.nicsy.cheese.jiggler.layout.AppPreferences
import com.nicsy.cheese.jiggler.remote.FirebaseControllerManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RemoteControllerActivity : ComponentActivity() {

    private lateinit var prefs: AppPreferences
    private lateinit var controllerManager: FirebaseControllerManager

    private lateinit var layoutConnect: LinearLayout
    private lateinit var layoutControl: LinearLayout
    private lateinit var etFriendCode: EditText
    private lateinit var btnConnect: MaterialButton

    private lateinit var tvDeviceStatus: TextView
    private lateinit var tvForegroundStatus: TextView
    private lateinit var tvLastSeen: TextView
    private lateinit var tvWorkStatus: TextView
    private lateinit var layoutDetailedStatus: LinearLayout
    private lateinit var tvStatusDetails: TextView
    private lateinit var btnStart: MaterialButton
    private lateinit var btnStop: MaterialButton
    private lateinit var btnCheckStatus: MaterialButton
    private lateinit var btnDisconnect: MaterialButton
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_controller)

        prefs = AppPreferences(this)
        controllerManager = FirebaseControllerManager()

        initViews()
        setupWindowInsets()

        val savedUid = prefs.remoteUid
        if (savedUid != null) {
            showControlMode(savedUid)
        } else {
            showConnectMode()
        }
    }

    private fun initViews() {
        layoutConnect = findViewById(R.id.layoutConnect)
        layoutControl = findViewById(R.id.layoutControl)
        etFriendCode = findViewById(R.id.etFriendCode)
        btnConnect = findViewById(R.id.btnConnect)

        tvDeviceStatus = findViewById(R.id.tvDeviceStatus)
        tvForegroundStatus = findViewById(R.id.tvForegroundStatus)
        tvLastSeen = findViewById(R.id.tvLastSeen)
        tvWorkStatus = findViewById(R.id.tvWorkStatus)
        layoutDetailedStatus = findViewById(R.id.layoutDetailedStatus)
        tvStatusDetails = findViewById(R.id.tvStatusDetails)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnCheckStatus = findViewById(R.id.btnCheckStatus)
        btnDisconnect = findViewById(R.id.btnDisconnect)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        btnConnect.setOnClickListener {
            val code = etFriendCode.text.toString()
            if (code.length == 6) {
                performConnect(code)
            }
        }

        btnDisconnect.setOnClickListener {
            prefs.remoteUid = null
            controllerManager.stopListening()
            showConnectMode()
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.controllerRootLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showConnectMode() {
        layoutConnect.visibility = View.VISIBLE
        layoutControl.visibility = View.GONE
    }

    private fun showControlMode(uid: String) {
        layoutConnect.visibility = View.GONE
        layoutControl.visibility = View.VISIBLE

        controllerManager.startListening(uid, object : FirebaseControllerManager.DeviceStatusCallback {
            override fun onStatusChanged(status: String, isOnline: Boolean, lastSeen: Long, mode: String?, speed: Float?, tileType: String?, isForeground: Boolean?) {
                updateUi(status, isOnline, lastSeen, mode, speed, tileType, isForeground)
            }

            override fun onError(message: String) {
                Toast.makeText(this@RemoteControllerActivity, message, Toast.LENGTH_SHORT).show()
                if (message == "Device not found") {
                    prefs.remoteUid = null
                    showConnectMode()
                }
            }
        })

        btnStart.setOnClickListener {
            lifecycleScope.launch {
                controllerManager.sendCommand(uid, "START")
            }
        }

        btnStop.setOnClickListener {
            lifecycleScope.launch {
                controllerManager.sendCommand(uid, "STOP")
            }
        }

        btnCheckStatus.setOnClickListener {
            lifecycleScope.launch {
                controllerManager.requestStatusCheck(uid)
                Toast.makeText(this@RemoteControllerActivity, "상태 체크 요청됨", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performConnect(code: String) {
        lifecycleScope.launch {
            val uid = controllerManager.connectDevice(code)
            if (uid != null) {
                prefs.remoteUid = uid
                showControlMode(uid)
            } else {
                Toast.makeText(this@RemoteControllerActivity, getString(R.string.controller_error_code), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUi(status: String, isOnline: Boolean, lastSeen: Long, mode: String?, speed: Float?, tileType: String?, isForeground: Boolean?) {
        tvDeviceStatus.text = if (isOnline) {
            getString(R.string.controller_status_online)
        } else {
            getString(R.string.controller_status_offline)
        }
        tvDeviceStatus.setTextColor(getColor(if (isOnline) R.color.cheese_primary else R.color.cheese_accent))

        tvForegroundStatus.text = when (isForeground) {
            true -> getString(R.string.controller_status_foreground)
            false -> getString(R.string.controller_status_background)
            else -> getString(R.string.controller_status_foreground_unknown)
        }

        if (lastSeen > 0) {
            val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            tvLastSeen.text = getString(R.string.controller_last_seen, sdf.format(Date(lastSeen)))
        }

        tvWorkStatus.text = if (status == "RUNNING") {
            getString(R.string.controller_device_running)
        } else {
            getString(R.string.controller_device_idle)
        }

        if (status == "RUNNING" && mode != null) {
            layoutDetailedStatus.visibility = View.VISIBLE
            val details = StringBuilder()
            details.append("Mode: $mode")
            if (speed != null) details.append("\nSpeed: ${String.format(Locale.getDefault(), "%.1fx", speed)}")
            if (tileType != null) details.append("\nPattern: $tileType")
            tvStatusDetails.text = details.toString()
        } else {
            layoutDetailedStatus.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controllerManager.stopListening()
    }
}
