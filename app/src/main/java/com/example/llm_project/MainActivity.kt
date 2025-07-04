package com.example.llm_project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.llm_project.databinding.ActivityMainBinding  // Add this import

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding  // Correct binding declaration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding properly
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)  // Use binding.root

        // Verify fragment_container exists in activity_main.xml
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MainInteractionFragment())
            .commit()
    }
}