package ir.staryas.doorway.fragments

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Dialog
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.materialspinner.MaterialSpinner
import ir.staryas.doorway.model.CategoriesResponse
import ir.staryas.doorway.networking.ApiClient
import ir.staryas.doorway.networking.ApiService
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.provider.MediaStore
import android.content.Intent
import android.net.Uri
import android.os.Environment
import java.io.File
import android.graphics.Bitmap
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.provider.DocumentsContract
import android.util.Base64
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ir.staryas.doorway.utils.PrefManage
import ir.staryas.doorway.utils.ViewDialog
import java.io.IOException
import java.util.*
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import id.zelory.compressor.Compressor
import ir.staryas.doorway.R
import ir.staryas.doorway.model.MSG
import ir.staryas.doorway.utils.NumberTextWatcher
import java.io.ByteArrayOutputStream
import java.lang.Exception


class AddFragment : Fragment() {

    companion object {

        @JvmStatic
        fun newInstance() =
            AddFragment().apply {
                arguments = Bundle().apply {
                    // putString(ARG_PARAM1, param1)
                }
            }


        private const val REQUEST_IMAGE_CAPTURE_1 = 10001
        private const val REQUEST_IMAGE_CAPTURE_2 = 20002
        private const val REQUEST_IMAGE_CAPTURE_3 = 30003
        private const val REQUEST_IMAGE_CAPTURE_4 = 40004

        private const val REQUEST_IMAGE_CHOOSE_1 = 111
        private const val REQUEST_IMAGE_CHOOSE_2 = 222
        private const val REQUEST_IMAGE_CHOOSE_3 = 333
        private const val REQUEST_IMAGE_CHOOSE_4 = 444

        private var mCurrentPhotoPath: String? = null

        private var auxFile1:String? = "null"
        private var auxFile2:String? = "null"
        private var auxFile3:String? = "null"
        private var auxFile4:String? = "null"

        private const val PERMISSION_REQUEST_CODE: Int = 101

    }

    private val apiClient = ApiClient()

    private var catList: ArrayList<String> = ArrayList()
    private var catIdList:ArrayList<Int> = ArrayList()
    private var catId:Int = 0

    private lateinit var cityName:String
    private var cityId:Int = 0
    private var userId:Int = 0

    private lateinit var viewOne: View
    private lateinit var prefManage: PrefManage
    private lateinit var viewDialog: ViewDialog
    private lateinit var pickImageDialog:Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOne = inflater.inflate(R.layout.fragment_add, container, false)

        initComponent(viewOne)

        return viewOne
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Initialize UI
    }

    private fun initComponent(view: View){
        catList = arrayListOf()
        catIdList = arrayListOf()
        auxFile1 = "null"
        auxFile2 = "null"
        auxFile3 = "null"
        auxFile4 = "null"

        prefManage = PrefManage(view.context)
        viewDialog = ViewDialog(view.context)
        viewDialog.showDialog()

        cityId = prefManage.getCityId()!!
        cityName = prefManage.getCityName()!!
        userId = prefManage.getUserId()!!

        view.etAddAdPrice.addTextChangedListener(object : NumberTextWatcher(view.etAddAdPrice){})

        view.imgAdd1.setOnClickListener {
            if (auxFile1 == "null"){
                showPickImageDialog(view,
                    REQUEST_IMAGE_CAPTURE_1,
                    REQUEST_IMAGE_CHOOSE_1
                )
            } else {
                showPickImageDialogWithDelete(view,
                    REQUEST_IMAGE_CAPTURE_1,
                    REQUEST_IMAGE_CHOOSE_1,1)
            }
        }

        view.imgAdd2.setOnClickListener {
            if (auxFile2 == "null"){
                showPickImageDialog(view,
                    REQUEST_IMAGE_CAPTURE_2,
                    REQUEST_IMAGE_CHOOSE_2
                )
            } else {
                showPickImageDialogWithDelete(view,
                    REQUEST_IMAGE_CAPTURE_2,
                    REQUEST_IMAGE_CHOOSE_2,2)
            }

        }

        view.imgAdd3.setOnClickListener {
            if (auxFile3 == "null"){
                showPickImageDialog(view,
                    REQUEST_IMAGE_CAPTURE_3,
                    REQUEST_IMAGE_CHOOSE_3
                )
            } else {
                showPickImageDialogWithDelete(view,
                    REQUEST_IMAGE_CAPTURE_3,
                    REQUEST_IMAGE_CHOOSE_3,3)
            }
        }

        view.imgAdd4.setOnClickListener {
            if (auxFile4 == "null"){
                showPickImageDialog(view,
                    REQUEST_IMAGE_CAPTURE_4,
                    REQUEST_IMAGE_CHOOSE_4
                )
            } else {
                showPickImageDialogWithDelete(view,
                    REQUEST_IMAGE_CAPTURE_4,
                    REQUEST_IMAGE_CHOOSE_4,4)
            }
        }

        view.tvAddCityName.text = cityName


        // Setup Spinner
        val spinner:MaterialSpinner = view.findViewById(R.id.spinner)

        try {
            getCategories(view)
        } catch (e:Exception){

        }
        spinner.setOnItemSelectedListener { view, position, id, item ->

            for (i in 0 until catIdList.size){
                if (item== catList[i]){
                    catId = catIdList[i]
                }
            }
        }
        spinner.hint = "دسته بندی"


        view.btnPostAdd.setOnClickListener {

            if (isNetworkAvailable()){
                val adImages = ArrayList<String>()
                if (auxFile1 != "null"){
                    adImages.add(auxFile1!!)
                }

                if (auxFile2 != "null"){
                    adImages.add(auxFile2!!)
                }

                if (auxFile3 != "null"){
                    adImages.add(auxFile3!!)
                }

                if (auxFile4 != "null"){
                    adImages.add(auxFile4!!)
                }

                if (auxFile1 == "null" && auxFile2 == "null" && auxFile3 == "null" && auxFile4 == "null"){
                    adImages.add("null")
                }

                val adName = view.etAddAdTitle.text.toString().trim()
                val adDesc = view.etAddAdDesc.text.toString().trim()
                val adPrice = view.etAddAdPrice.text.toString().trim()

                if (spinner.text == "املاک"){
                    catId = 4
                }
                try {
                    if (validate(adName,adDesc,adPrice)){
                        viewDialog.showDialog()
                        postAdd("add",adName,adDesc,adImages,adPrice,
                            catId,
                            userId,
                            cityId
                        )
                    }
                } catch (e:Exception){

                }

            } else {
                Toast.makeText(activity,R.string.no_internet,Toast.LENGTH_SHORT).show()
            }




        }

    }

    private fun validate(adName: String,adDesc: String,adPrice:String):Boolean {
        var valid = true

        if (adDesc.isEmpty() || adName.length < 3 )
        {
            tilAddAdName.error = "عنوان باید حداقل ۳ حرف باشد"
            valid = false
        } else {
            tilAddAdName.error = null
        }

        if (adDesc.isEmpty() || adDesc.length < 3)
        {
            tilAddAdDesc.error = "توضیحات باید حداقل ۳ حرف باشد"
            valid = false
        } else {
            tilAddAdDesc.error = null
        }

        if (adPrice.isEmpty())
        {
            tilAddAdPrice.error = "قیمت نباید خالی باشد"
            valid = false
        } else {
            tilAddAdPrice.error = null
        }

        return valid
    }


    // GET CATEGORIES
    private fun getCategories(view: View){
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.getCategories("priority",4)
        call.enqueue( object : Callback<CategoriesResponse> {
            override fun onResponse(call: Call<CategoriesResponse>?, response: Response<CategoriesResponse>?) {

                val resMsg = response!!.body()!!.success

                if (resMsg == 1){
                    val categories = response.body()!!.categories!!
                    catId =categories[0].categoryId!!.toInt()
                    for (i in 0 until categories.size){
                        catList.add(categories[i].categoryName.toString())
                        catIdList.add(categories[i].categoryId!!.toInt())
                    }
                    catList.let { view.spinner.setItems(it) }
                    viewDialog.hideDialog()
                } else {
                    viewDialog.hideDialog()
                    showWarningDialog()
                }
            }

            override fun onFailure(call: Call<CategoriesResponse>?, t: Throwable?) {
                viewDialog.hideDialog()
                showWarningDialog()
            }
        })
    }

    private fun showPickImageDialog(view: View, captureRequestCode:Int, chooseRequestCode:Int){
        pickImageDialog = Dialog(view.context)
        pickImageDialog.setContentView(R.layout.sheet_list)
        pickImageDialog.setCancelable(true)

        var lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(pickImageDialog.window.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        val btnPickCamera:LinearLayout = pickImageDialog.findViewById(
            R.id.btnPickCamera
        )
        val btnPickGallery:LinearLayout = pickImageDialog.findViewById(
            R.id.btnPickGallery
        )

        btnPickCamera.setOnClickListener {
            if (checkPersmission(view)) takePicture(captureRequestCode) else requestPermission()

            hidePickImageDialog()
        }

        btnPickGallery.setOnClickListener {
            if (checkPersmission(view)) openAlbum(chooseRequestCode) else requestPermission()

            hidePickImageDialog()
        }

        pickImageDialog.show()
        pickImageDialog.window.attributes = lp

    }

    private fun hidePickImageDialog(){
        pickImageDialog.dismiss()
    }

    private fun showPickImageDialogWithDelete(view: View, captureRequestCode:Int, chooseRequestCode:Int, imageId:Int){
        pickImageDialog = Dialog(view.context)
        pickImageDialog.setContentView(R.layout.sheet_list_delete)
        pickImageDialog.setCancelable(true)

        var lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(pickImageDialog.window.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        val btnPickCamera:LinearLayout = pickImageDialog.findViewById(
            R.id.btnPickCamera
        )
        val btnPickGallery:LinearLayout = pickImageDialog.findViewById(
            R.id.btnPickGallery
        )
        val btnDeleteImage:LinearLayout = pickImageDialog.findViewById(
            R.id.btnDeleteImage
        )

        btnPickCamera.setOnClickListener {
            if (checkPersmission(view)) takePicture(captureRequestCode) else requestPermission()

            hidePickImageDialogWithDelete()
        }

        btnPickGallery.setOnClickListener {
            if (checkPersmission(view)) openAlbum(chooseRequestCode) else requestPermission()

            hidePickImageDialogWithDelete()
        }

        btnDeleteImage.setOnClickListener {
            hidePickImageDialogWithDelete()
            when(imageId){
                1 -> {
                    if (auxFile2 !="null") {
                        auxFile1 =
                            auxFile2
                        auxFile2 = "null"
                        imgAdd1.scaleType = ImageView.ScaleType.CENTER_CROP
                        imgAdd1.setImageDrawable(imgAdd2.drawable)
                        imgAdd2.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        imgAdd2.setImageResource(R.drawable.add_img)
                        if (auxFile3 != "null") {
                            auxFile2 =
                                auxFile3
                            auxFile3 = "null"
                            imgAdd2.scaleType = ImageView.ScaleType.CENTER_CROP
                            imgAdd2.setImageDrawable(imgAdd3.drawable)
                            imgAdd3.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            imgAdd3.setImageResource(R.drawable.add_img)

                            if (auxFile4 != "null") {
                                auxFile3 =
                                    auxFile4
                                auxFile4 = "null"
                                imgAdd3.scaleType = ImageView.ScaleType.CENTER_CROP
                                imgAdd3.setImageDrawable(imgAdd4.drawable)
                                imgAdd4.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                imgAdd4.setImageResource(R.drawable.add_img)
                            }
                        }
                    } else {
                        auxFile1 = "null"
                        imgAdd1.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        imgAdd1.setImageResource(R.drawable.add_img)
                    }

                }
                2 -> {
                    if (auxFile3 != "null"){
                        auxFile2 =
                            auxFile3
                        auxFile3 = "null"
                        imgAdd2.scaleType = ImageView.ScaleType.CENTER_CROP
                        imgAdd2.setImageDrawable(imgAdd3.drawable)
                        imgAdd3.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        imgAdd3.setImageResource(R.drawable.add_img)

                        if (auxFile4 != "null"){
                            auxFile3 =
                                auxFile4
                            auxFile4 = "null"
                            imgAdd3.scaleType = ImageView.ScaleType.CENTER_CROP
                            imgAdd3.setImageDrawable(imgAdd4.drawable)
                            imgAdd4.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            imgAdd4.setImageResource(R.drawable.add_img)
                        }
                    } else {
                        auxFile2 = "null"
                        imgAdd2.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        imgAdd2.setImageResource(R.drawable.add_img)
                    }
                }
                3 -> {
                    if(auxFile4 != null){
                        auxFile3 =
                            auxFile4
                        auxFile4 = "null"
                        imgAdd3.scaleType = ImageView.ScaleType.CENTER_CROP
                        imgAdd3.setImageDrawable(imgAdd4.drawable)
                        imgAdd4.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        imgAdd4.setImageResource(R.drawable.add_img)
                    } else {
                        auxFile3 = "null"
                        imgAdd3.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        imgAdd3.setImageResource(R.drawable.add_img)
                    }

                }
                4 -> {
                    auxFile4 = "null"
                    imgAdd4.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    imgAdd4.setImageResource(R.drawable.add_img)
                }
            }
        }

        pickImageDialog.show()
        pickImageDialog.window.attributes = lp

    }

    private fun hidePickImageDialogWithDelete(){
        pickImageDialog.dismiss()
    }

    private fun showInfoDialog(view: View) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_info)
        dialog.setCancelable(true)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        dialog.findViewById<AppCompatButton>(R.id.bt_close).setOnClickListener { v ->
            auxFile1 = "null"
            auxFile2 = "null"
            auxFile3 = "null"
            auxFile4 = "null"
            imgAdd1.scaleType = ImageView.ScaleType.CENTER_INSIDE
            imgAdd1.setImageResource(R.drawable.add_img)
            imgAdd2.scaleType = ImageView.ScaleType.CENTER_INSIDE
            imgAdd2.setImageResource(R.drawable.add_img)
            imgAdd3.scaleType = ImageView.ScaleType.CENTER_INSIDE
            imgAdd3.setImageResource(R.drawable.add_img)
            imgAdd4.scaleType = ImageView.ScaleType.CENTER_INSIDE
            imgAdd4.setImageResource(R.drawable.add_img)

            view.etAddAdTitle.setText("")
            view.etAddAdDesc.setText("")
            view.etAddAdPrice.setText("")

            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun postAdd(mode:String,adName:String,adDesc:String,adImages:ArrayList<String>,
                        adPrice:String,categoryId:Int,userId:Int,cityId:Int){
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.postAd(mode,adName,adDesc,adImages,adPrice,categoryId,userId,cityId)

        call.enqueue(object : Callback<MSG> {

            override fun onResponse(call: Call<MSG>, response: Response<MSG>) {
                val resResult = response.body()!!.success
                val resMsg = response.body()!!.message
                if (resResult == 1) {
                    showInfoDialog(viewOne)
                } else {
                    Toast.makeText(activity!!,resMsg,Toast.LENGTH_LONG).show()
                }
                viewDialog.hideDialog()
            }

            override fun onFailure(call: Call<MSG>, t: Throwable) {
                Toast.makeText(activity!!, R.string.network_failure_try,Toast.LENGTH_LONG).show()
                viewDialog.hideDialog()
            }

        })
    }

    private fun takePicture(captureRequestCode:Int) {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile()

        val uri: Uri = FileProvider.getUriForFile(
            activity!!,
            "ir.staryas.doorway.fileprovider",
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, captureRequestCode)

    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            REQUEST_IMAGE_CAPTURE_1 -> {
                //auxFile1 = saveImage(resultCode,imgAdd1)
                if (resultCode == Activity.RESULT_OK) {
                    displayImage(mCurrentPhotoPath)
                }
            }
            REQUEST_IMAGE_CAPTURE_2 -> {
                //auxFile2 = saveImage(resultCode,imgAdd2)
                if (resultCode == Activity.RESULT_OK) {
                    displayImage(mCurrentPhotoPath)
                }
            }
            REQUEST_IMAGE_CAPTURE_3 -> {
                //auxFile3 = saveImage(resultCode,imgAdd3)
                if (resultCode == Activity.RESULT_OK) {
                    displayImage(mCurrentPhotoPath)
                }
            }
            REQUEST_IMAGE_CAPTURE_4 -> {
                //auxFile4 = saveImage(resultCode,imgAdd4)
                if (resultCode == Activity.RESULT_OK) {
                    displayImage(mCurrentPhotoPath)
                }
            }
            REQUEST_IMAGE_CHOOSE_1 -> {
                if (resultCode == Activity.RESULT_OK) {
                    handleImage(data)
                }
            }
            REQUEST_IMAGE_CHOOSE_2 -> {
                if (resultCode == Activity.RESULT_OK) {
                    handleImage(data)
                }
            }
            REQUEST_IMAGE_CHOOSE_3 -> {
                if (resultCode == Activity.RESULT_OK) {
                    handleImage(data)
                }
            }
            REQUEST_IMAGE_CHOOSE_4 -> {
                if (resultCode == Activity.RESULT_OK) {
                    handleImage(data)
                }
            }
        }
    }

    private fun checkPersmission(view: View): Boolean {
        return (ContextCompat.checkSelfPermission(view.context, CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(view.context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(activity!!, arrayOf(READ_EXTERNAL_STORAGE, CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    //takePicture()

                } else {
                    Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }

            else -> {

            }
        }
    }

    private fun openAlbum(chooseImageRequest:Int){
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, chooseImageRequest)
    }


    private fun handleImage(data: Intent?){
        var imagePath: String? = null
        val uri = data!!.data
        if (DocumentsContract.isDocumentUri(activity!!, uri)){
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri.authority){
                val id = docId.split(":")[1]
                val selsetion = MediaStore.Images.Media._ID + "=" + id
                imagePath = imagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selsetion)
            }
            else if ("com.android.providers.downloads.documents" == uri.authority){
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = imagePath(contentUri, null)
            }
        }
        else if ("content".equals(uri.scheme, ignoreCase = true)){
            imagePath = imagePath(uri, null)
        }
        else if ("file".equals(uri.scheme, ignoreCase = true)){
            imagePath = uri.path
        }
        displayImage(imagePath)
    }

    private fun imagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
        val cursor = activity!!.contentResolver.query(uri, null, selection, null, null )
        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }

    private fun displayImage(imagePath: String?){
        if (imagePath != null) {
            val compressedImageFile = Compressor(activity).compressToBitmap(File(imagePath))

            /*val cm = Compressor(activity)
                .setMaxWidth(640)
                .setMaxHeight(480)
                .setQuality(50)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).absolutePath
                )
                .compressToBitmap(File(imagePath))*/

            val baos = ByteArrayOutputStream()
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG,50,baos)
            val b:ByteArray = baos.toByteArray()



            val requestOption = RequestOptions()
                .centerCrop()
                .transforms(CenterCrop(), RoundedCorners(10))

            when {
                auxFile1 == "null" -> {
                    auxFile1 = Base64.encodeToString(b,Base64.DEFAULT)
                    imgAdd1.scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(activity!!).load(compressedImageFile)
                        .apply(requestOption)
                        .into(imgAdd1)
                }
                auxFile2 =="null" -> {
                    imgAdd2.scaleType = ImageView.ScaleType.CENTER_CROP
                    auxFile2 = Base64.encodeToString(b,Base64.DEFAULT)
                    Glide.with(activity!!).load(compressedImageFile)
                        .apply(requestOption)
                        .into(imgAdd2)
                }
                auxFile3 == "null" -> {
                    imgAdd3.scaleType = ImageView.ScaleType.CENTER_CROP
                    auxFile3 = Base64.encodeToString(b,Base64.DEFAULT)
                    Glide.with(activity!!).load(compressedImageFile)
                        .apply(requestOption)
                        .into(imgAdd3)
                }
                auxFile4 == "null" -> {
                    imgAdd4.scaleType = ImageView.ScaleType.CENTER_CROP
                    auxFile4 = Base64.encodeToString(b,Base64.DEFAULT)
                    Glide.with(activity!!).load(compressedImageFile)
                        .apply(requestOption)
                        .into(imgAdd4)
                }
            }
        }
        else {
            Toast.makeText(activity, "دریافت عکس ناموفق بود", Toast.LENGTH_SHORT).show()
        }
    }



    @Throws(IOException::class)
    private fun createFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    private fun showWarningDialog() {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_warning)
        dialog.setCancelable(false)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        (dialog.findViewById(R.id.btnWarningTry) as AppCompatButton).setOnClickListener { v ->
            initComponent(viewOne)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window.attributes = lp
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

}