//package com.example.phishingapp
//
//import android.os.Bundle
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.viewpager2.widget.ViewPager2
//
//class InfographicActivity : AppCompatActivity() {
//
//    private lateinit var titleTextView: TextView
//    private lateinit var viewPager: ViewPager2
//    private lateinit var adapter: InfographicAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_infographic)
//
//        titleTextView = findViewById(R.id.infographicTitle)
//        viewPager = findViewById(R.id.infographicViewPager)
//
//        val category = intent.getStringExtra("CATEGORY") ?: "phishing"
//
//        titleTextView.text = category.capitalize()
//
//        val images = when (category) {
//            "phishing" -> listOf(
////                R.drawable.phishing_infographic1,
////                R.drawable.phishing_infographic2
//            )
//            "smishing" -> listOf(
////                R.drawable.smishing_infographic1,
////                R.drawable.smishing_infographic2
//            )
//            "scamming" -> listOf(
////                R.drawable.scamming_infographic1,
////                R.drawable.scamming_infographic2
//            )
//            else -> emptyList()
//        }
//
//        adapter = InfographicAdapter(images)
//        viewPager.adapter = adapter
//    }
//}