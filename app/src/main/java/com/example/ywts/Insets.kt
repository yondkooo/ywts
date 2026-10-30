package com.example.ywts

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/** Системийн самбар, камерын нүхний доор контент орохоос сэргийлж зай нэмнэ. */
fun View.applySystemBarInsets() {
    val l = paddingLeft
    val r = paddingRight
    val b = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val i = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        v.setPadding(l + i.left, v.paddingTop, r + i.right, b + i.bottom)
        insets
    }
}
