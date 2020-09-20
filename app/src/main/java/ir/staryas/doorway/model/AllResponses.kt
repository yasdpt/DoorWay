package ir.staryas.doorway.model

import com.google.gson.annotations.SerializedName


class MSG {
    @SerializedName("success")
    val success:Int? = null
    @SerializedName("message")
    val message:String? = null
}

class USRMSG {
    @SerializedName("success")
    val success:Int? = null
    @SerializedName("message")
    val message:String? = null
    @SerializedName("user")
    val user: List<User>? = null
}


class CategoriesResponse {
    @SerializedName("success")
    var success:Int? = null
    @SerializedName("categories")
    var categories:List<Categories>? = null

}

class CitiesResponse {
    @SerializedName("success")
    var success:Int? = null
    @SerializedName("cities")
    var cities:List<Cities>? = null
}

class AdsResponse {
    @SerializedName("success")
    var success:Int? = null
    @SerializedName("ads")
    var ads:MutableList<Ads>? = null
}

class User {
    @SerializedName("user_id")
    val userId:String? = null
    @SerializedName("fullname")
    val fullName:String? = null
    @SerializedName("username")
    val username:String? = null
    @SerializedName("email")
    val email:String? = null
    @SerializedName("phone")
    val phone:String? = null

}

class Categories {
    @SerializedName("category_id")
    var categoryId:String? = null
    @SerializedName("category")
    var categoryName:String? = null
    @SerializedName("count")
    var count:String? = null
}



class Cities {
    @SerializedName("city_id")
    var cityId:String? = null
    @SerializedName("city")
    var cityName:String? = null
}

class Ads {
    @SerializedName("ad_id")
    var adId:String? = null
    @SerializedName("ad_name")
    var adName:String? = null
    @SerializedName("ad_desc")
    var adDesc:String? = null
    @SerializedName("ad_price")
    val adPrice:String? = null
    @SerializedName("ad_images")
    var adImages:String? = null
    @SerializedName("category")
    var categoryName:String? = null
    @SerializedName("city")
    var cityName:String? = null
    @SerializedName("email")
    var email:String? = null
    @SerializedName("phone")
    var phone:String? = null
    @SerializedName("is_published")
    var isPublished:String? = null
    @SerializedName("created_at")
    var createdAt:String? = null
}