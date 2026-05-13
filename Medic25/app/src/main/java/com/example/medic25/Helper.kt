package com.example.medic25

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime

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

data class SavedDoctor(
    val id: Int,
    val doctorId: Int,
    val doctorName: String,
    val specialty: String,
    val experience: String,
    val location: String
)

data class Appointment(
    val id: Int,
    val doctorId: Int,
    val doctorName: String,
    val specialty: String,
    val paymentMethod: String,
    val status: String,
    val createdAt: LocalDateTime
)

data class Coupon(
    val code: String,
    val quota: Int,
    val discount: Double,
    val expiryDate: LocalDateTime
)

data class User(
    val id: Int,
    val username: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String
)

object HttpClient {
    val addr = "http://10.0.2.2:5000/medicgo-api/v1/"
    var token = ""


    val savedDoctors = mutableStateListOf<SavedDoctor>()

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
        val res =
            jsonReq("users/login", """{"username": "$username", "password": "$password"}""", "POST")
        if (res.body.isNullOrEmpty()) return "Login Failed"
        if (res.code == 200) {
            val data = JSONObject(res.body).getJSONObject("data")
            token = data.getString("token")
            if (data.getString("role") == "admin") return "Not for admin"
            return "ok"
        }
        return try {
            JSONObject(res.body).getString("message")
        } catch (e: Exception) {
            "Login Failed"
        }
    }

    suspend fun register(
        username: String,
        fullname: String,
        email: String,
        phone: String,
        password: String
    ): String {
        val res = jsonReq(
            "users/register",
            """{"username": "$username", "fullname": "$fullname", "email": "$email", "phone": "$phone", "password": "$password"}""",
            "POST"
        )
        if (res.body.isNullOrEmpty()) return "Register Failed"
        if (res.code == 200) {
            return "ok"
        }
        return try {
            JSONObject(res.body).getString("message")
        } catch (e: Exception) {
            "Register Failed"
        }
    }

    suspend fun getDoctors(search: String, category: String): List<Doctor> {
        var url = "doctors?category=" + withContext(Dispatchers.IO) {
            URLEncoder.encode(category, "UTF-8")
        }
        if (search.trim() != "") {
            url += "&search=" + withContext(Dispatchers.IO) {
                URLEncoder.encode(search, "UTF-8")
            }
        }
        val res = jsonReq(url)
        if (res.code != 200 || res.body.isNullOrEmpty()) return emptyList()
        val json = JSONObject(res.body).getJSONArray("data")
        val arr = mutableListOf<Doctor>()
        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            arr.add(
                Doctor(
                    obj.getInt("id"),
                    obj.getString("name"),
                    obj.getString("specialty"),
                    obj.getString("experience"),
                    obj.getString("location"),
                    obj.getDouble("price"),
                )
            )
        }
        return arr
    }

    suspend fun getDoctorById(id: Int): Doctor? {
        val res = jsonReq("doctors/$id")
        if (res.code != 200 || res.body.isNullOrEmpty()) return null
        val obj = JSONObject(res.body).getJSONObject("data")
        val exprts = obj.getJSONArray("expertise")
        val expertise = mutableListOf<Expertise>()
        for (i in 0 until exprts.length()) {
            val obj2 = exprts.getJSONObject(i)
            expertise.add(
                Expertise(
                    obj2.getString("title"),
                    obj2.getString("content"),
                )
            )
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

    suspend fun saveDoctor(id: Int): Boolean {
        val json = """{"doctorId": $id}"""
        val res = jsonReq("saved-doctors", json, "POST")
        getSavedDoctors()
        return res.code == 200
    }

    suspend fun removeSavedDoctor(id: Int): Boolean {
        val res = jsonReq("saved-doctors/$id", method = "DELETE")
        getSavedDoctors()
        return res.code == 200
    }

    suspend fun getSavedDoctors() {
        val res = jsonReq("saved-doctors")
        if (res.code != 200 || res.body == null) return
        val jsonArr = JSONObject(res.body).getJSONArray("data")
        savedDoctors.clear()
        for (i in 0 until jsonArr.length()) {
            val obj = jsonArr.getJSONObject(i)
            savedDoctors.add(
                SavedDoctor(
                    obj.getInt("savedId"),
                    obj.getInt("doctorId"),
                    obj.getString("doctorName"),
                    obj.getString("specialty"),
                    obj.getString("experience"),
                    obj.getString("location"),
                )
            )
        }
    }

    suspend fun getAppointments(): List<Appointment> {
        val res = jsonReq("appointments")
        if (res.code != 200 || res.body == null) return emptyList()
        val jsonArr = JSONObject(res.body).getJSONArray("data")
        val arr = mutableListOf<Appointment>()
        for (i in 0 until jsonArr.length()) {
            val obj = jsonArr.getJSONObject(i)
            arr.add(
                Appointment(
                    obj.getInt("id"),
                    obj.getInt("doctorId"),
                    obj.getString("doctorName"),
                    obj.getString("specialty"),
                    obj.getString("paymentMethod"),
                    obj.getString("status"),
                    LocalDateTime.parse(obj.getString("createdAt")),
                )
            )
        }
        return arr
    }

    suspend fun bookAppointment(doctorId: Int, paymentMethod: String, couponCode: String): String {
        val json =
            """{"doctorId": $doctorId, "paymentMethod": "$paymentMethod", "couponCode": "$couponCode"}"""
        val res = jsonReq("appointments", json, "POST")
        if (res.body == null) return "Booking Failed"
        return try {
            if (res.code == 200) "ok"
            else {
                val json = JSONObject(res.body)
                json.getString("message")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Booking Failed"
        }
    }

    suspend fun checkCoupon(code: String): Pair<String, Coupon?> {
        val res = jsonReq("coupons/check/$code")
        println(res)
        if (res.body == null) return Pair("Can't get coupon data", null)
        return try {
            println(res.body)
            val json = JSONObject(res.body)
            if (res.code == 404) return Pair("Coupon not found", null)
            if (res.code == 200) {
                val data = json.getJSONObject("data")
                Pair(
                    "ok", Coupon(
                        data.getString("code"),
                        data.getInt("quota"),
                        data.getDouble("discount"),
                        LocalDateTime.parse(data.getString("expiryDate"))
                    )
                )
            } else {
                Pair(json.getString("message"), null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(e.message ?: "Can't get coupon data", null)
        }
    }

    suspend fun me(): User? {
        val res = jsonReq("users/profile")
        if (res.body == null || res.code != 200) return null
        val json = JSONObject(res.body).getJSONObject("data")
        return User(
            json.getInt("userId"),
            json.getString("username"),
            json.getString("fullName"),
            json.getString("email"),
            json.getString("phone"),
            json.getString("role"),
        )
    }
}