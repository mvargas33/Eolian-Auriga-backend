import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.System.currentTimeMillis
import java.util.*
import kotlin.system.measureTimeMillis

class MMain(p: Array<Producer>, c: Array<Consumer>) {
    var producers = p
    var consumers = c

    suspend fun main() {
        while(true) {
            for(p in producers) {
                p.produce()
            }

            for(c in consumers) {
                c.consume()
            }
        }
    }
}

class Producer(s: Array<String>, i: Int) {
    var msgIndex = 0
    var state = s
    var index = i

    suspend fun produce() {
        var data : String? = ""
        val timeInMillis = measureTimeMillis {
            try {
                val result = withTimeoutOrNull(1300L) {
                    repeat(1000) { i ->
                        println("I'm sleeping $i ...")
                        delay(500L)
                    }
                    "Done" // will get cancelled before it produces this result
                }
                println("Result is $result")
            } catch (e: TimeoutCancellationException) {
                println("No inputs received")
            }
            state[index] = "{${index.toString()}: $data}"
            print("Wrote $data to $index")
        }
        println(" -> ${timeInMillis}ms")
    }
}

class Consumer(s: Array<String>, i: String) {
    var state = s
    val id = i

    fun consume() {
        print("Consumer $id used US: [")
        val timeInMillis = measureTimeMillis {
            for (i in 0..state.lastIndex) {
                if (i == state.lastIndex) print("${state[i]}]")
                else print("${state[i]},")
            }
        }
        println(" -> ${timeInMillis}ms")
    }
}

class UniversalState() {
    var values: Array<String> = Array(20) { "N" }
}

fun main() = runBlocking {
    val startTime = currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        println("started proess")
        val p = ProcessBuilder("/home/jayki/Desktop/eolian/ea-back-end/cansim.sh").start()
        println("ended process")
        val br = BufferedReader(
            InputStreamReader(
                p.inputStream
            )
        )
        while(isActive) {

            var tempLine: String

            while (br.readLine().also { tempLine = it } != null) {
                println(tempLine)
            }
        }
    }
    delay(1300L) // delay a bit
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // cancels the job and waits for its completion
    println("main: Now I can quit.")
}

fun log(message: Any?) {
    println("[${Thread.currentThread().name}] $message")
}