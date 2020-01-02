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

data class User(val name: String, val lastname: String, val number: Int, val position: String, val birthDate: String)
data class PostUser(val name: PostUser.Text, val lastname: PostUser.Text, val number: PostUser.Text, val position: PostUser.Text, val birthDate: PostUser.Text) {
    data class Text(val text: String)
}
data class Parameters(val id: Int)

val client = KMongo.createClient()
val database = client.getDatabase("hockey")

@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    val client = HttpClient(Apache) {
        routing {
            get("/api/v2/getUsers") {
                val col = database.getCollection<User>("users")
                val limit: Int? = call.request.queryParameters["limit"]?.toInt()
                val list: List<User> = col.find().limit(if(limit === null) 50 else limit).toMutableList()
                val count: Int = list.count()
                call.respond(mapOf("count" to count, "users" to list))
            }
            get("/api/v2/getUser/") {
                val col = database.getCollection<User>("users")
                val name: String? = call.request.queryParameters["name"]
                val position: String? = call.request.queryParameters["position"]
                val number: Int? = call.request.queryParameters["number"]?.toInt()
                val limit: Int? = call.request.queryParameters["limit"]?.toInt()
                val list: List<User> = col.find(or(User::name eq name, User::position eq position, User::number eq number)).limit(if(limit === null) 50 else limit).toMutableList()
                val count: Int = list.count()
                call.respond(mapOf("count" to count, "users" to list))
            }
            post("/api/v2/addUser") {
                val post = call.receive<PostUser>()
                val name: String = post.name.text
                val lastname: String = post.lastname.text
                val position: String = post.position.text
                val number: Int = post.number.text.toInt()
                val birthDate: String = post.birthDate.text
                val col = database.getCollection<User>("users")
                col.insertOne(User(name, lastname, number, position, birthDate))
                call.respond(mapOf("OK" to true))
            }
            delete("/api/v2/deleteUser") {
                val params = call.receive<PostUser>()
            }
        }
    }
}