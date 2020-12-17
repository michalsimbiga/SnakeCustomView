package com.snake

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class SnakeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val rect: RectF = RectF(0f, 0f, 0f, 0f)
    private val rectPath = Path()
    private val cornerRadius = 18f

    private val yellowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
    }

    private val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
    }

    private val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
    }

    private val blackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 5f
    }

    data class Board(var topX, var topY, var bottomX, var bottomY)

    val gameBoard: Board = Board()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

//        widthInstance = measuredWidth.toFloat() / 100f
//        recHeight = measuredHeight.toFloat()
//
//        dataModel?.let { model ->
//            normMin = model.normMin.toFloat()
//            normMax = model.normMax.toFloat()
//
//            firstPoint = calculateWidthPercentage(normMin / 2f) * widthInstance
//            secondPoint = calculateWidthPercentage(normMin) * widthInstance
//            thirdPoint = calculateWidthPercentage(normMax) * widthInstance
//            fourthPoint = calculateWidthPercentage(normMax + (normMin / 2f)) * widthInstance
//
//            rect.set(
//                0f,
//                heightOffset,
//                measuredWidth.toFloat(),
//                measuredHeight.toFloat() - heightOffset
//            )
//            rectPath.reset()
//            rectPath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
//        }
    }
}