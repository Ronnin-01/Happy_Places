package com.bldsht.happyplaces.activities

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bldsht.happyplaces.R
import com.bldsht.happyplaces.database.DataBaseHandler
import com.bldsht.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.bldsht.happyplaces.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var binding: ActivityAddHappyPlaceBinding
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0


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
        updateDateInView()
        binding.etDate.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
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
            R.id.btn_save ->{
                val title = binding.etTitle.text
                val desc = binding.etDescription.text
                val date = binding.etDate.text
                val location = binding.etLocation.text
              if(title.isNullOrEmpty() || desc.isNullOrEmpty() || date.isNullOrEmpty() || location.isNullOrEmpty() || saveImageToInternalStorage == null){
                  Toast.makeText(this@AddHappyPlaceActivity, "Please fill all the fields", Toast.LENGTH_SHORT).show()
              }else{
                  val happyPlaceModel = HappyPlaceModel(0, title.toString(), saveImageToInternalStorage.toString(), desc.toString(), date.toString(), location.toString(), mLatitude, mLongitude)
                  val dbHandler = DataBaseHandler(this)
                  val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                  if(addHappyPlace > 0){
                      Toast.makeText(this@AddHappyPlaceActivity, "Happy Place added successfully", Toast.LENGTH_SHORT).show()
                      finish()
                  }
              }
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
                       saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                       Log.e("Saved Image", "Path :: $saveImageToInternalStorage")
                       binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                   }catch (e: IOException){
                       e.printStackTrace()
                       Toast.makeText(this@AddHappyPlaceActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                   }
               }
            }else if (requestCode == CAMERA){
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("Saved Image Camera", "Path :: $saveImageToInternalStorage")

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

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "Place_${System.currentTimeMillis()}.jpg")

        try{
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }
}
