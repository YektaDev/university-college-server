import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket

internal class Server(clientCount: Int) {
    private companion object {
        const val DEFAULT_PORT = 8080
    }

    private val port = System.getenv("PORT")?.toInt() ?: DEFAULT_PORT
    private val server = ServerSocket(port)

    @Suppress("BlockingMethodInNonBlockingContext")
    val sockets = Array(clientCount) { i ->
        val num = i + 1
        println("Server waiting for client $num...")
        server
            .accept()
            .also { println("Client $num connected.") }
    }
    private val readers = Array(clientCount) { BufferedReader(InputStreamReader(sockets[it].inputStream)) }
    private val writers = Array(clientCount) { PrintWriter(sockets[it].outputStream, true) }

    fun receive(client: Int): String {
        val reader = readers[client]
        val lines = reader.readLine()!!.toInt()
        return buildString {
            repeat(lines) { i ->
                append(reader.readLine())
                if (i < lines - 1) append('\n')
            }
        }
    }

    fun send(client: Int, text: String) {
        writers[client].println(text.lines().size)
        writers[client].println(text)
    }
}
