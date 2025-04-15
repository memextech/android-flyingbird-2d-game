package com.example.flyingbirdsgame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private var thread: Thread? = null
    private var running = false
    private var holder: SurfaceHolder = getHolder()
    private var canvas: Canvas? = null
    private val paint = Paint()
    
    // Game objects
    private var bird: Bird
    private var obstacles = ArrayList<Obstacle>()
    private var background: Background
    
    // Game metrics
    private var screenWidth = 0
    private var screenHeight = 0
    private var score = 0
    private var gameOver = false
    
    // Time tracking for spawning obstacles
    private var lastObstacleTime = System.currentTimeMillis()
    private val obstacleSpawnInterval = 2500L // 2.5 seconds - gives more time between obstacles
    
    init {
        holder.addCallback(this)
        
        // Initialize game objects
        bird = Bird(context, getBitmapFromDrawable(context, R.drawable.bird))
        background = Background(getBitmapFromDrawable(context, R.drawable.background))
        
        isFocusable = true
    }
    
    // Helper function to convert drawable resources to bitmaps
    private fun getBitmapFromDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        
        // Create bitmap with drawable dimensions
        val bitmap = Bitmap.createBitmap(
            drawable?.intrinsicWidth ?: 100,
            drawable?.intrinsicHeight ?: 100,
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        
        return bitmap
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        screenWidth = width
        screenHeight = height
        
        // Position bird on the left side of screen
        bird.x = screenWidth / 6f
        bird.y = screenHeight / 2f
        
        // Initialize background dimensions
        background.width = screenWidth
        background.height = screenHeight
        background.x1 = 0f
        background.x2 = screenWidth.toFloat()
        
        if (!running) {
            running = true
            thread = Thread(this)
            thread?.start()
        }
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        try {
            thread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    
    override fun run() {
        while (running) {
            update()
            draw()
            control()
        }
    }
    
    private fun update() {
        if (gameOver) return
        
        // Update background position
        background.update()
        
        // Update bird position
        bird.update()
        
        // Keep bird within screen bounds
        if (bird.y < 0) bird.y = 0f
        if (bird.y > screenHeight - bird.height) bird.y = screenHeight - bird.height.toFloat()
        
        // Check if it's time to spawn a new obstacle
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastObstacleTime > obstacleSpawnInterval) {
            spawnObstacle()
            lastObstacleTime = currentTime
        }
        
        // Update obstacles and check for collisions
        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obstacle = iterator.next()
            obstacle.update()
            
            // Remove obstacles that have gone off screen
            if (obstacle.x + obstacle.width < 0) {
                iterator.remove()
                score++
            }
            
            // Check for collision with bird
            if (isColliding(bird, obstacle)) {
                gameOver = true
            }
        }
    }
    
    private fun spawnObstacle() {
        // Create obstacles with more variation in height
        val obstacleHeight = Random.nextInt(100, 200)
        
        // Make sure obstacles don't span the whole screen height
        // This ensures there's always a path for the bird
        val maxY = screenHeight - obstacleHeight - 100
        val obstacleY = Random.nextInt(100, maxY)
        
        val obstacle = Obstacle(
            getBitmapFromDrawable(context, R.drawable.obstacle),
            screenWidth.toFloat(),
            obstacleY.toFloat(),
            obstacleHeight
        )
        obstacles.add(obstacle)
    }
    
    private fun isColliding(bird: Bird, obstacle: Obstacle): Boolean {
        return Rect(
            bird.x.toInt(),
            bird.y.toInt(),
            (bird.x + bird.width).toInt(),
            (bird.y + bird.height).toInt()
        ).intersect(
            obstacle.x.toInt(),
            obstacle.y.toInt(),
            (obstacle.x + obstacle.width).toInt(),
            (obstacle.y + obstacle.height).toInt()
        )
    }
    
    private fun draw() {
        if (holder.surface.isValid) {
            try {
                canvas = holder.lockCanvas()
                canvas?.let {
                    // Draw background
                    background.draw(it, paint)
                    
                    // Draw obstacles
                    for (obstacle in obstacles) {
                        obstacle.draw(it, paint)
                    }
                    
                    // Draw bird
                    bird.draw(it, paint)
                    
                    // Draw score
                    paint.color = Color.WHITE
                    paint.textSize = 50f
                    it.drawText("Score: $score", 50f, 50f, paint)
                    
                    // Draw game over text if game is over
                    if (gameOver) {
                        paint.textSize = 100f
                        val text = "Game Over"
                        val textWidth = paint.measureText(text)
                        it.drawText(
                            text,
                            (screenWidth - textWidth) / 2,
                            screenHeight / 2f,
                            paint
                        )
                        
                        paint.textSize = 50f
                        val scoreText = "Score: $score"
                        val scoreTextWidth = paint.measureText(scoreText)
                        it.drawText(
                            scoreText,
                            (screenWidth - scoreTextWidth) / 2,
                            screenHeight / 2f + 100,
                            paint
                        )
                        
                        val restartText = "Tap to restart"
                        val restartTextWidth = paint.measureText(restartText)
                        it.drawText(
                            restartText,
                            (screenWidth - restartTextWidth) / 2,
                            screenHeight / 2f + 200,
                            paint
                        )
                    }
                }
            } finally {
                canvas?.let { holder.unlockCanvasAndPost(it) }
            }
        }
    }
    
    private fun control() {
        try {
            Thread.sleep(17) // ~60 FPS
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    
    fun restart() {
        // Reset game state
        gameOver = false
        score = 0
        obstacles.clear()
        
        // Reset bird position
        bird.y = screenHeight / 2f
        
        lastObstacleTime = System.currentTimeMillis()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (gameOver) {
                    restart()
                } else {
                    // Bird flies upward when screen is touched
                    bird.onTap()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}

class Bird(context: Context, private val bitmap: Bitmap) {
    var x = 0f
    var y = 0f
    var width = 100
    var height = 100
    private var velocity = 0f
    private val gravity = 0.6f     // Reduced gravity for more control
    private val jumpForce = -12f   // Less powerful jump for more manageable movement
    
    init {
        // Set a fixed size for the bird to make gameplay more consistent
        width = 80
        height = 80
    }
    
    fun update() {
        velocity += gravity
        y += velocity
    }
    
    fun onTap() {
        velocity = jumpForce
    }
    
    fun draw(canvas: Canvas, paint: Paint) {
        canvas.drawBitmap(bitmap, x, y, paint)
    }
}

class Obstacle(private val bitmap: Bitmap, var x: Float, var y: Float, val obstacleHeight: Int) {
    var width = 70  // Narrower obstacles for easier navigation
    var height = obstacleHeight
    private val speed = 8f  // Slower speed for better reaction time
    
    init {
        // Fixed width for obstacles
        width = 70
    }
    
    fun update() {
        x -= speed
    }
    
    fun draw(canvas: Canvas, paint: Paint) {
        canvas.drawBitmap(bitmap, null, Rect(
            x.toInt(),
            y.toInt(),
            (x + width).toInt(),
            (y + height).toInt()
        ), paint)
    }
}

class Background(private val bitmap: Bitmap) {
    var x1 = 0f
    var x2 = 0f
    var width = 0
    var height = 0
    private val speed = 5f
    
    fun update() {
        x1 -= speed
        x2 -= speed
        
        // If first background is completely off screen, reset its position
        if (x1 + width < 0) {
            x1 = x2 + width
        }
        
        // If second background is completely off screen, reset its position
        if (x2 + width < 0) {
            x2 = x1 + width
        }
    }
    
    fun draw(canvas: Canvas, paint: Paint) {
        // Draw both background images to create infinite scrolling
        canvas.drawBitmap(bitmap, null, Rect(
            x1.toInt(), 0, (x1 + width).toInt(), height
        ), paint)
        
        canvas.drawBitmap(bitmap, null, Rect(
            x2.toInt(), 0, (x2 + width).toInt(), height
        ), paint)
    }
}