package com.snake

import android.content.Context
import android.graphics.Canvas
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

    private var job = Job()
    private var scopeMain = CoroutineScope(job + Dispatchers.Main)
    private var scopeIO = CoroutineScope(job + Dispatchers.IO)

    fun recreateJobs() {
        scopeMain.cancel()
        scopeIO.cancel()
        job = Job()
        scopeMain = CoroutineScope(job + Dispatchers.Main)
        scopeIO = CoroutineScope(job + Dispatchers.IO)
    }

    val paintBucket = PaintBucket

    enum class GameState {
        GAME_OVER, INIT, PLAYING
    }

    private var currentGameState: GameState = GameState.INIT
        set(value) {
            field = value

            recreateJobs()

            when (value) {
                GameState.GAME_OVER -> {
                    drawSnake = false
                    gameOverState()
                }
                GameState.INIT -> {
                    initGame()
                    snakeMoveDirection = MoveDirection.RIGHT
                }
                GameState.PLAYING -> {
                    startGame()
                }

            }
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

        fun findBody() =
            listOf(
                leftDirection,
                rightDirection,
                topDirection,
                bottomDirection
            ).find { it?.content == SquareContent.SnakeBody }

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
        object SnakeEnd : SquareContent()
    }

    val gameBoard: Board = Board(0f, 0f, 0f, 0f)

    val listOfGameSquare = mutableListOf<GameSquare>()

    var zeroY = 0f
    var heightPadding = 0f

    var numberOfElements = 10
    var elementWidth = 0f
    var gameActive = false
    var foodOnTable = false
    var drawSnake = false

    enum class MoveDirection {
        TOP, BOTTOM, LEFT, RIGHT;

        companion object {
            fun getOpposite(direction: MoveDirection) = when (direction) {
                TOP -> BOTTOM
                BOTTOM -> TOP
                LEFT -> RIGHT
                RIGHT -> LEFT
            }
        }
    }

    private var snakeMoveDirection = MoveDirection.RIGHT
    private var snakeSquares = mutableListOf<GameSquare>()

    private var snakeLength = 3

    fun changeDirection(newDirection: MoveDirection) {
        val sanitizeDirection = when (newDirection) {
            MoveDirection.TOP -> if (snakeMoveDirection == MoveDirection.getOpposite(newDirection)) MoveDirection.BOTTOM else MoveDirection.TOP
            MoveDirection.LEFT -> if (snakeMoveDirection == MoveDirection.getOpposite(newDirection)) MoveDirection.RIGHT else MoveDirection.LEFT
            MoveDirection.BOTTOM -> if (snakeMoveDirection == MoveDirection.getOpposite(newDirection)) MoveDirection.TOP else MoveDirection.BOTTOM
            MoveDirection.RIGHT -> if (snakeMoveDirection == MoveDirection.getOpposite(newDirection)) MoveDirection.LEFT else MoveDirection.RIGHT
        }

        snakeMoveDirection = sanitizeDirection
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
                val newRightDirection =
                    if (elementX == numberOfElements - 1) 0 else elementX + 1
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

        currentGameState = GameState.INIT
    }

    fun start() {
        currentGameState = GameState.PLAYING
    }

    fun restart() {
        currentGameState = GameState.INIT
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        with(gameBoard) {
            canvas?.drawRect(left, top, right, bottom, paintBucket.blackPaint)
        }

        drawBoardSquares(canvas)

    }

    private fun drawBoardSquares(canvas: Canvas?) = canvas?.apply {
        listOfGameSquare.forEach { square ->
            drawRect(
                square.left,
                square.top,
                square.right,
                square.bottom,
                paintBucket.boardBackgroundPaint
            )

            if (drawSnake) drawSquareContent(square, canvas)
        }
    }

    private fun drawSquareContent(gameSquare: GameSquare, canvas: Canvas?) = canvas?.apply {
        when (gameSquare.content) {
            is SquareContent.Food -> {
                drawFood(gameSquare, canvas)
            }
            is SquareContent.SnakeHead -> {
                drawSnakeHead(gameSquare, canvas)
            }
            is SquareContent.SnakeBody -> {
                drawSnakeBody(gameSquare, canvas)
            }
        }
    }

    private fun drawFood(gameSquare: GameSquare, canvas: Canvas?) = canvas?.apply {
        drawRect(
            gameSquare.left + 20f,
            gameSquare.top + 20f,
            gameSquare.right - 20f,
            gameSquare.bottom - 20f,
            paintBucket.foodPaint
        )
    }

    private fun drawSnakeHead(gameSquare: GameSquare, canvas: Canvas?) = canvas?.apply {
        var headTopX = 0f
        var headTopY = 0f
        val rotation = when (snakeMoveDirection) {
            MoveDirection.TOP -> 90f
            MoveDirection.BOTTOM -> -90f
            MoveDirection.LEFT -> 180f
            MoveDirection.RIGHT -> 180f
        }

        drawCircle(
            gameSquare.left + (gameSquare.right - gameSquare.left) / 2,
            gameSquare.bottom + (gameSquare.top - gameSquare.bottom) / 2,
            ((gameSquare.right - gameSquare.left) / 2) - 10f,
            paintBucket.snakePaint
        )
    }

    private fun drawSnakeBody(gameSquare: GameSquare, canvas: Canvas?) = canvas?.apply {
        drawRect(
            gameSquare.left + 20f,
            gameSquare.top + 20f,
            gameSquare.right - 20f,
            gameSquare.bottom - 20f,
            paintBucket.snakePaint
        )
    }

    private fun createNewSnake() {
        snakeSquares.clear()
        val center = numberOfElements / 2

        Log.d("TEST", "snake center $center")
        listOfGameSquare.find { it.x == center && it.y == center }
            ?.apply {
                setNewContent(SquareContent.SnakeHead)
                snakeSquares.add(this)
                Log.d("TEST", "snake head ${this.x} ${this.y}")

                var currentGameSquare = this
                for (bodyCount in 0 until snakeLength) {
                    val nextSquare = currentGameSquare.leftDirection
                    snakeSquares.add(nextSquare ?: return@apply)

                    Log.d("TEST", "snake body ${nextSquare.x} ${nextSquare.y}")

                    nextSquare.apply {
                        content = SquareContent.SnakeBody
                        currentGameSquare = this
                    }
                }

                val endSquare = currentGameSquare.leftDirection
                endSquare?.apply {
                    content = SquareContent.SnakeEnd
                    snakeSquares.add(this)
                }

                Log.d("TEST", "snake end ${endSquare?.x} ${endSquare?.y}")
            }
    }

    private fun moveSnake() {

        Log.d("TEST", "Snake Squares ${snakeSquares.map { "${it.x} ${it.y}" }}")
        var currentHead: GameSquare = snakeSquares.first()
        var currentEnd: GameSquare = snakeSquares.last()

        val peekSquare = when (snakeMoveDirection) {
            MoveDirection.TOP -> {
                currentHead.topDirection
            }
            MoveDirection.BOTTOM -> {
                currentHead.bottomDirection
            }
            MoveDirection.LEFT -> {
                currentHead.leftDirection
            }
            MoveDirection.RIGHT -> {
                currentHead.rightDirection
            }
        }

        peekSquare?.apply {
            snakeSquares.add(0, peekSquare)
            currentHead.setNewContent(SquareContent.SnakeBody)
        }

        when (peekSquare?.content) {
            is SquareContent.EmptySquare -> {
                currentEnd.clearSquare()
                snakeSquares.removeAt(snakeSquares.size - 1)
            }
            is SquareContent.Food -> {
                foodOnTable = false
            }
            else -> {
                gameActive = false
                currentGameState = GameState.GAME_OVER
            }
        }

        peekSquare?.setNewContent(SquareContent.SnakeHead)
    }

    private fun initGame() = scopeIO.launch {
        drawSnake = true
        gameActive = false
        foodOnTable = false
        snakeMoveDirection = MoveDirection.RIGHT
        listOfGameSquare.forEach { it.clearSquare() }
        createNewSnake()
        invalidate()
    }

    private fun startGame() = scopeIO.launch {
        while (currentGameState == GameState.PLAYING) {
            moveSnake()
            startFood()
            invalidate()
            delay(400)
        }
    }

    private fun startFood() = scopeIO.launch {
        if (foodOnTable.not()) {
            val square = listOfGameSquare.random()
            if (square.content == SquareContent.EmptySquare
                && snakeSquares.contains(square).not()
            ) {
                square.setNewContent(SquareContent.Food)
                foodOnTable = true
                invalidate()
            }
        }
    }

    private fun gameOverState() = scopeIO.launch {
        while (currentGameState == GameState.GAME_OVER) {
            drawSnake = drawSnake.not()
            invalidate()
            delay(200)
        }
    }
}


//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//    }
