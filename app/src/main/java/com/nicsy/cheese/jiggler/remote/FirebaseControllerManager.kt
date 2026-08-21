package com.nicsy.cheese.jiggler.remote

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class FirebaseControllerManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var statusListener: ListenerRegistration? = null
    private var retryCount = 0
    private val maxRetries = 3

    interface DeviceStatusCallback {
        fun onStatusChanged(status: String, isOnline: Boolean, lastSeen: Long, mode: String?, speed: Float?, tileType: String?, isForeground: Boolean?)
        fun onError(message: String)
    }

    private suspend fun ensureAuth() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    /**
     * Resolves a 6-digit friend code to a target UID.
     */
    suspend fun connectDevice(code: String): String? {
        return try {
            ensureAuth()
            val doc = db.collection("codes").document(code).get().await()
            doc.getString("uid")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Listens to the status and activity of a target device.
     */
    fun startListening(uid: String, callback: DeviceStatusCallback) {
        statusListener?.remove()
        
        // We trigger an async auth check, but the listener might fail if not yet authed.
        // In a more complex app, we'd wait for auth, but for simple control, retry on error is enough.
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnSuccessListener {
                startListeningInternal(uid, callback)
            }.addOnFailureListener {
                callback.onError("Authentication failed")
            }
        } else {
            startListeningInternal(uid, callback)
        }
    }

    private fun startListeningInternal(uid: String, callback: DeviceStatusCallback) {
        statusListener = db.collection("devices").document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ControllerManager", "Listen failed for $uid: ${e.message}", e)
                    
                    // Retry if permission denied
                    if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED && retryCount < maxRetries) {
                        retryCount++
                        Handler(Looper.getMainLooper()).postDelayed({
                            startListeningInternal(uid, callback)
                        }, 2000L * retryCount)
                        return@addSnapshotListener
                    }
                    
                    callback.onError(e.message ?: "Unknown error")
                    return@addSnapshotListener
                }

                retryCount = 0
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status") ?: "IDLE"
                    val timestamp = snapshot.getLong("timestamp") ?: 0L
                    val mode = snapshot.getString("mode")
                    val speed = snapshot.getDouble("speed")?.toFloat()
                    val tileType = snapshot.getString("tileType")
                    val isForeground = snapshot.getBoolean("isForeground")
                    
                    // Consider online if active within last 2 minutes
                    val isOnline = (System.currentTimeMillis() - timestamp) < 120_000
                    
                    callback.onStatusChanged(status, isOnline, timestamp, mode, speed, tileType, isForeground)
                } else {
                    callback.onError("Device not found")
                }
            }
    }

    fun stopListening() {
        statusListener?.remove()
    }

    /**
     * Requests a real-time status update from the target device.
     */
    suspend fun requestStatusCheck(uid: String) {
        sendCommand(uid, "STATUS_CHECK")
    }

    /**
     * Sends a command (START/STOP) to the target device.
     */
    suspend fun sendCommand(uid: String, command: String) {
        try {
            ensureAuth()
            db.collection("devices").document(uid).update("lastCommand", command).await()
        } catch (e: Exception) {
            // Log error
        }
    }
}
