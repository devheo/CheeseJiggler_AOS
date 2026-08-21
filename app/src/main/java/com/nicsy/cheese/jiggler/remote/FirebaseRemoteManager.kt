package com.nicsy.cheese.jiggler.remote

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.random.Random

class FirebaseRemoteManager(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var commandListener: ListenerRegistration? = null
    private var retryCount = 0
    private val maxRetries = 3

    interface RemoteCommandCallback {
        fun onCommandReceived(command: String)
    }

    /**
     * Initialize Firebase Auth and prepare the device.
     */
    suspend fun initializeDevice(onCodeReady: (String) -> Unit): Boolean {
        try {
            val user = auth.currentUser ?: auth.signInAnonymously().await().user
            if (user != null) {
                val uid = user.uid
                val deviceDoc = db.collection("devices").document(uid).get().await()
                
                var code = deviceDoc.getString("code")
                if (code == null) {
                    code = generateUniqueCode()
                    val deviceData = mapOf(
                        "code" to code,
                        "status" to "IDLE",
                        "lastCommand" to "",
                        "timestamp" to System.currentTimeMillis()
                    )
                    db.collection("devices").document(uid).set(deviceData).await()
                    db.collection("codes").document(code).set(mapOf("uid" to uid)).await()
                }
                onCodeReady(code)
                return true
            } else {
                Log.e("RemoteManager", "Failed to sign in anonymously")
                return false
            }
        } catch (e: Exception) {
            Log.e("RemoteManager", "Init failed: ${e.message}", e)
            if (e.message?.contains("PERMISSION_DENIED") == true) {
                Log.e("RemoteManager", "Check Firestore Security Rules and ensure Anonymous Auth is enabled in Firebase Console.")
            }
            return false
        }
    }

    private suspend fun generateUniqueCode(): String {
        var code: String
        var isUnique = false
        var attempts = 0
        do {
            code = String.format(Locale.US, "%06d", Random.nextInt(100000, 999999))
            try {
                val doc = db.collection("codes").document(code).get().await()
                if (!doc.exists()) isUnique = true
            } catch (e: Exception) {
                Log.e("RemoteManager", "Code collision check failed", e)
                // If we can't check, assume it might not be unique or permissions are wrong
            }
            attempts++
        } while (!isUnique && attempts < 10)
        return code
    }

    fun startListening(callback: RemoteCommandCallback) {
        val user = auth.currentUser
        if (user == null) {
            Log.w("RemoteManager", "Cannot start listening: User not authenticated")
            return
        }
        val uid = user.uid
        commandListener?.remove()
        
        Log.d("RemoteManager", "Starting listener for UID: $uid")
        commandListener = db.collection("devices").document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("RemoteManager", "Listen failed: ${e.message}", e)
                    
                    // Retry if permission denied, often happens right after anonymous sign-in
                    if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED && retryCount < maxRetries) {
                        retryCount++
                        Log.d("RemoteManager", "Retrying listener ($retryCount/$maxRetries)...")
                        Handler(Looper.getMainLooper()).postDelayed({
                            startListening(callback)
                        }, 2000L * retryCount)
                    }
                    return@addSnapshotListener
                }

                retryCount = 0 // Reset on success
                if (snapshot != null && snapshot.exists()) {
                    val command = snapshot.getString("lastCommand")
                    if (!command.isNullOrEmpty()) {
                        Log.d("RemoteManager", "Command received: $command")
                        
                        // Trigger callback IMMEDIATELY for responsiveness
                        callback.onCommandReceived(command)

                        // Clear command in the background
                        db.collection("devices").document(uid).update("lastCommand", "")
                            .addOnSuccessListener { Log.d("RemoteManager", "Command cleared successfully") }
                            .addOnFailureListener { Log.e("RemoteManager", "Failed to clear command", it) }
                    }
                }
            }
    }

    fun stopListening() {
        commandListener?.remove()
    }

    fun updateStatus(isRunning: Boolean, mode: String = "", speed: Float = 0f, tileType: String = "", isForeground: Boolean = true) {
        val uid = auth.currentUser?.uid ?: return
        val status = if (isRunning) "RUNNING" else "IDLE"
        val data = mutableMapOf<String, Any>(
            "status" to status,
            "timestamp" to System.currentTimeMillis(),
            "isForeground" to isForeground
        )
        if (isRunning) {
            data["mode"] = mode
            data["speed"] = speed
            data["tileType"] = tileType
        }
        db.collection("devices").document(uid).update(data)
            .addOnFailureListener { Log.e("RemoteManager", "Failed to update status", it) }
    }
}
