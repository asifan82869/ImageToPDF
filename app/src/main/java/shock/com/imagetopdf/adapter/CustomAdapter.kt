package shock.com.imagetopdf.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.model.*
import kotlinx.android.synthetic.main.toolbar.*
import shock.com.imagetopdf.R
import shock.com.imagetopdf.view.ShowPDF
import shock.com.imagetopdf.data.PDFDoc
import java.io.File

class CustomAdapt(private val mContext: ShowPDF, private val pdfDocs: ArrayList<PDFDoc>, val listener: ICustomAdaptListener): RecyclerView.Adapter<CustomAdapt.ViewHolder>() {

    var inSharingMode = false
    var shareFile:ArrayList<Uri> = ArrayList()
    var addfile:ArrayList<String> = ArrayList()
    var count:Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.model, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pdfDocs.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pdfDoc = pdfDocs[position]
        val name = pdfDoc.getName().toString()
        holder.nameTxt.text = name

        if (!inSharingMode){
            holder.checkbox.visibility = View.GONE
            pdfDoc.isChecked = false
        }else{
            holder.checkbox.visibility = View.VISIBLE
            holder.checkbox.isChecked = pdfDoc.isChecked
        }

        holder.itemView.setOnClickListener {
            if(inSharingMode){
                pdfDoc.isChecked = !pdfDoc.isChecked
                holder.checkbox.isChecked = pdfDoc.isChecked
            } else {
                listener.openPDFView(pdfDoc.getPath(), name)
            }
        }

        holder.itemView.setOnLongClickListener{
            if (!inSharingMode){
                listener.onLongClick()
                toggleSharingMode()
            }
            true
        }

        holder.checkbox.setOnCheckedChangeListener { compoundButton, b ->
            if (pdfDoc.isChecked != !b){
                pdfDoc.isChecked = b
            }
        }
    }

    private fun addShare(){
        val selectedPdfs = getSelectedPdfs()
        for (i in selectedPdfs){
            i.getPath()?.let { addfile.add(it) }
        }
    }

    private fun multiSharePath(){
        addShare()
        for (i in addfile){
            val file = File(i)
            var uri = Uri.fromFile(file)
            shareFile.add(uri)
        }
    }

    fun fileShares(){
        val selectedPdfs = getSelectedPdfs()
        if(selectedPdfs.isNotEmpty()) {
            val i = Intent()
            i.action = Intent.ACTION_SEND_MULTIPLE;
            i.type = "application/pdf"
            multiSharePath()
            Toast.makeText(mContext, "${addfile.size} and ${shareFile.size}", Toast.LENGTH_SHORT)
                .show()
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareFile)
            mContext.startActivity(i)
        }else{
            Toast.makeText(mContext, "Please Select a file for share", Toast.LENGTH_LONG).show()
        }
    }

    fun deletePdfs() {
        val selectedPdfs = getSelectedPdfs()
        val s = pdfDocs.size-1
        if(selectedPdfs.isNotEmpty()) {
            for (i in s downTo 0) {
                val pdf = pdfDocs[i]
                for (selectedPdf in selectedPdfs) {
                    if (pdf.getPath()?.equals(selectedPdf.getPath()) == true) {
                        val file = File(selectedPdf.getPath()!!)
                        file.delete()
                        pdfDocs.removeAt(i)
                        break
                    }
                }
            }
            notifyDataSetChanged()
        }else{
            Toast.makeText(mContext,"Please Select a file for delete", Toast.LENGTH_LONG).show()
        }

    }

    private fun getSelectedPdfs():ArrayList<PDFDoc>{
        val selectedPdfs = ArrayList<PDFDoc>()
        for(pdfDoc in pdfDocs){
            if(pdfDoc.isChecked){
                selectedPdfs.add(pdfDoc)
            }
        }
        return selectedPdfs
    }

    fun toggleSharingMode(){
        inSharingMode = !inSharingMode
        notifyDataSetChanged()
    }

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer

    interface ICustomAdaptListener{
        fun onLongClick()
        fun openPDFView(path: String?, name: String)
    }
}