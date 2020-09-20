package ir.staryas.doorway.networking

import ir.staryas.doorway.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Login
    @FormUrlEncoded
    @POST("Login.php")
    fun login(@Field("username") username:String,@Field("password") password:String): Call<USRMSG>

    // SignUp
    @FormUrlEncoded
    @POST("SignUp.php")
    fun signUp(@Field("fullname") fullName:String,@Field("username") username:String,
               @Field("email") email:String,@Field("password") password:String,@Field("phone") phone:String): Call<MSG>

    // ManageAds ADD
    @FormUrlEncoded
    @POST("ManageAds.php")
    fun postAd(@Field("mode") mode:String, @Field("ad_name") adName:String,@Field("ad_desc") adDesc:String,
                @Field("ad_images[]") adImages:ArrayList<String>,@Field("ad_price") adPrice:String,
                @Field("category_id") categoryId:Int, @Field("user_id") userId:Int,@Field("city_id") cityId:Int): Call<MSG>

    // ManageAds DELETE
    @FormUrlEncoded
    @POST("ManageAds.php")
    fun deleteAd(@Field("mode") mode:String, @Field("ad_id") adId:Int): Call<MSG>

    // PASSWORD RECOVERY
    @GET("PasswordRecovery.php")
    fun recoverPassword(@Query("email") email: String): Call<MSG>

    // GET CATEGORIES
    @GET("GetCategories.php")
    fun getCategories(@Query("orderby") orderby:String,@Query("city_id") cityId: Int): Call<CategoriesResponse>

    // GET CITIES
    @GET("GetCities.php")
    fun getCities(@Query("orderby") orderby: String): Call<CitiesResponse>

    // GET ADS BY CITY
    @GET("GetAds.php")
    fun getAdsByCity(@Query("city") cityId: Int, @Query("page") page:Int,
                     @Query("orderby") orderby: String): Call<AdsResponse>

    // GET ADS BY CATEGORY
    @GET("GetAdsByCat.php")
    fun getAdsByCategory(@Query("city") cityId: Int,@Query("page") page: Int,
                         @Query("orderby") orderby: String,@Query("category") categoryId: Int): Call<AdsResponse>


    // GET ADS BY USER
    @GET("GetAdsByUser.php")
    fun getAdsByUser(@Query("user_id") userId: Int,@Query("page") page: Int,
                         @Query("orderby") orderby: String): Call<AdsResponse>



    // GET ADS BY CITY
    @GET("SearchAds.php")
    fun searchAdsByCity(@Query("mode") mode: String,@Query("query") query: String,@Query("city") cityId: Int, @Query("page") page:Int,
                     @Query("orderby") orderby: String): Call<AdsResponse>

    // GET ADS BY CATEGORY
    @GET("SearchAds.php")
    fun searchAdsByCategory(@Query("mode") mode: String,@Query("query") query: String,@Query("city") cityId: Int,@Query("page") page: Int,
                         @Query("orderby") orderby: String,@Query("category") categoryId: Int): Call<AdsResponse>

}