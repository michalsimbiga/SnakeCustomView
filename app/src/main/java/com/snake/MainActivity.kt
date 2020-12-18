package com.snake

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resetButton.setOnClickListener { snake.restart() }

        directionTop.setOnClickListener { snake.changeDirection(SnakeView.MoveDirection.TOP) }
        directionLeft.setOnClickListener { snake.changeDirection(SnakeView.MoveDirection.LEFT) }
        directionRight.setOnClickListener { snake.changeDirection(SnakeView.MoveDirection.RIGHT) }
        directionBottom.setOnClickListener { snake.changeDirection(SnakeView.MoveDirection.BOTTOM) }
    }
}