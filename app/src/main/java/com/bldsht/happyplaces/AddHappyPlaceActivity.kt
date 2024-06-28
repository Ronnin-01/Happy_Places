package com.bldsht.happyplaces

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bldsht.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var binding: ActivityAddHappyPlaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_add_happy_place)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //setSupportActionBar(toolbar_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddPlace.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateDateInView()

        }
        binding.etDate.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date ->{
                DatePickerDialog(this@AddHappyPlaceActivity,
                    dateSetListener,cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tv_add_image ->{
                //Toast.makeText(this@AddHappyPlaceActivity, "Coming soon...", Toast.LENGTH_SHORT).show()
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems){
                    dialog, which ->
                    when(which){
                        0 -> choosePhotoFromGallery()
                        1 -> clickPhoto()
                    }
                }
                pictureDialog.show()
            }
        }
    }

    private fun clickPhoto(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
              if (report!!.areAllPermissionsGranted()){
            val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(galleryIntent, CAMERA)
            }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationalDialogForPermissions()
            }

        }).onSameThread().check()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == GALLERY){
               if(data != null){
                  val contentURI = data.data
                   try{
                       val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentURI)
                       binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                   }catch (e: IOException){
                       e.printStackTrace()
                       Toast.makeText(this@AddHappyPlaceActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                   }
               }
            }else if (requestCode == CAMERA){
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                binding.ivPlaceImage.setImageBitmap(thumbnail)
            }
        }
    }

    private fun choosePhotoFromGallery() {
//        Dexter.withActivity(this).withPermissions(
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ).withListener(object : MultiplePermissionsListener{
//            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
              //  if (report!!.areAllPermissionsGranted()){
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
            //    }
//            }
//
//            override fun onPermissionRationaleShouldBeShown(
//                permissions: MutableList<PermissionRequest>?,
//                token: PermissionToken?
//            ) {
//                showRationalDialogForPermissions()
//            }
//
//        }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("Permissions is not Enabled")
            .setPositiveButton("GO TO SETTINGS")
            { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun updateDateInView(){
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(cal.time).toString())
    }

    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
    }
}
