// ApiService.kt
package com.example.phishingapp.backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
    val userPassword: String
)

data class UserDetails(
    val userUsername: String,
    val userEmail: String
)

data class LoginResponse(
    val message: String,
    val error: String? = null
)

interface ApiService {
    @POST("/usersignup")
    fun signup(@Body signupRequest: SignupRequest): Call<SignupResponse>

    @POST("/userlogin")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    // New method for retrieving user details
    @GET("/getUserDetails")
    fun getUserDetails(@Query("userId") userId: String): Call<UserDetails>
}
