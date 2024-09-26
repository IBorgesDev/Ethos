import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Tray
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*


import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Duration


val hide = mutableStateOf(false)
val isOpen = mutableStateOf(false)
var isVisible = mutableStateOf(true)

fun runSubprocess(command: String, args: List<String>) {
    try {
        val processBuilder = ProcessBuilder(listOf(command) + args)
        val env = processBuilder.environment()
        val currentPath = env["PATH"]
        env["PATH"] = if (currentPath != null) {
            "$currentPath;C:\\Users\\icaro\\AppData\\Local\\Packages\\PythonSoftwareFoundation.Python.3.12_qbz5n2kfra8p0\\LocalCache\\local-packages\\Python312\\Scripts"
        } else {
            "C:\\Users\\icaro\\AppData\\Local\\Packages\\PythonSoftwareFoundation.Python.3.12_qbz5n2kfra8p0\\LocalCache\\local-packages\\Python312\\Scripts"
        }
        println("Updated PATH: ${env["PATH"]}") // Debug output to verify PATH

        // Hide the window
         isVisible.value = false
         hide.value = true

        val process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            println(line)
            if (line!!.contains("[Render thread] [INFO] Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]")) {
                // Kill the Compose Desktop application
            }
        }
        process.waitFor()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun exitApplication() {
    System.exit(0)
}

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("")}

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            runSubprocess("pip", listOf("install", "portablemc"))
        }) {
            Text("Install portablemc")
        }
        Button(onClick = {
            runSubprocess("C:\\Users\\icaro\\AppData\\Local\\Packages\\PythonSoftwareFoundation.Python.3.12_qbz5n2kfra8p0\\LocalCache\\local-packages\\Python312\\Scripts\\portablemc.exe", listOf("start", "--login", "icarodb@outlook.com"))
        }) {
            Text("Run Minecraft Authed")
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter your username") }
        )

        Button(onClick = {
            runSubprocess("C:\\Users\\icaro\\AppData\\Local\\Packages\\PythonSoftwareFoundation.Python.3.12_qbz5n2kfra8p0\\LocalCache\\local-packages\\Python312\\Scripts\\portablemc.exe", listOf("start", "-u", text))
        }) {
            Text("Run Minecraft")
        }
    }
}



fun main() = application {
    embeddedServer(Netty, port = 3500) {
        install(WebSockets) {
            pingPeriod = Duration.ofMinutes(1)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        routing {
            webSocket("/ws") {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val receivedText = frame.readText()
                            send("Server received: $receivedText")
                            println(receivedText)
                        }
                        is Frame.Binary -> TODO()
                        is Frame.Close -> TODO()
                        is Frame.Ping -> TODO()
                        is Frame.Pong -> TODO()
                    }
                }
            }
        }
    }.start(wait = true)

    var isVisible by remember { mutableStateOf(isVisible.value) }

    LaunchedEffect(isVisible) {
        println("isVisible: ${isVisible}")
    }

    Window(
        onCloseRequest = { isVisible = false },
        visible = isVisible,
        title = "Counter",
    ) {
        App()
    }

    if (!isVisible) {
        Tray(
            TrayIcon,
            tooltip = "Counter",
            onAction = { isVisible= true },
            menu = {
                Item("Exit", onClick = ::exitApplication)
            },
        )
    }
}

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFFA500))
    }
}
