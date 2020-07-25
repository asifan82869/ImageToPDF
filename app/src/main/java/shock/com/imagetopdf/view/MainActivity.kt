package shock.com.imagetopdf.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_main.*
import kotlinx.android.synthetic.main.savefile.view.*
import kotlinx.android.synthetic.main.toolbar.*
import shock.com.imagetopdf.R
import shock.com.imagetopdf.adapter.ImageAdapter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private var _imageFileName = ""
    private val bitmaps:  ArrayList<Uri> = ArrayList()
    private var _imagefileUri: Uri? = null
    private val GALLERY = 1
    private val CAMERA = 2
    private val REQUEST_EXTERNAL_STORAGE = 100
    private val IMAGE_CAPTURE_FOLDER = "/sdcard/IMAGE COVERT PDF/Camera Photo/"
    private var image:Bitmap? = null
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private var document:PdfDocument? = null
    private lateinit var adapter : ImageAdapter
    private var saveFileName = ""
    private var isAlready:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Image To PDF"
        permission()
    }

    private fun permission(){
        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE)
        } else {
            recycle()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    recycle()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun recycle(){
        adapter = ImageAdapter(this)
        rvImage.adapter = adapter
        rvImage.layoutManager = GridLayoutManager(this@MainActivity,2)
        val itemTouchHelperCallback =
            object :
                ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val fp = viewHolder.adapterPosition
                    val tp = target.adapterPosition
                    Collections.swap(bitmaps, fp, tp)
                    recyclerView.adapter?.notifyItemMoved(fp, tp)
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                }
            }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvImage)
    }

    private fun clearAll(){
        bitmaps.clear()
        (rvImage.adapter as ImageAdapter).imageDataList.clear()
        (rvImage.adapter as ImageAdapter).notifyDataSetChanged()
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.menu)
        supportActionBar?.title = "Image To PDF"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        toolbar.inflateMenu(R.menu.menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.sImage -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.type = "image/*"
                startActivityForResult(intent, GALLERY)
            }
            R.id.iCamera -> {
                genRandom()
                val intent2 = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                _imagefileUri = Uri.fromFile(getFileCamera())
                intent2.putExtra(MediaStore.EXTRA_OUTPUT, _imagefileUri)
                startActivityForResult(intent2, CAMERA)
            }
            R.id.showPDF ->{
                val i = Intent(this, ShowPDF::class.java)
                startActivity(i)
            }
            R.id.cancel -> {
                clearAll()
            }
            R.id.convert -> {
                convert()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == GALLERY) {
                val clipData = data!!.clipData
                if (clipData != null) {
                    //multiple images selecetd
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri
                        Log.d("URI", imageUri.toString())
                        try {
                            bitmaps.add(imageUri!!)
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }
                    }
                }else {
                    //single image selected
                    val imageUri = data.data
                    Log.d("URI", imageUri.toString())
                    try {
                        bitmaps.add(imageUri!!)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }else{
                if (requestCode == CAMERA){
                    bitmaps.add(_imagefileUri!!)
                }
            }
            if(rvImage.adapter is ImageAdapter){
                (rvImage.adapter as ImageAdapter).addBitmaps(bitmaps)
            }
            toolbar.menu.clear()
            toolbar.inflateMenu(R.menu.toolbar_meanu_for_convert_nd_cancel)
            supportActionBar?.title = ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun convert(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.savefile, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("Save File")
        val mAlertDialog = mBuilder.show()
        mDialogView.save.setOnClickListener {
            saveFileName = mDialogView.savefile.text.toString()
            fileNameCheack()
            if(isAlready){
                isAlready = false
                Toast.makeText(this, "This name Already exits. please enter different name", Toast.LENGTH_SHORT).show()
            }else{
                mAlertDialog.dismiss()
                createPdf()
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun createPdf(){
        document = PdfDocument()
        for (i in bitmaps.indices){
            val inputStream = contentResolver.openInputStream(bitmaps[i])
            val bitmap = BitmapFactory.decodeStream(inputStream)
            image = bitmap
            if(image != null) {
                val pageInfo =
                    PdfDocument.PageInfo.Builder(image!!.width, image!!.height, i).create()
                val page = document?.startPage(pageInfo)
                if(page != null) {
                    val canvas = page.canvas

                    val paint = Paint()
                    paint.color = Color.parseColor("#ffffff")
                    canvas.drawPaint(paint)
                    val bitmap = Bitmap.createScaledBitmap(image!!, image!!.width, image!!.height, true)

                    paint.color = Color.BLUE
                    canvas?.drawBitmap(bitmap, 0f, 0f, null)
                    document?.finishPage(page)
                }
            }
        }
        try {
            val file = getPath(File(Environment.getExternalStorageDirectory().absolutePath),"IMAGE COVERT PDF")
            val fout = FileOutputStream(file)
            document?.writeTo(fout)
            Toast.makeText(this, "Your image has Successfully converted into PDF. Your file save in '/IMAGE COVERT PDF/' Folder", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
        document?.close()
        document = null
        clearAll()
    }

    private fun fileNameCheack() {
        val downloadsFolder = File("/sdcard/IMAGE COVERT PDF/Covert PDF/")
        val files = downloadsFolder.listFiles()
        var fileName = "$saveFileName.pdf"

        for(i in files.indices){
            val file = files[i]
            if (file.name == fileName){
                isAlready = true
                break
            }
        }
    }

    private fun getPath(path:File, fileName: String): File?{
        val root = File(path, fileName)
        if (!root.exists()) {
            root.mkdirs()
        }
        val child = File("/sdcard/IMAGE COVERT PDF/Covert PDF/")
        if (!child.exists()){
            child.mkdir()
        }

        val pdfName: String = saveFileName
        val targetPdf = "$pdfName.pdf"
        val file = File(child, targetPdf)

        //Saving Pdf file in IMAGE COVERT PDF folder

        return file
    }

    //Checking a camera path/folder
    private fun getFileCamera(): File? {
        var  file = File(IMAGE_CAPTURE_FOLDER)
        if (!file.exists()) {
            file.mkdirs()
        }
        return File(
            file.toString() + File.separator + _imageFileName
                    + ".jpg"
        )
    }

    // creating a file name to joining random alphabet
    private fun genRandom() {
        val r = Random()
        val alphabet = "abcdefghijklmnopqrstuvwxyz123456789"
        val sb = StringBuilder()
        for (i in 0..5) {
            sb.append(alphabet[r.nextInt(alphabet.length)])
        }
        _imageFileName = sb.toString()
    }

}