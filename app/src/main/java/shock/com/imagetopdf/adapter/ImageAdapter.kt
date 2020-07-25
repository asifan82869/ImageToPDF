package shock.com.imagetopdf.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item.*
import shock.com.imagetopdf.R
import java.io.FileNotFoundException

class ImageAdapter(private val context: Context): RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    val imageDataList: ArrayList<Uri> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageDataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the data model based on position
        var imageData = imageDataList[position]
        holder.imageShow.apply {
            try {
                val inputStream = context.contentResolver.openInputStream(imageData)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                setImageBitmap(bitmap)
            }catch (e: FileNotFoundException){
                Toast.makeText(context,"$e", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun addBitmaps(bitmaps: ArrayList<Uri>) {
        imageDataList.clear()
        imageDataList.addAll(bitmaps)
        notifyDataSetChanged()
    }

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

}