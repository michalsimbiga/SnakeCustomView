package com.snake

import android.graphics.Color
import android.graphics.Paint

object PaintBucket {
    val yellowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
    }

    val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
    }

    val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
    }

    val blackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 5f
        textSize = 22f
    }

    val foodPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9F454B")
    }

    val snakePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5C1424")
    }

    val boardBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DDB38C")
    }
}