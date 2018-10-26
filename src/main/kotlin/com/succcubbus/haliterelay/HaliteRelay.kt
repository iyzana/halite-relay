package com.succcubbus.haliterelay

import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    val startCmd = args.getOrNull(0) ?: throw IllegalArgumentException("expected bot command as argument")
    val port = args.getOrNull(1)?.toInt() ?: 7777
    val server = ServerSocket(port)

    println("listening for connections on port $port")

    while (true) {
        val socket = server.accept()
        socket.soTimeout = 70000

        handleConnection(socket, startCmd)
    }
}

private fun handleConnection(socket: Socket, startCmd: String) {
    println()
    println("accepted connection from ${socket.remoteSocketAddress} at ${now().format(ISO_DATE_TIME)}")

    val process = Runtime.getRuntime().exec(startCmd)

    thread {
        val oStream = socket.getOutputStream()
        generateSequence { process.inputStream.read().takeIf { it != -1 } }
            .forEach { oStream.write(it) }

        val exitStatus = process.waitFor()
        println("bot stopped with status $exitStatus")

        socket.close()
    }
    thread {
        try {
            generateSequence { socket.getInputStream().read().takeIf { it != -1 } }
                .forEach {
                    process.outputStream.write(it)

                    if (it == '\n'.toInt()) {
                        process.outputStream.flush()
                    }
                }
        } catch (ignored: SocketException) {
        } catch (ignored: SocketTimeoutException) {
            println("connection timed out")
        }
        println("closed connection from ${socket.remoteSocketAddress} at ${now().format(ISO_DATE_TIME)}")
        process.destroy()
    }
}
