package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Task(val id: Int, val title: String, val description: String, val completed: Boolean = false)


val tasks = mutableListOf(
    Task(1, "Learn Ktor", "Study Ktor framework for web development", false),
    Task(2, "Build Task Manager", "Create a simple task manager API", true)
)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    routing {
        // GET всіх задач
        get("/tasks") {
            call.respond(tasks)
        }


        get("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respondText("Invalid task ID", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@get
            }

            val task = tasks.find { it.id == id }
            if (task == null) {
                call.respondText("Task not found", status = io.ktor.http.HttpStatusCode.NotFound)
            } else {
                call.respond(task)
            }
        }


        post("/tasks") {
            try {
                val task = call.receive<Task>()

                // Перевірка на унікальність ID
                if (tasks.any { it.id == task.id }) {
                    call.respondText("Task with this ID already exists", status = io.ktor.http.HttpStatusCode.Conflict)
                    return@post
                }


                tasks.add(task)
                call.respondText("Task added successfully: ${task.title}", status = io.ktor.http.HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respondText("Invalid task data: ${e.message}", status = io.ktor.http.HttpStatusCode.BadRequest)
            }
        }


        get("/") {
            call.respondText("Task Manager API is running! Use /tasks endpoint")
        }
    }
}