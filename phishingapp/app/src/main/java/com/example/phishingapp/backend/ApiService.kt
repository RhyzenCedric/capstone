// ApiService.kt
package com.example.phishingapp.backend

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*

// Signup Request/Response
data class SignupRequest(
    @SerializedName("userusername")
    val username: String,

    @SerializedName("userpassword")
    val password: String
)

data class SignupResponse(
    val message: String
)

// Login Request/Response
data class LoginRequest(
    @SerializedName("userusername")
    val username: String,

    @SerializedName("userpassword")
    val password: String
)

data class LoginResponse(
    val message: String,

    @SerializedName("userid")
    val userId: Int,

    @SerializedName("userusername")
    val username: String,

    @SerializedName("userpassword")
    val password: String,

    val error: String? = null
)

// User Details
data class UserDetails(
    @SerializedName("userid")
    val userId: Int,

    @SerializedName("userusername")
    val username: String,

    @SerializedName("userpassword")
    val password: String  // Only include if needed for updates
)

// Report Handling
data class ReportRequest(
    @SerializedName("userid")
    val userId: Int,

    @SerializedName("link_reported")
    val linkReported: String,

    @SerializedName("report_description")
    val description: String
)

data class ReportResponse(
    val message: String,
    val username: String?
)

// Threat Listing
data class Threat(
    @SerializedName("link_id")
    val id: Int,

    @SerializedName("url_link")
    val url: String,

    @SerializedName("tld")
    val domain: String,

    @SerializedName("date_verified")
    val date: String,

    @SerializedName("reported_by")
    val reporter: String?
)

// Profile Update
data class UpdateRequest(
    @SerializedName("newUsername")
    val newUsername: String?,

    @SerializedName("currentPassword")
    val currentPassword: String?,

    @SerializedName("newPassword")
    val newPassword: String?
)

data class UpdateResponse(
    val message: String
)

data class Infographic(
    val image_url: String,
    val title_text: String,
    val description: String,

)

data class WhitelistResponse(
    @SerializedName("original_url")
    val url: String,
    @SerializedName("domain")
    val domain: String,
    @SerializedName("tld")
    val tld: String
)
data class BlacklistResponse(
    @SerializedName("original_url")
    val url: String,
    @SerializedName("domain")
    val domain: String,
    @SerializedName("tld")
    val tld: String
)

interface ApiService {
    // Authentication
    @POST("/usersignup")
    fun signup(@Body request: SignupRequest): Call<SignupResponse>

    @POST("/userlogin")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // User Data
    @GET("/users/{id}")
    fun getUserDetails(@Path("id") userId: Int): Call<UserDetails>

    @PUT("/users/{id}")
    fun updateProfile(
        @Path("id") userId: Int,
        @Body updateRequest: UpdateRequest  // Use data class instead of Map
    ): Call<UpdateResponse>

    // Reporting System
    @POST("/submitreport")
    fun submitReport(@Body request: ReportRequest): Call<ReportResponse>

    @GET("/links/{userid}")
    fun getUserLinks(@Path("userid") userid: Int): Call<List<Threat>>

    // Admin Endpoints (if needed)
    @GET("/reports")
    fun getAllReports(): Call<List<ReportResponse>>

    @POST("/reports/approve")
    fun approveReport(@Body approvalData: Map<String, Any>): Call<ReportResponse>

    @GET("/whitelist")
    fun getWhitelist(): Call<List<WhitelistResponse>>
    @GET("/blacklist")
    fun getBlacklist(): Call<List<BlacklistResponse>>
}