package com.example.medic25

import android.accessibilityservice.GestureDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.exp

data class HttpReq(
    val url: String,
    val body: String = "",
    val method: String = "GET",
    val headers: Map<String, String> = emptyMap(),
    val timeout: Int = 10000
)

data class HttpRes(
    val code: Int,
    val body: String? = null,
    val headers: Map<String, List<String>> = emptyMap(),
    val errors: String? = null
)

data class Doctor(
    val id: Int,
    val name: String,
    val specialty: String,
    val experience: String,
    val location: String,
    val price: Double,
    val description: String = "",
    val duration: Int = 0,
    val expertise: List<Expertise> = emptyList(),
)

data class Expertise(
    val title: String,
    val content: String
)

object HttpClient {
    val addr = "http://10.0.2.2:5000/medicgo-api/v1/"
    var token = ""

    fun send(req: HttpReq): HttpRes {
        val conn = URL(req.url).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = req.method
            conn.readTimeout = req.timeout
            conn.connectTimeout = req.timeout
            req.headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            if (req.body.isNotEmpty() && req.method in listOf("POST", "PUT", "PATCH")) {
                conn.getOutputStream().buffered().use { it.write(req.body.toByteArray()) }
            }

            conn.connect()
            val code = conn.responseCode
            val body = if (code in 200..299) {
                conn.getInputStream().bufferedReader().use { it.readText() }
            } else {
                conn.errorStream?.bufferedReader()?.use { it.readText() }
            }
            HttpRes(code, body, conn.headerFields)
        } catch (e: Exception) {
            HttpRes(-1, e.message ?: "Network error")
        } finally {
            conn.disconnect()
        }
    }

    suspend fun jsonReq(route: String, body: String = "", method: String = "GET"): HttpRes {
        val headers = if (token.isNotEmpty()) mapOf(
            "content-type" to "application/json",
            "authorization" to "Bearer $token"
        ) else mapOf("content-type" to "application/json")
        return withContext(Dispatchers.IO) {
            send(HttpReq(addr + route, body, method, headers))
        }
    }

    suspend fun login(username: String, password: String): String {
        val res = jsonReq("users/login", """{"username": "$username", "password": "$password"}""", "POST")
        if(res.body.isNullOrEmpty()) return "Login Failed"
        if(res.code == 200) {
            val data = JSONObject(res.body).getJSONObject("data")
            token = data.getString("token")
            if(data.getString("role") == "admin") return "Not for admin"
            return "ok"
        }
        return try {
            JSONObject(res.body).getString("message")
        } catch (e: Exception) {
            "Login Failed"
        }
    }

    suspend fun register(username: String, fullname: String, email: String, phone: String, password: String): String {
        val res = jsonReq("users/register", """{"username": "$username", "fullname": "$fullname", "email": "$email", "phone": "$phone", "password": "$password"}""", "POST")
        if(res.body.isNullOrEmpty()) return "Register Failed"
        if(res.code == 200) {
            return "ok"
        }
        return try {
            JSONObject(res.body).getString("message")
        } catch (e: Exception) {
            "Register Failed"
        }
    }

    suspend fun getDoctors(search: String): List<Doctor> {
        var url = "doctors"
        if(search.trim() != "") {
            url += "?search=" + URLEncoder.encode(search, "UTF-8")
        }
        val res = jsonReq(url)
        if(res.code != 200 || res.body.isNullOrEmpty()) return emptyList()
        val json = JSONObject(res.body).getJSONArray("data")
        val arr = mutableListOf<Doctor>()
        for(i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            arr.add(Doctor(
                obj.getInt("id"),
                obj.getString("name"),
                obj.getString("specialty"),
                obj.getString("experience"),
                obj.getString("location"),
                obj.getDouble("price"),
            ))
        }
        return arr
    }

    suspend fun getDoctorById(id: Int): Doctor? {
        val res = jsonReq("doctors/$id")
        if(res.code != 200 || res.body.isNullOrEmpty()) return null
        val obj = JSONObject(res.body).getJSONObject("data")
        val exprts = obj.getJSONArray("expertise")
        val expertise = mutableListOf<Expertise>()
        for(i in 0 until exprts.length()) {
            val obj2 = exprts.getJSONObject(i)
            expertise.add(Expertise(
                obj2.getString("title"),
                obj2.getString("content"),
            ))
        }
        return Doctor(
            obj.getInt("id"),
            obj.getString("name"),
            obj.getString("specialty"),
            obj.getString("experience"),
            obj.getString("location"),
            obj.getDouble("price"),
            obj.getString("description"),
            obj.getInt("duration"),
            expertise,
        )
    }
}