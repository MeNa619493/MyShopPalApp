package com.example.myshoppal.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Address (
    val user_id: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val zipCode: String = "",
    val additionalNote: String = "",
    val type: String = "",
    val otherDetails: String = "",
    var id: String = "" ) : Parcelable