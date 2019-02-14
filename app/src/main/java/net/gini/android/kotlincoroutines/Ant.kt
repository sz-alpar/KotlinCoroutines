package net.gini.android.kotlincoroutines

import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.util.Size
import kotlinx.coroutines.delay
import java.util.*

/**
 * Created by Alpar Szotyori on 13.02.2019.
 *
 * Copyright (c) 2019 Gini GmbH.
 */

class Ant(
    var position: Point = Point(0, 0),
    private val fieldSize: Size,
    private val movementCallback: MovementCallback
) : Runnable {

    companion object {
        val size = Size(10, 10)
        private const val movementStep: Int = 2

        fun makeBoundsRect(position: Point): Rect {
            return Rect(
                position.x - (Ant.size.width / 2F).toInt(),
                position.y - (Ant.size.height / 2F).toInt(),
                position.x + (Ant.size.width / 2F).toInt(),
                position.y + (Ant.size.height / 2F).toInt()
            )
        }
    }

    enum class Direction {
        UP {
            override fun move(from: Point): Point {
                return Point(from.x, from.y + movementStep)
            }
        },

        DOWN {
            override fun move(from: Point): Point {
                return Point(from.x, from.y - movementStep)
            }
        },

        LEFT {
            override fun move(from: Point): Point {
                return Point(from.x - movementStep, from.y)
            }
        },

        RIGHT {
            override fun move(from: Point): Point {
                return Point(from.x + movementStep, from.y)
            }
        };

        abstract fun move(from: Point): Point
    }

    val boundsRect: Rect
        get() {
            return makeBoundsRect(position)
        }

    private val delayMillis = (1000F / 120F).toLong()

override fun run() {
    try {
        while (true) {
            move(randomDirection())
            Thread.sleep(delayMillis)
        }
    } catch (e: InterruptedException) {
        Log.d("ant", "interrupted")
    }
}

    private fun move(inDirection: Direction) {
        val nextPosition = inDirection.move(position)
        if (canMoveTo(nextPosition)) {
            position = nextPosition
        }
    }

    private fun randomDirection(): Direction = Direction.values()[Random().nextInt(4)]

    private fun canMoveTo(position: Point): Boolean =
        isOnTheField(position) && movementCallback.isPositionFree(position, this)

    suspend fun runAsSuspend() {
        try {
            while (true) {
                moveAsSuspend(randomDirection())
                delay(delayMillis)
                //yield()
            }
        } finally {
        }
    }

    private suspend fun moveAsSuspend(inDirection: Direction) {
        val nextPosition = inDirection.move(position)
        if (canMoveToAsSuspend(nextPosition)) {
            position = nextPosition
        }
    }

    private suspend fun canMoveToAsSuspend(position: Point): Boolean =
        isOnTheField(position) && movementCallback.isPositionFreeAsSuspend(position, this)

    private fun isOnTheField(position: Point): Boolean {
        val newBoundsRect = Ant.makeBoundsRect(position)
        return newBoundsRect.left >= 0
                && newBoundsRect.top >= 0
                && newBoundsRect.right <= fieldSize.width
                && newBoundsRect.bottom <= fieldSize.height
    }

}

interface MovementCallback {
    fun isPositionFree(position: Point, ant: Ant): Boolean

    suspend fun isPositionFreeAsSuspend(position: Point, ant: Ant): Boolean
}