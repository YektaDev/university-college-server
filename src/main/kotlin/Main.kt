import java.io.File
import kotlin.concurrent.thread

private const val RECORD_COUNT = 9

fun main() {
    print("Enter the number of colleges: ")
    val count = readlnOrNull()?.toIntOrNull() ?: println("No valid count is received.").let { return }
    repeat(count) { openClientInstance() }
    val server = Server(count)
    repeat(count) { i ->
        thread {
            server.send(i, read(collegeIndex = i))
            while (!server.sockets[i].isClosed) {
                println("Listening to client of college ${i + 1}...")
                val request = server.receive(i)
                println("Client of college ${i + 1} sent '$request'")
                val response = processData(i, request)
                val responseLog = response.takeIf { it.lines().size == 1 }?.let { "'$it'" } ?: "a long response"
                println("Responding $responseLog")
                server.send(i, response)
            }
            println("Client of college ${i + 1} is closed.")
        }
    }
}

private fun read(collegeIndex: Int): String = File("college_data/college_$collegeIndex.csv").readText()
private fun write(collegeIndex: Int, data: String) = File("college_data/college_$collegeIndex.csv").writeText(data)
private fun readData(collegeIndex: Int): List<String> = read(collegeIndex)
    .lines()
    .drop(1)
    .filter { it.split(",").size == RECORD_COUNT }

private fun processData(clientIndex: Int, data: String): String = when (data.lowercase()) {
    "average" -> computeAverage(clientIndex)
    "sort" -> computeSort(clientIndex)
    "max" -> computeMax(clientIndex)
    "min" -> computeMin(clientIndex)
    else -> write(clientIndex, data).let { "" } // Raw data came to be stored
}

private fun openClientInstance() = Runtime.getRuntime().exec("java -jar ./college-client.jar")

private fun computeAverage(clientIndex: Int): String = readData(clientIndex).joinToString(separator = "\n") { line ->
    val tokens = line.split(",")
    val ssn = tokens[2]
    val avg = tokens.subList(4, 9).map { it.toInt() }.average()
    format(ssn = ssn, average = avg)
}

private fun computeSort(clientIndex: Int): String = readData(clientIndex)
    .asSequence()
    .map { line ->
        val tokens = line.split(",")
        val ssn = tokens[2]
        val avg = tokens.subList(4, 9).map { it.toInt() }.average()
        ssn to avg
    }
    .sortedBy { it.second }
    .joinToString(separator = "\n") { (ssn, avg) -> format(ssn = ssn, average = avg) }

private fun computeMax(clientIndex: Int): String = computeOnePersonWithAvgBy(clientIndex) { maxBy { it.third } }
private fun computeMin(clientIndex: Int): String = computeOnePersonWithAvgBy(clientIndex) { minBy { it.third } }
private inline fun computeOnePersonWithAvgBy(
    clientIndex: Int,
    getPredicate: Sequence<Triple<String, String, Double>>.() -> Triple<String, String, Double>
): String = readData(clientIndex)
    .asSequence()
    .map { line ->
        val tokens = line.split(",")
        val firstName = tokens[0]
        val lastName = tokens[1]
        val avg = tokens.subList(4, 9).map { it.toInt() }.average()
        Triple(firstName, lastName, avg)
    }
    .getPredicate()
    .let { (firstName, lastName, avg) -> format(firstName = firstName, lastName = lastName, average = avg) }

private fun format(ssn: String, average: Double) = "SSN: $ssn\nAverage: $average\n"
private fun format(firstName: String, lastName: String, average: Double) =
    "First name: $firstName\nLast name: $lastName\nAverage: $average\n"
