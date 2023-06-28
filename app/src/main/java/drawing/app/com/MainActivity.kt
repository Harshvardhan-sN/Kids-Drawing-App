package drawing.app.com

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.get
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    companion object{
        private const val GALLERY_CODE = 1
        private const val READ_EXTERNAL_STORAGE_PERMISSION_CODE = 2
        private const val MANAGE_EXTERNAL_STORAGE_CODE = 3
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }

    private var drawingView : DrawingView? = null
    private var mImageButtonCur: ImageButton? = null
    var customProgress: Dialog? = null

    private var selectedImageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val iBrush: ImageButton = findViewById(R.id.brush_size)
        val pickImageBtn: ImageButton = findViewById(R.id.pick_image_btn)
        val linearLayoutPaintColor = findViewById<LinearLayout>(R.id.color_pallet)
        val btnUndo: ImageButton = findViewById(R.id.undo_btn)
        val btnRedo: ImageButton = findViewById(R.id.redo_btn)
        val btnSave: ImageButton = findViewById(R.id.btn_save)


        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(5.toFloat())

        mImageButtonCur = linearLayoutPaintColor[8] as ImageButton
        mImageButtonCur!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_selected)
        )

        iBrush.setOnClickListener {
            showBrushSizeDialog()
        }

        btnUndo.setOnClickListener{
            drawingView?.onClickUndo()
        }
        btnRedo.setOnClickListener{
            drawingView?.onClickRedo()
        }
        pickImageBtn.setOnClickListener{
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchGalleryActivity.launch(intent)
        }
        btnSave.setOnClickListener {
            val finalLayout: FrameLayout = findViewById(R.id.frame_layout)
            val mBitMap: Bitmap = getBitmapFromView(finalLayout)
            val imageUri = ImageUtils.saveImage(this@MainActivity, mBitMap)
            if (imageUri != null) {
                ImageUtils.shareImage(this@MainActivity, imageUri)
            } else {
                Toast.makeText(this@MainActivity, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val launchGalleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK) {
            selectedImageUri = it.data!!.data
            val frameBgImage: ImageView = findViewById(R.id.frames_image)
            frameBgImage.setImageURI(selectedImageUri)
        }
    }

    private fun showBrushSizeDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView((R.layout.dailog_brush_size))
        brushDialog.setTitle("Brush Size: ")
        val smallBtn: ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        smallBtn.setOnClickListener {
            drawingView?.setSizeForBrush(3.toFloat())
            brushDialog.dismiss()
        }
        val mediumBtn: ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener {
            drawingView?.setSizeForBrush(7.toFloat())
            brushDialog.dismiss()
        }
        val largeBtn: ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
        largeBtn.setOnClickListener {
            drawingView?.setSizeForBrush(13.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    fun paintClicked(view: View){
        if(view !== mImageButtonCur){
            val imgBtn = view as ImageButton
            val clTag = imgBtn.tag.toString()
            drawingView?.setColor(clTag)
            imgBtn.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_selected)
            )
            mImageButtonCur?.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet)
            )
            mImageButtonCur = view
        }
    }
    private fun showRationaleDialog(
        title: String,
        message: String,
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel"){
                    dialog, _->dialog.dismiss()
            }
        builder.create().show()
    }

    private fun showCustomProgress() {
        customProgress = Dialog(this)
        customProgress?.setContentView(R.layout.progress_dialog)
        customProgress?.show()
    }
    private  fun cancelProgressBar(){
        if(customProgress != null){
            customProgress?.dismiss()
            customProgress = null
        }
    }

    private fun getBitmapFromView(view: View) : Bitmap{
        val returnedBitmap = Bitmap.createBitmap(view.width,
            view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if(bgDrawable!=null){
            bgDrawable.draw(canvas)
        } else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)

        return returnedBitmap
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.reset, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_Reset ->{
                restartApp(this@MainActivity)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun restartApp(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}