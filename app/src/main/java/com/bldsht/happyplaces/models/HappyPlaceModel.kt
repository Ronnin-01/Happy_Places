package com.bldsht.happyplaces.models

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

@SuppressLint("ParcelCreator")
data class HappyPlaceModel (
    val id : Int,
    val title : String?,
    val image : String?,
    val description : String?,
    val date : String?,
    val location : String?,
    val latitude : Double,
    val longitude : Double
) : Serializable
