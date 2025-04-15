# Android Graphics Handling Guide

When implementing Android games or graphics-heavy applications:

## Vector Drawable and Bitmap Fundamentals
- BitmapFactory.decodeResource() works ONLY with bitmap resources (.png, .jpg, .webp)
- Vector drawables (.xml) CANNOT be loaded directly using BitmapFactory.decodeResource()
- Always check if resources are vector or bitmap before choosing loading method

## Correct Implementation Pattern
```kotlin
// Helper function for safe drawable loading
private fun getBitmapFromDrawable(context: Context, drawableId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    
    // Create bitmap from vector drawable
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
```

## Required Dependencies
```kotlin
// Add to build.gradle.kts
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("androidx.vectordrawable:vectordrawable:1.2.0")
```

## Common Pitfalls
- Never assume decodeResource() will work with any resource type
- Always handle null returns from drawable loading functions
- When using Canvas with SurfaceView, ensure proper resource conversion
- Test on multiple device densities to verify graphics scaling correctly

## Performance Considerations
- Cache converted bitmaps when possible rather than converting repeatedly
- Consider resource size and memory constraints on lower-end devices
- Use appropriate bitmap configurations (ARGB_8888 vs RGB_565) based on needs

Following these guidelines will prevent the "decodeResource(...) must not be null" exception and similar graphics-loading errors in Android applications.