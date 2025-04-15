package com.example

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*


fun Application.configureRouting() {
    val webhookUrl = environment.config.property("ktor.discord.webhookUrl").getString()
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }

    routing {
        post("/send-to-discord") {
            val message = call.receiveParameters()["message"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing 'message' parameter")
                return@post
            }

            try {
                val response = httpClient.post(webhookUrl) {
                    contentType(ContentType.Application.Json)
                    setBody("""{"content":"$message"}""")
                }

                when (response.status) {
                    HttpStatusCode.NoContent -> call.respond("Message sent to Discord!")
                    else -> call.respond(
                        HttpStatusCode.InternalServerError,
                        "Failed to send message: ${response.status}"
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Error sending message: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
}
