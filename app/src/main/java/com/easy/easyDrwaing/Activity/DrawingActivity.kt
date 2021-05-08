package com.easy.easyDrwaing.Activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.easy.easyDrwaing.R
import com.easy.easyDrwaing.View.DrawingView
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class DrawingActivity : AppCompatActivity() {
    companion object {
        const val READ_AND_WRITE_PERMISSION_CODE = 101
        const val MEDIA = 102
    }

    lateinit var drawingView: DrawingView
    private lateinit var brushSizeImageView: ImageView
    private lateinit var colorPickerImageView: ImageView
    private lateinit var flDrawingView: FrameLayout
    private lateinit var backgroundImage: ImageView
    private lateinit var ivGallery: ImageView
    private lateinit var ivClear: ImageView
    private var drawingImage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        // Hooks
        drawingView = findViewById(R.id.drawing_view)
        brushSizeImageView = findViewById(R.id.iv_brush)
        colorPickerImageView = findViewById(R.id.color_picker)
        flDrawingView = findViewById(R.id.fl_drawing_view)
        backgroundImage = findViewById(R.id.iv_background_image)
        ivGallery = findViewById(R.id.iv_gallery)
        ivClear = findViewById(R.id.iv_clear)


        // Change Brush Size
        brushSizeImageView.setOnClickListener {
            openBrushSizeDialog()
        }
        // Change Brush Color
        colorPickerImageView.setOnClickListener {
            openColorPickerDialog()
        }
        // Change Background Image
        ivGallery.setOnClickListener {
            if (checkForPermission()) {
                val photoIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(photoIntent, MEDIA)
            } else {
                requestPermission()
            }
        }
        // Clear all drawing to the starting
        ivClear.setOnClickListener {
            showConfirmationDialog("Are you sure you want to clear all?", "allClear")

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo -> {
                drawingView.onUndoClick()
                return true
            }
            R.id.redo -> {
                drawingView.onRedoClick()
                return true
            }
            R.id.new_paint -> {
                showConfirmationDialog("Are you sure you want to open new paint?", "newPaint")
                return true
            }
            R.id.share -> {
                if (drawingImage.isNotEmpty()) {
                    sharedFunctionIntent()
                } else {
                    Toast.makeText(
                        this,
                        "Nothing to share \n Please save your drawing first",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }
            R.id.save -> {
                if (checkForPermission()) {
                    BitmapAsyncTask(getDrawingImage(flDrawingView), applicationContext).execute()
                } else
                    requestPermission()
                return true
            }
        }
        return false
    }

    private fun sharedFunctionIntent() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(drawingImage))
        intent.type = "image/png"
        startActivity(intent)
    }

    // Confirmation Popup
    private fun showConfirmationDialog(text: String, from: String) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.confirmation_dialog, null)
        builder.setView(view)
        builder.setCancelable(false)
        val dialog = builder.show()
        val title = dialog.findViewById<TextView>(R.id.title)
        val btnYes = dialog.findViewById<Button>(R.id.btn_yes)
        val btnNo = dialog.findViewById<Button>(R.id.btn_no)
        title!!.text = text
        btnYes!!.setOnClickListener {
            if (from == "newPaint") {
                backgroundImage.setImageURI(null)
                drawingView.onAllClear()
                drawingImage = ""
                Toast.makeText(this, "Draw a new paint", Toast.LENGTH_SHORT).show()
                colorPickerImageView.background = Color.BLACK.toDrawable()
                dialog.dismiss()
            } else if (from == "allClear") {
                drawingView.onAllClear()
                drawingImage = ""
                Toast.makeText(this, "All Cleared", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        btnNo!!.setOnClickListener {
            dialog.dismiss()
        }
    }

    // Getting background image from storage
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MEDIA) {
                try {
                    if (data!!.data != null) {
                        backgroundImage.setImageURI(data.data)
                    } else
                        Snackbar.make(
                            findViewById(R.id.content),
                            "Image Read failed",
                            Snackbar.LENGTH_SHORT
                        ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Snackbar.make(
                    findViewById(R.id.content),
                    "Something Went Wrong",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Color Picker Dialog
    private fun openColorPickerDialog() {
        val coloPicker = ColorPickerDialog
            .Builder(this)
            .setTitle("Pick Your Color")
            .setColorShape(ColorShape.CIRCLE)
            .setDefaultColor(R.color.black)
            .setColorListener { color, colorHex ->
                drawingView.setBrushColor(colorHex)
                colorPickerImageView.background = color.toDrawable()
            }
        coloPicker.show()
    }

    // Brush Size Dialog
    private fun openBrushSizeDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.brush_size_dialog)
        val vSmallSize = dialog.findViewById<ImageButton>(R.id.ib_very_small_brush_size)
        val smallSize = dialog.findViewById<ImageButton>(R.id.ib_small_brush_size)
        val mediumSize = dialog.findViewById<ImageButton>(R.id.ib_medium_brush_size)
        val largeSize = dialog.findViewById<ImageButton>(R.id.ib_large_brush_size)

        vSmallSize.setOnClickListener {
            drawingView.setBrushSize(5.toFloat())
            dialog.dismiss()
        }
        smallSize.setOnClickListener {
            drawingView.setBrushSize(10.toFloat())
            dialog.dismiss()
        }
        mediumSize.setOnClickListener {
            drawingView.setBrushSize(15.toFloat())
            dialog.dismiss()
        }
        ;largeSize.setOnClickListener {
            drawingView.setBrushSize(20.toFloat())
            dialog.dismiss()
        }
        dialog.show()
    }

    // Checking for permission
    private fun checkForPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), READ_AND_WRITE_PERMISSION_CODE
        )

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_AND_WRITE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Permission Granted",
                    Snackbar.LENGTH_LONG
                ).show();
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Oops you have denied the permission",
                    Snackbar.LENGTH_LONG
                ).show();
            }
        }
    }

    // Saving Drawing View in Bitmap
    private fun getDrawingImage(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgImage = view.background
        if (bgImage != null)
            bgImage.draw(canvas)
        else
            canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    private inner class BitmapAsyncTask(val bitmap: Bitmap, val context: Context) :
        AsyncTask<Any, Void, String>() {

        private lateinit var mProgressBar: Dialog

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any?): String {
            var result = ""

            if (bitmap != null) {
                try {
                    val bytesArray = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, bytesArray)
                    val file =
                        File(Environment.getExternalStorageDirectory()!!.absoluteFile.toString() + File.separator + "EasyDrawingApp_" + System.currentTimeMillis() / 1000 + ".png")
                    val fos = FileOutputStream(file)
                    fos.write(bytesArray.toByteArray())
                    fos.close()
                    result = file.absolutePath
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            hideProgressBar()
            if (result.isNotEmpty()) {
                Toast.makeText(context, "File are saved: $result", Toast.LENGTH_SHORT).show()
                drawingImage = result
            } else {
                Toast.makeText(context, "File are not saved", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        private fun showProgressDialog() {
            mProgressBar = Dialog(this@DrawingActivity)
            mProgressBar.setContentView(R.layout.custom_progress_dialog)
            mProgressBar.show()
        }

        private fun hideProgressBar() {
            mProgressBar.dismiss()
        }

    }


    // Enabling double click back button
    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}
