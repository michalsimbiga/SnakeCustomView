package com.snake

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlinx.coroutines.*
import kotlin.math.max
import kotlin.math.min

class SnakeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val job = Job()
    private val scopeMain = CoroutineScope(job + Dispatchers.Main)
    private val scopeIO = CoroutineScope(job + Dispatchers.IO)


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
        var content: SquareContent = SquareContent.EmptySquare,
        var leftDirection: GameSquare? = null,
        var topDirection: GameSquare? = null,
        var rightDirection: GameSquare? = null,
        var bottomDirection: GameSquare? = null
    ) {
        fun clearSquare() {
            content = SquareContent.EmptySquare
        }

        fun setNewContent(squareContent: SquareContent) {
            content = squareContent
        }
    }

    sealed class SquareContent {
        object EmptySquare : SquareContent()
        object Food : SquareContent()
        object SnakeHead : SquareContent()
    }

    val gameBoard: Board = Board(0f, 0f, 0f, 0f)

    val listOfGameSquare = mutableListOf<GameSquare>()

    var zeroY = 0f
    var heightPadding = 0f

    var numberOfElements = 10
    var elementWidth = 0f

    enum class MoveDirection {
        TOP, BOTTOM, LEFT, RIGHT
    }

    val snakeMoveDirection = MoveDirection.RIGHT

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val max = max(width.toFloat(), height.toFloat())
        val min = min(width.toFloat(), height.toFloat())

        elementWidth = min / numberOfElements
        heightPadding = ((max - min) / 2)
        zeroY = (max - heightPadding)

        gameBoard.apply {
            this.left = 0f
            this.top = 0f + heightPadding
            this.right = min
            this.bottom = zeroY
        }

        listOfGameSquare.clear()
        for (elementY in 0 until numberOfElements) {
            for (elementX in 0 until numberOfElements) {
                val squareLeft = gameBoard.left + (elementX * elementWidth)
                val squareTop = gameBoard.top + (elementY * elementWidth)
                val squareRight = squareLeft + elementWidth
                val squareBottom = squareTop + elementWidth

                listOfGameSquare.add(
                    GameSquare(
                        elementX, elementY, squareLeft, squareTop, squareRight, squareBottom
                    )
                )
            }
        }

        for (elementY in 0 until numberOfElements) {
            for (elementX in 0 until numberOfElements) {
                val newLeftDirection =
                    if (elementX == 0) (numberOfElements - 1) else if (elementX == numberOfElements - 1) 0 else elementX - 1
                val newTopDirection =
                    if (elementY == 0) (numberOfElements - 1) else if (elementY == numberOfElements - 1) 0 else elementY - 1
                val newRightDirection = if (elementX == numberOfElements - 1) 0 else elementX + 1
                val newBottomDirection = if (elementY == numberOfElements - 1) 0 else elementY + 1

                val square = listOfGameSquare.find { it.x == elementX && it.y == elementY }
                square?.apply {
                    leftDirection =
                        listOfGameSquare.find { it.x == newLeftDirection && it.y == elementY }
                    topDirection =
                        listOfGameSquare.find { it.x == elementX && it.y == newTopDirection }
                    rightDirection =
                        listOfGameSquare.find { it.x == newRightDirection && it.y == elementY }
                    bottomDirection =
                        listOfGameSquare.find { it.x == elementX && it.y == newBottomDirection }
                }
            }
        }

        listOfGameSquare.find { it.x == numberOfElements / 2 && it.y == numberOfElements / 2 }
            ?.apply { this.setNewContent(SquareContent.SnakeHead) }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        with(gameBoard) {
            canvas?.drawRect(left, top, right, bottom, blackPaint)
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
            is SquareContent.EmptySquare -> {
                return@apply
            }
            is SquareContent.Food -> {
                Log.d("TEST", "draw food in ${gameSquare.x} ${gameSquare.y}}}")
                drawFood(gameSquare, canvas)
            }
            is SquareContent.Food -> {
                Log.d("TEST", "draw food in ${gameSquare.x} ${gameSquare.y}}}")
                drawFood(gameSquare, canvas)
            }
            is SquareContent.SnakeHead -> {
                Log.d("TEST", "draw snakeHead in ${gameSquare.x} ${gameSquare.y}}}")
                drawSnakeHEad(gameSquare, canvas)
            }
        }
    }

    private fun drawFood(gameSquare: GameSquare, canvas: Canvas?) = canvas?.apply {
        drawRect(
            gameSquare.left + 3f,
            gameSquare.top + 3f,
            gameSquare.right - 3f,
            gameSquare.bottom - 3f,
            redPaint
        )
    }

    private fun drawSnakeHEad(gameSquare: GameSquare, canvas: Canvas?) = canvas?.apply {
        drawCircle(
            gameSquare.left + (gameSquare.right - gameSquare.left) / 2,
            gameSquare.top + (gameSquare.top - gameSquare.bottom) / 2,
            ((gameSquare.right - gameSquare.left) / 2) - 10f,
            redPaint
        )
    }

    fun startGame() = scopeIO.launch {
        while (true) {
//            val square = listOfGameSquare.random()
//            if (square.content == SquareContent.EmptySquare) {
//                square.setNewContent(SquareContent.Food)
//            }
            val findSnakeHead = listOfGameSquare.find { it.content == SquareContent.SnakeHead }
            val nextSnakeHeadSquare = when (snakeMoveDirection) {
                MoveDirection.TOP -> findSnakeHead?.topDirection
                MoveDirection.BOTTOM -> findSnakeHead?.bottomDirection
                MoveDirection.LEFT -> findSnakeHead?.leftDirection
                MoveDirection.RIGHT -> findSnakeHead?.rightDirection
            }
            nextSnakeHeadSquare?.setNewContent(SquareContent.SnakeHead)
            findSnakeHead?.setNewContent(SquareContent.EmptySquare)

            Log.d("TEST", "snake head  ${findSnakeHead?.x} ${findSnakeHead?.y}}}")
            Log.d("TEST", "snake next square   ${nextSnakeHeadSquare?.x} ${nextSnakeHeadSquare?.y}}}")

            invalidate()
            delay(500)
        }
    }
//
//    fun startFood() = scopeIO.launch {
//        while (true) {
//            val square = listOfGameSquare.random()
//            if (square.content == SquareContent.EmptySquare) {
//                square.setNewContent(SquareContent.Food)
//                invalidate()
//            }
//            delay(50)
//        }
//    }

//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//    }
}