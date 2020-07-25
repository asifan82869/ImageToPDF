package shock.com.imagetopdf.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import kotlinx.android.synthetic.main.activity_pdf.*
import shock.com.imagetopdf.R
import java.io.File

class PDFActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)

        //UNPACK OUR DATA FROM INTENT
        val i = this.intent
        val path = i.extras!!.getString("PATH")
        //GET THE PDF FILE
        val file = File(path)

        pdfView.fromFile(file)
            .defaultPage(0)
            .enableSwipe(true)
            .scrollHandle( DefaultScrollHandle(this))
            .spacing(10)
            .enableAnnotationRendering(true)
            .enableDoubletap(true)
            .load()
    }
}