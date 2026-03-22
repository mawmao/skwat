package com.humayapp.scout.feature.form.api

//  Region Code: ^06 or ^60 [To be confirmed]
//  Province Code: (04|06|19|30|45|79)
//  City, Municipality Code:  \\d{2}
//  Barangay Code: \\d{3}
const val region = "(60)"
const val province = "(04|06|19|30|45|79)"
const val cityMunicipality = "(\\d{2})"
const val barangay = "(\\d{3})"
val mfidPattern = Regex("$region$province$cityMunicipality$barangay$")
