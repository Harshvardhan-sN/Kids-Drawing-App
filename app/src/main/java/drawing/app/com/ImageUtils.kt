package drawing.app.com
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    fun saveImage(context: Context, bitmap: Bitmap): Uri? {
        val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(imagesDir, "drawn_image.png")
        try {
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            val imageUri = FileProvider.getUriForFile(
                context,
                "com.example.yourapp.fileprovider",
                imageFile
            )

            val location = imageFile.absolutePath
            Log.d("FileLocation", "Image saved at: $location")

            return imageUri

        } catch (e: Exception) {
            Log.e("LKE", "$e")
        }
        return null
    }

    fun shareImage(context: Context, imageUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
    }
}
