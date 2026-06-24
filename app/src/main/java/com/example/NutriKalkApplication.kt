package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class NutriKalkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            Log.d("NutriKalkApplication", "FirebaseApp úspěšně inicializován v Application třídě.")
        } catch (e: Exception) {
            Log.e("NutriKalkApplication", "Chyba při inicializaci FirebaseApp", e)
        }
    }
}
