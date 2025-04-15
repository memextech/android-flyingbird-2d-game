package com.example.flyingbirdsgame

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var gameView: GameView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set window to fullscreen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // Initialize our game view
        gameView = GameView(this)
        
        // Set the game view as the content view
        setContentView(gameView)
    }
    
    override fun onPause() {
        super.onPause()
        gameView.surfaceDestroyed(gameView.holder)
    }
    
    override fun onResume() {
        super.onResume()
        gameView.surfaceCreated(gameView.holder)
    }
}