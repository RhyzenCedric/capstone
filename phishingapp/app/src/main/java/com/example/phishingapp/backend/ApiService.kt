package com.example.phishingapp.backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

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

data class LoginResponse(
    val message: String,
    val error: String? = null
)

interface ApiService {
    @POST("/usersignup")
    fun signup(@Body signupRequest: SignupRequest): Call<SignupResponse>

    @POST("/userlogin")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
}

