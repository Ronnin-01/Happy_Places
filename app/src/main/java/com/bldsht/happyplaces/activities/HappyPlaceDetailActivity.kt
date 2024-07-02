package com.bldsht.happyplaces.activities

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bldsht.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import com.bldsht.happyplaces.models.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHappyPlaceDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        var happyPlaceDetailModel: HappyPlaceModel? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailModel = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }
        if (happyPlaceDetailModel != null){
            setSupportActionBar(binding.toolbarHappyPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailModel.title

            binding.toolbarHappyPlaceDetail.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            binding.ivPlaceImage.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            binding.tvDescription.text = happyPlaceDetailModel.description
            binding.tvLocation.text = happyPlaceDetailModel.location
        }
    }
}