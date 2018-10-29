package com.succcubbus.haliterelay

import com.succcubbus.haliterelay.OS.LINUX
import com.succcubbus.haliterelay.OS.MAC
import com.succcubbus.haliterelay.OS.WINDOWS
import java.io.OutputStream
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

enum class OS {
    LINUX,
    WINDOWS,
    MAC
}

private fun handleConnection(socket: Socket, startCmd: String) {
    println()
    println("accepted connection from ${socket.remoteSocketAddress} at ${now().format(ISO_DATE_TIME)}")

    val process = Runtime.getRuntime().exec(startCmd)

    var remoteOs = LINUX

    val localOsName = System.getProperty("os.name")
    val localOs = when {
        localOsName.contains("nux") -> LINUX
        localOsName.contains("win") -> WINDOWS
        localOsName.contains("mac") || localOsName.contains("darwin") -> MAC
        else -> LINUX
    }

    thread {
        val oStream = socket.getOutputStream()
        generateSequence { process.inputStream.read().takeIf { it != -1 } }
            .forEach { translateEndings(localOs, remoteOs, it, oStream) }

        val exitStatus = process.waitFor()
        println("bot stopped with status $exitStatus")

        socket.close()
    }
    thread {
        try {
            generateSequence { socket.getInputStream().read().takeIf { it != -1 } }
                .forEach {
                    remoteOs = detectOs(it, remoteOs)

                    translateEndings(remoteOs, localOs, it, process.outputStream)
                }
        } catch (ignored: SocketException) {
        } catch (ignored: SocketTimeoutException) {
            println("connection timed out")
        }
        println("closed connection from ${socket.remoteSocketAddress} at ${now().format(ISO_DATE_TIME)}")
        process.destroy()
    }
}

private fun detectOs(it: Int, remoteOs: OS): OS {
    return when {
        it == '\r'.toInt() && remoteOs == LINUX -> MAC
        it == '\n'.toInt() && remoteOs == MAC -> WINDOWS
        else -> remoteOs
    }
}

private fun translateEndings(from: OS, to: OS, msg: Int, output: OutputStream) {
    when {
        from == WINDOWS && msg == '\r'.toInt() -> {}

        from == WINDOWS && msg == '\n'.toInt() ||
        from == LINUX && msg == '\n'.toInt() ||
        from == MAC && msg == '\r'.toInt() -> {
            when (to) {
                WINDOWS -> {
                    output.write('\r'.toInt())
                    output.write('\n'.toInt())
                }
                LINUX -> output.write('\n'.toInt())
                MAC -> output.write('\r'.toInt())
            }
            output.flush()
        }

        else -> output.write(msg)
    }
}