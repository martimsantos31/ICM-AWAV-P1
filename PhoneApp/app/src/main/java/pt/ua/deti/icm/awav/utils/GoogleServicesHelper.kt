package pt.ua.deti.icm.awav.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.security.ProviderInstaller
import pt.ua.deti.icm.awav.AWAVApplication

/**
 * Helper class to safely initialize Google Play Services
 */
object GoogleServicesHelper {
    private const val TAG = "GoogleServicesHelper"
    
    /**
     * Initialize Google Play Services
     * Must be called in the Application class
     */
    fun init(context: Context): Boolean {
        try {
            // Install security providers
            try {
                ProviderInstaller.installIfNeeded(context)
                Log.d(TAG, "Security provider successfully installed")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to install security provider", e)
                // Continue even on failure - we'll check Play Services availability next
            }
            
            // Verify the package exists
            try {
                val packageInfo = context.packageManager.getPackageInfo("com.google.android.gms", 0)
                Log.d(TAG, "Google Play Services package found: ${packageInfo.packageName}")
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "Google Play Services package not found", e)
                AWAVApplication.disableGooglePlayServices()
                return false
            } catch (e: Exception) {
                Log.e(TAG, "Error checking Google Play Services package", e)
                AWAVApplication.disableGooglePlayServices()
                return false
            }
            
            // Check Google Play Services availability
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
            
            if (resultCode != ConnectionResult.SUCCESS) {
                Log.w(TAG, "Google Play Services not available (code $resultCode)")
                AWAVApplication.disableGooglePlayServices()
                return false
            }
            
            Log.d(TAG, "Google Play Services successfully initialized")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Critical error initializing Google Play Services", e)
            AWAVApplication.disableGooglePlayServices()
            return false
        }
    }
    
    /**
     * Get the debug SHA-1 certificate fingerprint for Firebase
     * Output: B0:F5:0E:D4:A6:8D:D9:FC:B5:10:50:CB:D3:1B:51:CC:97:63:7B:E9
     */
    fun getDebugFingerprint(): String {
        return "B0:F5:0E:D4:A6:8D:D9:FC:B5:10:50:CB:D3:1B:51:CC:97:63:7B:E9"
    }
} 