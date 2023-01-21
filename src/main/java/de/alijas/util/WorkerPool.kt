package de.alijas.dirproc.crypto


import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

/**
 * This class represents a pool of resources which can be borrowed
 * by parallel executed coroutine tasks
 */
class WorkerPool<T>(vararg initialResources: T) {
    private val channel = Channel<T>(Channel.UNLIMITED)

    init {
        for (res in initialResources) {
            channel.trySend(res)
        }
    }

    fun add(value: T) {
        channel.trySend(value)
    }

    suspend fun remove(): T = channel.receive()

    suspend inline fun borrow(handler: (T) -> Unit) {
        val borrowed = remove()
        try {
            handler(borrowed)
        } finally {
            add(borrowed)
        }
    }
}

class Worker(val name: String) {
    suspend fun work(itemToBeWorked: Int) {
        val waittime = Random.nextLong(10000)
        println("$itemToBeWorked. Item: Start working hard with worker $name for $waittime ms")
        delay(waittime)
        println("$itemToBeWorked. Item: Finished working with worker $name.")
    }


}

fun main() {
    val pool = WorkerPool(Worker("First"), Worker("Second"), Worker("Third"), Worker("Fourth"))

    runBlocking {
        repeat(10) { itemToBeWorked ->
            launch {
                pool.borrow {
                    it.work(itemToBeWorked)
                }
            }
        }
    }
}