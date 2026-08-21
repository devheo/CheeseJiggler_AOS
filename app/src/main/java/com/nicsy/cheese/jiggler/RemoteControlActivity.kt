package com.nicsy.cheese.jiggler

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.nicsy.cheese.jiggler.remote.FirebaseRemoteManager
import kotlinx.coroutines.launch

class RemoteControlActivity : ComponentActivity() {

    private lateinit var remoteManager: FirebaseRemoteManager
    private lateinit var tvFriendCode: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvLastCommand: TextView
    private lateinit var btnCopyCode: MaterialButton
    private lateinit var btnHelp: MaterialButton
    private lateinit var btnResetCode: MaterialButton
    private lateinit var btnOpenController: MaterialButton
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_control)

        remoteManager = FirebaseRemoteManager(applicationContext)
        tvFriendCode = findViewById(R.id.tvFriendCode)
        tvStatus = findViewById(R.id.tvStatus)
        tvLastCommand = findViewById(R.id.tvLastCommand)
        btnCopyCode = findViewById(R.id.btnCopyCode)
        btnHelp = findViewById(R.id.btnHelp)
        btnResetCode = findViewById(R.id.btnResetCode)
        btnOpenController = findViewById(R.id.btnOpenController)
        btnBack = findViewById(R.id.btnBack)

        tvLastCommand.text = getString(R.string.remote_last_command_none)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack.setOnClickListener { finish() }
        
        btnCopyCode.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("Friend Code", tvFriendCode.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, getString(R.string.remote_copied), Toast.LENGTH_SHORT).show()
        }

        btnHelp.setOnClickListener {
            startActivity(android.content.Intent(this, RemoteGuideActivity::class.java))
        }

        btnOpenController.setOnClickListener {
            startActivity(android.content.Intent(this, RemoteControllerActivity::class.java))
        }

        btnResetCode.setOnClickListener {
            // In a real app, you might want to delete the old mapping in Firestore too.
            // For now, we'll just re-trigger the initialization logic if code is null.
            Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            tvFriendCode.text = getString(R.string.remote_init_loading)
            val success = remoteManager.initializeDevice { code ->
                tvFriendCode.text = code
                tvStatus.text = getString(R.string.remote_status_connected)
            }
            if (!success) {
                tvFriendCode.text = "Error"
                tvStatus.text = "Failed to connect to Firebase"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        remoteManager.startListening(object : FirebaseRemoteManager.RemoteCommandCallback {
            override fun onCommandReceived(command: String) {
                tvLastCommand.text = getString(R.string.remote_last_command, command)
                // Commands are also handled in MainActivity if it's running
            }
        })
    }

    override fun onStop() {
        super.onStop()
        remoteManager.stopListening()
    }
}
