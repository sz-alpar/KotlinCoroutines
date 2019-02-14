package net.gini.android.kotlincoroutines


import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Size
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.Executors
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class MainActivity : AppCompatActivity(), MovementCallback {

    private val fieldSize = Size(1000, 1300)
    private val ants: List<Ant> = List(3000) {
        val columns = 48
        val xOffset = it % columns
        val yOffset = it / columns
        Ant(
            Point(
                32 + xOffset * (Ant.size.width + 10),
                32 + yOffset * (Ant.size.height + 10)
            ),
            fieldSize, this
        )
    }

    private val lock: Lock = ReentrantLock()

    private val useCoroutines = true

    override fun isPositionFree(position: Point, ant: Ant): Boolean {
        synchronized(lock) {
            return canAntMoveTo(ant, position)
        }
    }

    private val mutex: Mutex = Mutex()

    override suspend fun isPositionFreeAsSuspend(position: Point, ant: Ant): Boolean {
//        mutex.withLock {
        return true //canAntMoveTo(ant, position)
//        }
    }

    private fun canAntMoveTo(ant: Ant, position: Point): Boolean {
        for (anotherAnt in ants) {
            if (anotherAnt !== ant) {
                if (Rect.intersects(anotherAnt.boundsRect, Ant.makeBoundsRect(position))) {
                    return false
                }
            }
        }
        return true
    }

    private val millisPerFrame = fpsToMillis(30)
    private fun fpsToMillis(fps: Int): Long = (1000F / fps.toFloat()).toLong()

    private var threads: Array<Thread> = arrayOf()

    private val uiScope = CoroutineScope(Dispatchers.Main)
//    private var job: Job? = null

//    private val context = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    //    private val context = Executors.newFixedThreadPool(152).asCoroutineDispatcher()
//        private val context = Dispatchers.IO
        private val context = Dispatchers.Default
    private val backgroundScope = CoroutineScope(context)

    //    private val context = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val context2 = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
    //    private val context2 = Dispatchers.IO
//    private val context2 = Dispatchers.Default
    private val nestedScope = CoroutineScope(context2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fieldView.ants = ants
        fieldView.fieldSize = fieldSize

        drawLoop()

        fieldView.setOnClickListener {
            uiScope.launch {
                Toast.makeText(this@MainActivity, "I've been tapped!", Toast.LENGTH_SHORT).show()
                delay(2000L)
                Toast.makeText(this@MainActivity, "2 seconds passed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        @Suppress("ConstantConditionIf")
        if (useCoroutines) {

//            for (ant in ants) {
//                uiScope.launch {
//                    withContext(context) {
//                        ant.runAsSuspend()
//                    }
//                }
//            }

            for (ant in ants) {
                backgroundScope.launch {
                    ant.runAsSuspend()
                }
            }

//            val batchSize = 100
//            for (i in 0..(ants.size - batchSize) step batchSize) {
//                backgroundScope.launch {
//                    ants.subList(i, i + batchSize).forEach {
//                        nestedScope.launch {
//                            it.runAsSuspend()
//                        }
//                    }
//                }
//            }

        } else {
            threads = Array(ants.size) {
                Thread(ants[it])
            }
            for (thread in threads) {
                thread.start()
            }
        }
    }

    private fun drawLoop() {
//        fieldView.postDelayed({
//            fieldView.invalidate()
//            drawLoop()
//        }, millisPerFrame)
        uiScope.launch {
            while (isActive) {
                fieldView.invalidate()
                delay(millisPerFrame)
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onStop() {
        super.onStop()
        for (thread in threads) {
            thread.interrupt()
        }

//        try {
//            job?.cancel()
//        } catch (e: Exception) {
//            Log.d("uiScope", "Exception: $e")
//        }

    }
}
