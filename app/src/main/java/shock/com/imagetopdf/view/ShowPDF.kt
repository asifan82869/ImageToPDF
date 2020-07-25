package shock.com.imagetopdf.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.option.view.*
import kotlinx.android.synthetic.main.toolbar.*
import shock.com.imagetopdf.R
import shock.com.imagetopdf.adapter.CustomAdapt
import shock.com.imagetopdf.data.PDFDoc
import java.io.File
import java.util.*

class ShowPDF : AppCompatActivity() {
    val REQUEST_CODE = 100
    private lateinit var adapter : CustomAdapt
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_p_d_f)
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Your PDF"
        toolbar.inflateMenu(R.menu.menu_pdf)
        pdfshow()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBackPressed() {
        if(rv.adapter is CustomAdapt){
            val adapter = (rv.adapter as CustomAdapt)
            if(adapter.inSharingMode){
                adapter.toggleSharingMode()
                toolbar.menu.clear()
                toolbar.inflateMenu(R.menu.menu_pdf)
                supportActionBar?.title = "Your PDF"
                itemCounter.text = ""
                adapter.count = 0
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        toolbar.inflateMenu(R.menu.menu_pdf)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.delete -> {
                val mDialogView = LayoutInflater.from(this).inflate(R.layout.option, null)
                val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("Delete File")
                val mAlertDialog = mBuilder.show()
                mDialogView.sdelete.setOnClickListener {
                    mAlertDialog.dismiss()
                    (rv.adapter as CustomAdapt).deletePdfs()
                }
                mDialogView.scancel.setOnClickListener {
                    mAlertDialog.dismiss()
                }
            }
            R.id.share -> {
                (rv.adapter as CustomAdapt).fileShares()
                (rv.adapter as CustomAdapt).shareFile.clear()
                (rv.adapter as CustomAdapt).addfile.clear()
            }
            R.id.storageFile -> {
                Toast.makeText(this, "selectPDF", Toast.LENGTH_LONG).show()
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                val uri = Uri.parse(
                    (Environment.getExternalStorageDirectory().path
                            + File.separator).toString() + "/IMAGE COVERT PDF/Covert PDF/" + File.separator
                )
                intent.setDataAndType(uri, "application/pdf")
                startActivityForResult(
                    Intent.createChooser(intent, "Open Folder"), REQUEST_CODE
                )
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedPdfFromStorage = data.data
            val i = Intent(this, PDFFromStorage::class.java)
            i.putExtra("code", "100")
            i.putExtra("pdf", selectedPdfFromStorage.toString())
            startActivity(i)
        }
    }

    fun pdfshow(){
        adapter = CustomAdapt(
            this@ShowPDF,
            getPDFs(),
            object :
                CustomAdapt.ICustomAdaptListener {
                @RequiresApi(Build.VERSION_CODES.M)
                override fun onLongClick() {
                    val adapter =
                        (rv.adapter as CustomAdapt)
                    if (!adapter.inSharingMode) {
                        toolbar.menu.clear()
                        toolbar.inflateMenu(R.menu.toolbar_menu)
                        supportActionBar?.title = ""
                        itemCounter.text = "0 Selected Items"
                    } else {
                        adapter.count = 0
                    }
                }

                override fun openPDFView(path: String?, name: String) {
                    val i = Intent(this@ShowPDF, PDFActivity::class.java)
                    i.putExtra("PATH", path)
                    i.putExtra("name", name)
                    startActivity(i)
                }
            })
        rv.adapter = adapter
        rv.layoutManager = GridLayoutManager(this@ShowPDF,2)
    }

    private fun getPDFs(): ArrayList<PDFDoc> {
        val pdfDocs = ArrayList<PDFDoc>()
        //TARGET FOLDER
        val downloadsFolder = File("/sdcard/IMAGE COVERT PDF/Covert PDF/")
        var pdfDoc: PDFDoc?

        //GET ALL FILES IN DOWNLOAD FOLDER
        val files = downloadsFolder.listFiles()
        if(files.isEmpty()){
            Toast.makeText(this, "There is no file available", Toast.LENGTH_SHORT).show()
        }else{
            try {
                //LOOP THRU THOSE FILES GETTING NAME AND URI
                for (i in files.indices) {
                    val file = files[i]
                    pdfDoc = PDFDoc()
                    pdfDoc.setName(file.name)
                    pdfDoc.setPath(file.absolutePath)
                    pdfDocs.add(pdfDoc)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "PDF not found", Toast.LENGTH_SHORT).show()
            }
        }
        return pdfDocs
    }
}