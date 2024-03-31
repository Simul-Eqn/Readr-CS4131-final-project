package com.example.readr.presentation.onboarding

import com.example.readr.R

data class Page(
    val title:String,
    val description: String,
    val light_image: Any,
    val dark_image: Any?=null,
)

