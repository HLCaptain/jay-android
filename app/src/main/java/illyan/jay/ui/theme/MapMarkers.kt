package illyan.jay.ui.theme

import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap

data class MapMarkers(
    val height: Int,
    private val locationPuckDrawableId: Int,
    private val poiDrawableId: Int,
    private val pathStartDrawableId: Int,
    private val pathEndDrawableId: Int,
) {
    fun getLocationPuckBitmap(context: Context, height: Int = this.height) = getDrawableAsBitmap(context, locationPuckDrawableId, height)
    fun getPoiBitmap(context: Context, height: Int = this.height) = getDrawableAsBitmap(context, poiDrawableId, height)
    fun getPathStartBitmap(context: Context, height: Int = this.height) = getDrawableAsBitmap(context, pathStartDrawableId, height)
    fun getPathEndBitmap(context: Context, height: Int = this.height) = getDrawableAsBitmap(context, pathEndDrawableId, height)

    private fun getDrawableAsBitmap(
        context: Context,
        drawableId: Int,
        height: Int = this.height
    ): Bitmap {
        val drawable = AppCompatResources.getDrawable(
            context,
            drawableId
        )
        return drawable!!.toBitmap(
            width = height * drawable.intrinsicWidth / drawable.intrinsicHeight,
            height = height
        )
    }
}
