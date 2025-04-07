package pt.ua.deti.icm.awav.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Utility class for Firebase Storage operations
 */
object StorageUtils {
    private const val TAG = "StorageUtils"
    
    /**
     * Ensures a folder exists in Firebase Storage
     * Note: Folders are virtual in Firebase Storage, but this can help with organization
     */
    fun ensureFolderExists(folderPath: String): StorageReference {
        val storage = FirebaseStorage.getInstance()
        val folderRef = storage.reference.child(folderPath)
        
        // Create an empty file as a marker (not required, but can help debug)
        val markerRef = folderRef.child(".folder")
        try {
            // This doesn't upload anything but helps test permissions
            val metadata = StorageMetadata.Builder()
                .setContentType("application/octet-stream")
                .setCustomMetadata("purpose", "folder_marker")
                .build()
                
            markerRef.metadata
                .addOnSuccessListener {
                    Log.d(TAG, "Folder $folderPath already exists")
                }
                .addOnFailureListener {
                    // Try to create it with an empty file
                    markerRef.putBytes(ByteArray(0), metadata)
                        .addOnSuccessListener {
                            Log.d(TAG, "Created folder marker for $folderPath")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to create folder marker for $folderPath", e)
                        }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking folder: $folderPath", e)
        }
        
        return folderRef
    }
    
    /**
     * Upload an image to Firebase Storage with multiple fallback approaches
     */
    fun uploadImage(
        context: Context,
        imageUri: Uri,
        path: String,
        onSuccess: (Uri) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val storage = FirebaseStorage.getInstance()
            val imageRef = storage.reference.child(path)
            
            Log.d(TAG, "Uploading image to $path")
            
            // Try to get input stream
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw IOException("Cannot open image URI")
                
            // Read bytes
            val imageBytes = inputStream.readBytes()
            inputStream.close()
            
            // Set metadata
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCacheControl("public, max-age=86400")
                .build()
                
            // Upload with progression tracking
            val uploadTask = imageRef.putBytes(imageBytes, metadata)
            
            uploadTask
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    Log.d(TAG, "Successfully uploaded image to $path")
                    onSuccess(downloadUri)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to upload image to $path", e)
                    
                    // Try a fallback path at root level if the first one fails
                    val fileName = path.substringAfterLast('/')
                    val rootFallbackPath = "fallback_$fileName"
                    val fallbackRef = storage.reference.child(rootFallbackPath)
                    
                    Log.d(TAG, "Trying fallback upload to $rootFallbackPath")
                    
                    fallbackRef.putBytes(imageBytes, metadata)
                        .continueWithTask { fallbackTask ->
                            if (!fallbackTask.isSuccessful) {
                                fallbackTask.exception?.let { throw it }
                            }
                            fallbackRef.downloadUrl
                        }
                        .addOnSuccessListener { fallbackUri ->
                            Log.d(TAG, "Fallback upload successful to $rootFallbackPath")
                            onSuccess(fallbackUri)
                        }
                        .addOnFailureListener { fallbackError ->
                            Log.e(TAG, "Fallback upload also failed", fallbackError)
                            onFailure(e) // Report the original error
                        }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in uploadImage", e)
            onFailure(e)
        }
    }
} 