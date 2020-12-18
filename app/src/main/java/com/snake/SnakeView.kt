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
        textSize = 22f
    }

    data class Board(
        var left: Float = 0f,
        var top: Float = 0f,
        var right: Float = 0f,
        var bottom: Float = 0f
    )

    data class SnakeSquare(
        var x: Int = 0,
        var y: Int = 0
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

        fun findBody() =
            listOf(leftDirection, rightDirection, topDirection, bottomDirection).find { it?.content == SquareContent.SnakeBody }

        fun setValues(square: GameSquare) = this.apply {
            x = square.x
            y = square.y
            left = square.left
            top = square.top
            right = square.right
            bottom = square.bottom
            content = square.content
            leftDirection = square.leftDirection
            topDirection = square.topDirection
            rightDirection = square.rightDirection
            bottomDirection = square.bottomDirection
        }

        fun setNewContent(squareContent: SquareContent) {
            content = squareContent
        }
    }

    sealed class SquareContent {
        object EmptySquare : SquareContent()
        object Food : SquareContent()
        object SnakeHead : SquareContent()
        object SnakeBody : SquareContent()
    }

    val gameBoard: Board = Board(0f, 0f, 0f, 0f)

    val listOfGameSquare = mutableListOf<GameSquare>()

    var zeroY = 0f
    var heightPadding = 0f

    var numberOfElements = 10
    var elementWidth = 0f
    var gameActive = false

    enum class MoveDirection {
        TOP, BOTTOM, LEFT, RIGHT
    }

    private var snakeMoveDirection = MoveDirection.RIGHT
    private var snakeSquares = mutableListOf<SnakeSquare>()

    private var snakeLength = 3

    fun changeDirection(newDirection: MoveDirection) {
        snakeMoveDirection = newDirection
    }

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

        for (elementY in 0..numberOfElements) {
            for (elementX in 0..numberOfElements) {
                val newLeftDirection =
                    if (elementX == 0) numberOfElements - 1 else elementX - 1
                val newTopDirection =
                    if (elementY == 0) numberOfElements - 1 else elementY - 1
                val newRightDirection = if (elementX == numberOfElements - 1) 0 else elementX + 1
                val newBottomDirection = (elementY + 1) % (numberOfElements)

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

        calculateNewSnakeCoords()
        startGame()
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
//                Log.d("TEST", "draw EmptySquare in ${gameSquare.x} ${gameSquare.y}}}")
                drawText(
                    "${gameSquare.x} ${gameSquare.y}",
                    gameSquare.left + 4f,
                    gameSquare.bottom - 4f,
                    blackPaint
                )
            }
            is SquareContent.Food -> {
//                Log.d("TEST", "draw Food in ${gameSquare.x} ${gameSquare.y}}}")
                drawFood(gameSquare, canvas)
            }
            is SquareContent.SnakeHead -> {
//                Log.d("TEST", "draw SnakeHead in ${gameSquare.x} ${gameSquare.y}}}")
                drawSnakeHead(gameSquare, canvas)
            }
            is SquareContent.SnakeBody -> {
//                Log.d("TEST", "draw SnakeBody in ${gameSquare.x} ${gameSquare.y}}}")
                drawSnakeBody(gameSquare, canvas)
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

    private fun drawSnakeHead(gameSquare: GameSquare, canvas: Canvas?) = canvas?.apply {
        drawCircle(
            gameSquare.left + (gameSquare.right - gameSquare.left) / 2,
            gameSquare.bottom + (gameSquare.top - gameSquare.bottom) / 2,
            ((gameSquare.right - gameSquare.left) / 2) - 10f,
            redPaint
        )
    }

    private fun drawSnakeBody(gameSquare: GameSquare, canvas: Canvas?) = canvas?.apply {
        drawRect(
            gameSquare.left + 20f,
            gameSquare.top + 20f,
            gameSquare.right - 20f,
            gameSquare.bottom - 20f,
            redPaint
        )
    }

    private fun calculateNewSnakeCoords() {
        val center = numberOfElements / 2

        Log.d("TEST", "snake center $center")
        listOfGameSquare.find { it.x == center && it.y == center }
            ?.apply {
                setNewContent(SquareContent.SnakeHead)

                Log.d("TEST", "snake head ${this.x} ${this.y}")

                var currentGameSquare = this
                for (bodyCount in 0..snakeLength) {
                    val nextSquare = currentGameSquare.leftDirection

                    Log.d("TEST", "snake body ${nextSquare?.x} ${nextSquare?.y}")

                    nextSquare?.apply {
                        content = SquareContent.SnakeBody
                        currentGameSquare = this
                    }
                }

//                var direction = snakeMoveDirection
//
//                for (count in 0..snakeLength) {
//                    val nextSquare = when (direction) {
//                        MoveDirection.TOP -> currentSquare.topDirection
//                        MoveDirection.BOTTOM -> currentSquare.bottomDirection
//                        MoveDirection.LEFT -> currentSquare.leftDirection
//                        MoveDirection.RIGHT -> currentSquare.rightDirection
//                    }
//                    nextSquare?.setNewContent(currentSquare.content)
//                }
            }
    }
//
//        listOfGameSquare.find { it.x == snakeHeadX - 1 && it.y == snakeHeadY }
//            ?.apply {
//                this.setNewContent(SquareContent.SnakeBody)
//            }
//
//        listOfGameSquare.find { it.x == snakeHeadX - 2 && it.y == snakeHeadY }
//            ?.apply {
//                this.setNewContent(SquareContent.SnakeBody)
//            }
//
//        listOfGameSquare.find { it.x == snakeHeadX - 3 && it.y == snakeHeadY }
//            ?.apply {
//                this.setNewContent(SquareContent.SnakeBody)
//            }


    private fun moveSnake() {
        var currentHead: GameSquare =
            listOfGameSquare.find { it.content == SquareContent.SnakeHead } ?: return
        var direction = snakeMoveDirection

        val peekSquare = when (direction) {
            MoveDirection.TOP -> {
                direction = MoveDirection.TOP
                currentHead.topDirection
            }
            MoveDirection.BOTTOM -> {
                direction = MoveDirection.BOTTOM
                currentHead.bottomDirection
            }
            MoveDirection.LEFT -> {
                direction = MoveDirection.LEFT
                currentHead.leftDirection
            }
            MoveDirection.RIGHT -> {
                direction = MoveDirection.RIGHT
                currentHead.rightDirection
            }
        }
        peekSquare?.content = SquareContent.SnakeHead
        currentHead.content = SquareContent.SnakeBody

        for (count in 0 until snakeLength) {
//            val nextSquare = when (direction) {
//                MoveDirection.TOP -> {
//                    direction = MoveDirection.TOP
//                    currentSquare.topDirection
//                }
//                MoveDirection.BOTTOM -> {
//                    direction = MoveDirection.BOTTOM
//                    currentSquare.bottomDirection
//                }
//                MoveDirection.LEFT -> {
//                    direction = MoveDirection.LEFT
//                    currentSquare.leftDirection
//                }
//                MoveDirection.RIGHT -> {
//                    direction = MoveDirection.RIGHT
//                    currentSquare.rightDirection
//                }
//            }
//            }


//        for (count in 0 until snakeLength) {
//            val nextSquare = when (direction) {
//                MoveDirection.TOP -> {
//                    direction = MoveDirection.TOP
//                    currentSquare.topDirection
//                }
//                MoveDirection.BOTTOM -> {
//                    direction = MoveDirection.BOTTOM
//                    currentSquare.bottomDirection
//                }
//                MoveDirection.LEFT -> {
//                    direction = MoveDirection.LEFT
//                    currentSquare.leftDirection
//                }
//                MoveDirection.RIGHT -> {
//                    direction = MoveDirection.RIGHT
//                    currentSquare.rightDirection
//                }
//            }
//            nextSquare?.setNewContent(currentHead.content)
        }
    }

    fun startGame() = scopeIO.launch {
        gameActive = true
        while (gameActive) {
            listOfGameSquare.find { it.content == SquareContent.SnakeHead }?.let {
                moveSnake()
                invalidate()
            }
            delay(400)
        }
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
