package pt.ua.deti.icm.awav.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Utility class for Firebase Cloud Messaging operations
 */
object FCMUtils {
    private const val TAG = "FCMUtils"
    
    /**
     * Get the current FCM token synchronously
     */
    fun getFCMToken(callback: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                callback(null)
                return@addOnCompleteListener
            }

            // Get the token
            val token = task.result
            Log.d(TAG, "Current FCM Token: $token")
            callback(token)
        }
    }
    
    /**
     * Get the current FCM token as a suspend function
     */
    suspend fun getFCMTokenAsync(): String = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "Current FCM Token: $token")
                continuation.resume(token)
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                continuation.resumeWithException(task.exception ?: Exception("Unknown error getting FCM token"))
            }
        }
    }
    
    /**
     * Display the current FCM token in a toast and log
     */
    fun showFCMToken(context: Context) {
        getFCMToken { token ->
            if (token != null) {
                Log.i(TAG, "FCM Token: $token")
                Toast.makeText(context, "FCM Token: $token", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to get FCM token", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Copy FCM token to clipboard
     */
    fun copyFCMTokenToClipboard(context: Context) {
        getFCMToken { token ->
            if (token != null) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("FCM Token", token)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "FCM Token copied to clipboard", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to get FCM token", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 