package com.snake

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

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

    data class Board(
        var topX: Float = 0f,
        var topY: Float = 0f,
        var bottomX: Float = 0f,
        var bottomY: Float = 0f
    )

    data class GameSquare(
        var x: Int = 0,
        var y: Int = 0,
        var topX: Float = 0f,
        var topY: Float = 0f,
        var bottomX: Float = 0f,
        var bottomY: Float = 0f
    )

//    val gameBoard: RectF = RectF(0f, 0f, 0f, 0f)

    val gameBoard2: Board = Board(0f, 0f, 0f, 0f)

    val listOfGameSquare = mutableListOf<GameSquare>()

    var zeroY = 0f
    var heightPadding = 0f

    var numberOfElements = 11
    var elementWidth = 0f

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val max = max(width.toFloat(), height.toFloat())
        val min = min(width.toFloat(), height.toFloat())

        heightPadding = ((max - min) / 2)

        elementWidth = max / numberOfElements

        listOfGameSquare.clear()
        for (elementY in 0..numberOfElements) {
            for (elementX in 0..numberOfElements) {
                listOfGameSquare.add(
                    GameSquare(
                        elementX,
                        elementY,
                        elementX * elementWidth,
                        elementX * elementWidth + heightPadding,
                        elementX * elementWidth + elementWidth,
                        elementX * elementWidth + elementWidth

                    )
                )
            }
        }

        zeroY = (max - heightPadding)

//        gameBoard.set(
//            0f,
//            0f + heightPadding,
//            0f + min,
//            zeroY
//        )

        gameBoard2.apply {
            topX = 0f
            topY = 0f + heightPadding
            bottomX = 0f + min
            bottomY = zeroY
        }
    }

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


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

//        canvas?.drawRect(gameBoard, greenPaint)

        with(gameBoard2) {
            canvas?.drawRect(
                topX,
                topY,
                bottomX,
                bottomY,
                blackPaint
            )
        }

        drawBoardSquares(canvas)

    }

    private fun drawBoardSquares(canvas: Canvas?) = canvas?.apply {

        listOfGameSquare.forEach { square ->
            drawRect(square.topX, square.topY, square.bottomX, square.bottomY, yellowPaint)
        }

    }
}