// ApiService.kt
package com.example.phishingapp.backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class SignupRequest(
    val userUsername: String,
    val userEmail: String,
    val userPassword: String
)

data class SignupResponse(
    val message: String
)

data class LoginRequest(
    val userUsername: String,
    val userPassword: String,

)

data class UserDetails(
    val userUsername: String,
    val userEmail: String,
    val userId: Int?,
)

data class LoginResponse(
    val message: String,
    val userId: Int?,
    val error: String? = null
)

data class ReportRequest(
    val userId: Int?, // The user ID from the database
    val link_reported: String,
    val report_description: String
)

data class ReportResponse(
    val message: String
)

interface ApiService {
    @POST("/usersignup")
    fun signup(@Body signupRequest: SignupRequest): Call<SignupResponse>

    @POST("/userlogin")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    // New method for retrieving user details
    @GET("/users/{id}")  // Use {id} to pass the userId as a URL parameter
    fun getUserDetails(@Path("id") userId: Int): Call<UserDetails>

    @POST("/submitreport")
    fun submitReport(@Body reportRequest: ReportRequest): Call<ReportResponse>
}

