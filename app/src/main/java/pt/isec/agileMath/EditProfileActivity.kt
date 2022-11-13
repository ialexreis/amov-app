package pt.isec.agileMath

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.FileProvider
import com.alex.amov_app.databinding.ActivityEditProfileBinding
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    companion object {
        private const val FILE_NAME = "photo.jpg"
        private const val REQUEST_CODE = 42

        fun getIntent(ctx: Context): Intent {
            return Intent(ctx, EditProfileActivity::class.java)
        }
    }

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var file: File

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
        binding.btnTakePicture.setOnClickListener { takePicture() }
    }

    fun openActivityForResult() {
        resultLauncher.launch(Companion.getIntent(this))
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePicture(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        file = getPhotoFile(FILE_NAME)

        // This DOESN'T work for API >= 24 (starting 2016)
        // takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile)

        val fileProvider = FileProvider.getUriForFile(this, "pt.isec.agileMath.fileprovider", file)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        if (takePictureIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CODE)
        } else {
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    fun onSave() {

    }
}
