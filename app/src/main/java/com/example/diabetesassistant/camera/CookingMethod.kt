package com.example.diabetesassistant.camera

import androidx.annotation.StringRes
import com.example.diabetesassistant.R

enum class CookingMethod(@param:StringRes val labelRes: Int) {
    Steam(R.string.cook_steam),
    Braise(R.string.cook_braise),
    StirFry(R.string.cook_stir_fry),
    Boil(R.string.cook_boil),
    ColdDish(R.string.cook_cold),
    Roast(R.string.cook_roast),
    Other(R.string.cook_other),
}
