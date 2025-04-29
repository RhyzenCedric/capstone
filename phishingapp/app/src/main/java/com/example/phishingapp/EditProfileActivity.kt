package com.example.phishingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.phishingapp.MainActivity.Companion.TAG
import com.example.phishingapp.backend.RetrofitClient
import com.example.phishingapp.backend.UpdateRequest
import com.example.phishingapp.backend.UpdateResponse
import retrofit2.Call
import retrofit2.Response

class EditProfileActivity : AppCompatActivity() {

    private lateinit var passwordToggle: ImageView
    private lateinit var newPasswordToggle: ImageView
    private var isPasswordVisible = false
    private var isNewPasswordVisible = false

    private lateinit var textViewUsername: TextView
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextNewPassword: EditText
    private lateinit var buttonUpdate: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val username = intent.getStringExtra("userUsername") ?: "Guest"
        val userId = intent.extras?.getInt("userId", -1)

        Log.d("EditProfileActivity", "Received username: $username")
        Log.d("EditProfileActivity", "User ID: $userId")

        // Initialize all views
        passwordToggle = findViewById(R.id.CurrentpasswordToggle)
        newPasswordToggle = findViewById(R.id.NewpasswordToggle)
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextCurrentPassword)
        editTextNewPassword = findViewById(R.id.editTextNewPassword)
        buttonUpdate = findViewById(R.id.buttonUpdate)
        textViewUsername = findViewById(R.id.textViewUsername)

        textViewUsername.text = username

        // Password visibility toggle
        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            editTextPassword.inputType = if (isPasswordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            passwordToggle.setImageResource(
                if (isPasswordVisible) R.drawable.invisible_icon else R.drawable.visible_icon
            )
            editTextPassword.setSelection(editTextPassword.text.length)
        }

        newPasswordToggle.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            editTextNewPassword.inputType = if (isNewPasswordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            newPasswordToggle.setImageResource(
                if (isNewPasswordVisible) R.drawable.invisible_icon else R.drawable.visible_icon
            )
            editTextNewPassword.setSelection(editTextNewPassword.text.length)
        }

        // Example button update click
        buttonUpdate.setOnClickListener {
            updateProfile(userId ?: -1)
            // You can handle the update logic here
        }

        setupNavigationButtons()
    }

    private fun updateProfile(userId: Int) {
        val enteredUsername = editTextUsername.text.toString().trim()
        val currentUsername = textViewUsername.text.toString().trim()
        val newUsername = if (enteredUsername.isEmpty()) currentUsername else enteredUsername

        val currentPassword = editTextPassword.text.toString()
        val newPassword = editTextNewPassword.text.toString()

        val updateRequest = UpdateRequest(
            newUsername = if (enteredUsername.isEmpty()) null else enteredUsername,
            currentPassword = if (currentPassword.isEmpty()) null else currentPassword,
            newPassword = if (newPassword.isEmpty()) null else newPassword
        )

        RetrofitClient.instance.updateProfile(userId, updateRequest)
            .enqueue(object : retrofit2.Callback<UpdateResponse> {
                override fun onResponse(call: Call<UpdateResponse>, response: Response<UpdateResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                        // ✅ Update the TextView to reflect the new username
                        textViewUsername.text = newUsername

                        // ✅ Update the intent with new values
                        intent.putExtra("userUsername", newUsername)
                        intent.putExtra("userId", userId)

                        // Optionally clear input fields after update
                        editTextUsername.text.clear()
                        editTextPassword.text.clear()
                        editTextNewPassword.text.clear()

                        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("userUsername", newUsername)
                        editor.putInt("userId", userId)
                        editor.apply()
                        Log.d("EditProfileActivity", "User ID: $userId")
                        Log.d(TAG, "Updated username: $newUsername")



                    } else {
                        Toast.makeText(this@EditProfileActivity, "Update failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UpdateResponse>, t: Throwable) {
                    Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun setupNavigationButtons() {
        findViewById<ImageView>(R.id.return_icon).setOnClickListener {
            val updatedUsername = textViewUsername.text.toString().trim()

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("userUsername", updatedUsername)

            }
            startActivity(intent)
        }
    }

}
