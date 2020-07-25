package shock.com.imagetopdf.data

class PDFDoc {

    private var name:String? = ""
    private var path:String? = ""
    var isChecked:Boolean = false

    fun getName(): String? {
        return name
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getPath(): String? {
        return path
    }

    fun setPath(path: String) {
        this.path = path
    }

}