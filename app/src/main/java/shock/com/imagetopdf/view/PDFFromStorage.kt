package shock.com.imagetopdf.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import kotlinx.android.synthetic.main.activity_pdf.*
import shock.com.imagetopdf.R

class PDFFromStorage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_from_storage)
        val code = intent.getStringExtra("code")
        if (code.toInt() == 100){
            var string = intent.getStringExtra("pdf")
            val uri = Uri.parse(string)
            showPdfFromUri(uri)
        }
    }

    private fun showPdfFromUri(uri: Uri?) {
        pdfView.fromUri(uri)
            .defaultPage(0)
            .enableSwipe(true)
            .scrollHandle( DefaultScrollHandle(this))
            .spacing(10)
            .enableAnnotationRendering(true)
            .enableDoubletap(true)
            .load()
    }
}