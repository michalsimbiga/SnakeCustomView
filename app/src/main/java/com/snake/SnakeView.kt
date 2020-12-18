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
        var left: Float = 0f,
        var top: Float = 0f,
        var right: Float = 0f,
        var bottom: Float = 0f
    )

    data class GameSquare(
        var x: Int = 0,
        var y: Int = 0,
        var left: Float = 0f,
        var top: Float = 0f,
        var right: Float = 0f,
        var bottom: Float = 0f,
        var content: SquareContent
    )

    sealed class SquareContent {
        object EmptySquare : SquareContent()
        class Food(val gameSquareX: Int, val gameSquareY: Int) : SquareContent()
    }


//    val gameBoard: RectF = RectF(0f, 0f, 0f, 0f)

    val gameBoard2: Board = Board(0f, 0f, 0f, 0f)

    val listOfGameSquare = mutableListOf<GameSquare>()

    var zeroY = 0f
    var heightPadding = 0f

    var numberOfElements = 5
    var elementWidth = 0f

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val max = max(width.toFloat(), height.toFloat())
        val min = min(width.toFloat(), height.toFloat())

        elementWidth = min / numberOfElements
        heightPadding = ((max - min) / 2)
        zeroY = (max - heightPadding)

        gameBoard2.apply {
            this.left = 0f
            this.top = 0f + heightPadding
            this.right = min
            this.bottom = zeroY
        }

        listOfGameSquare.clear()
        for (elementY in 0 until numberOfElements) {
            for (elementX in 0..numberOfElements) {
                val squareLeft = gameBoard2.left + (elementX * elementWidth)
                val squareTop = gameBoard2.top + (elementY * elementWidth)
                val squareRight = squareLeft + elementWidth
                val squareBottom = squareTop + elementWidth
                listOfGameSquare.add(
                    GameSquare(elementX, elementY, squareLeft, squareTop, squareRight, squareBottom)
                )
            }
        }

    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)


        with(gameBoard2) {
            canvas?.drawRect(
                left,
                top,
                right,
                bottom,
                blackPaint
            )
        }

        drawBoardSquares(canvas)

    }

    private fun drawBoardSquares(canvas: Canvas?) = canvas?.apply {
        listOfGameSquare.forEach { square ->
            drawRect(
                square.left + 2f,
                square.top + 2f,
                square.right - 2f,
                square.bottom - 2f,
                yellowPaint
            )

            drawSquareContent(square, canvas)
        }
    }

    private fun drawSquareContent(gameSquare: GameSquare, canvas: Canvas?) = canvas?.apply {
        when (gameSquare.content) {
            is SquareContent.EmptySquare -> return@apply
            is SquareContent.Food -> drawFood(gameSquare, canvas)
        }
    }


    private fun drawFood(gameSquare: GameSquare, canvas: Canvas?) = canvas?.apply {
        drawRect(gameSquare.left, gameSquare.top, gameSquare.bottom, gameSquare.right, redPaint)
    }
}