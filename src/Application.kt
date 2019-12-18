package com.hockey_api_v2

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.features.ContentNegotiation
import io.ktor.http.Parameters
import io.ktor.jackson.*
import io.ktor.routing.*
import java.util.*
import org.litote.kmongo.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf

data class Snippet(val text: String)
data class User(val name: String, val lastname: String, val number: Int, val position: String, val birthDate: String)


val client = KMongo.createClient()
val database = client.getDatabase("hockey")

val snippets = Collections.synchronizedList(mutableListOf(
    Snippet("hello"),
    Snippet("world")
))

@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    val client = HttpClient(Apache) {
        routing {
            get("/v2/getUsers") {
                val col = database.getCollection<User>("users")
                val list: List<User> = col.find().toMutableList()
                val count: Int = list.count()
                call.respond(mapOf("count" to count, "users" to list))
            }
            get("/v2/getUser/") {
                val col = database.getCollection<User>("users")
                val param1: String? = call.request.queryParameters["name"]
                val list: List<User> = col.find(User::name eq param1).toMutableList()
                val count: Int = list.count()
                call.respond(mapOf("count" to count, "users" to list))
            }
        }
    }
}