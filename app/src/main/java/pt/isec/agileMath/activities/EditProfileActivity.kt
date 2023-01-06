package pt.isec.agileMath.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.FileProvider
import kotlinx.coroutines.runBlocking
import pt.isec.agileMath.constants.Constants.Companion.PREFERENCE_NAME
import pt.isec.agileMath.constants.Tables
import pt.isec.agileMath.databinding.ActivityEditProfileBinding
import pt.isec.agileMath.models.Player
import pt.isec.agileMath.services.FirebaseService
import pt.isec.agileMath.services.Image64Utils
import pt.isec.agileMath.services.PreferenceServices
import pt.isec.agileMath.services.PreferenceServices.customPreference
import pt.isec.agileMath.services.PreferenceServices.id
import pt.isec.agileMath.services.PreferenceServices.nickname
import pt.isec.agileMath.services.PreferenceServices.profile_url
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    companion object {
        private const val FILE_NAME = "avatar_"
        private const val REQUEST_CODE = 42

        fun getIntent(ctx: Context): Intent {
            return Intent(ctx, EditProfileActivity::class.java)
        }

        fun getProfilePlayer(ctx: Context): Player {
            val prefs = customPreference(ctx, PREFERENCE_NAME)

            return Player(prefs.nickname, prefs.profile_url)
        }
    }

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var file: File
    lateinit var currentPhotoPath: String

    var resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val takenImage = BitmapFactory.decodeFile(file.absolutePath)
            binding.imageView.setImageBitmap(takenImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = customPreference(this, PREFERENCE_NAME)

        binding.nickname.setText(prefs.nickname)

        var imagePath = prefs.profile_url?.toByteArray()

        if (imagePath!!.isNotEmpty()){
            val takenImage = BitmapFactory.decodeByteArray(imagePath, 0, imagePath.size)
            binding.imageView.setImageBitmap(takenImage)
        }

        binding.btnTakePicture.setOnClickListener { takePicture() }
        binding.saveProfile.setOnClickListener { onSave() }
    }

    fun openActivityForResult() {
        resultLauncher.launch(getIntent(this))
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePicture(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        file = getPhotoFile(FILE_NAME)

        dispatchTakePictureIntent()
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    fun onSave() {
        var file = getPhotoFile(FILE_NAME)
        var name = binding.nickname.text.toString()

        var str = Image64Utils.byteArrToString(file.readBytes())

        val prefs = customPreference(this, PREFERENCE_NAME)
        prefs.nickname = name.ifEmpty { "Player 1" }
        prefs.profile_url = str

        runBlocking {
            var id = FirebaseService.save(Tables.PLAYERS.parent, Player(prefs.nickname, prefs.profile_url))
            prefs.id = id.toString()
        }

        Toast.makeText(applicationContext,"User information saved",Toast.LENGTH_SHORT).show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val imageBitmap = BitmapFactory.decodeFile(file.absolutePath)
            binding.imageView.setImageBitmap(imageBitmap)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                file = try {
                    createImageFile()
                } catch (ex: IOException) {
                    return
                }
                // Continue only if the File was successfully created
                file.also {
                    val photoURI: Uri = FileProvider.getUriForFile(applicationContext, "pt.isec.agileMath.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                    @Suppress("DEPRECATION")
                    startActivityForResult(takePictureIntent, REQUEST_CODE)
                }
            }
        }
    }
}
