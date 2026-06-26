package com.example.medicgo

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class HttpReq(
    val route: String,
    val method: String = "GET",
    val body: String = "",
    val headers: Map<String, String> = emptyMap(),
    val timeout: Int = 10000
)

data class HttpRes(
    val code: Int,
    val body: String? = null,
    val error: String? = null
)

data class Profile(
    val username: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String
)

data class Doctor(
    val id: Int,
    val name: String,
    val specialty: String,
    val experience: String,
    val location: String,
    val description: String = "",
    val expertises: List<Exp> = emptyList()
)

data class Appointment(
    val id: Int,
    val doctorId: Int,
    val doctorName: String,
    val doctorSpecialty: String,
    val paymentMethod: String,
    val status: String = "",
)

data class Exp(
    val title: String,
    val content: String
)

data class SavedDoctor(
    val savedId: Int,
    val doctorId: Int,
    val doctorName: String,
    val specialty: String,
    val experience: String,
    val location: String,
)

object HttpClient {
    val addr = "http://10.0.2.2:5000/medicgo-api/v1/"
    var token = ""

    var profile by mutableStateOf<Profile?>(null)
    var savedDoctors = mutableStateListOf<SavedDoctor>()
    lateinit var prefs: SharedPreferences

    fun saveToken() {
        prefs.edit {
            putString("token", token)
        }
    }

    fun loadToken() {
        token = prefs.getString("token", "") ?: ""
    }

    suspend fun send(rq: HttpReq): HttpRes {
        return withContext(Dispatchers.IO) {
            val c = URL("${addr}${rq.route}").openConnection() as HttpURLConnection
            try {
                c.run {
                    requestMethod = rq.method
                    connectTimeout = rq.timeout
                    readTimeout = rq.timeout
                    if (token.isNotEmpty()) setRequestProperty("authorization", "Bearer $token")
                    setRequestProperty("content-type", "application/json")
                    rq.headers.forEach { k, v -> setRequestProperty(k, v) }
                    if (rq.body.isNotEmpty() && rq.method in listOf("POST", "PUT", "PATCH")) {
                        getOutputStream().buffered().use { it.write(rq.body.toByteArray()) }
                    }

                    connect()
                    val code = responseCode
                    val body = if (code in 200..299) {
                        getInputStream().bufferedReader().use { it.readText() }
                    } else {
                        errorStream?.bufferedReader()?.use { it.readText() }
                    }
                    HttpRes(code, body)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                HttpRes(-1, error = e.message ?: "Network error")
            } finally {
                c.disconnect()
            }
        }
    }

    suspend fun jsonReq(
        route: String,
        method: String = "GET",
        body: String = "",
        errMsg: String = "Error",
        onSuccess: JSONObject.() -> Unit
    ): String {
        val res = send(HttpReq(route, method, body))
        if (res.body == null) return errMsg
        return try {
            val json = JSONObject(res.body)
            if (res.code == 200) {
                json.run(onSuccess)
                "ok"
            } else {
                json.optString("message", errMsg)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            errMsg
        }
    }

    suspend fun login(username: String, password: String): String {
        return jsonReq(
            "users/login", "POST", """{
  "username": "$username",
  "password": "$password"
}""", "Login failed",
            {
                token = getJSONObject("data").getString("token")
                saveToken()
            })
    }

    suspend fun register(
        username: String,
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): String {
        return jsonReq(
            "users/register", "POST", """{
  "username": "$username",
  "password": "$password"
  "fullName": "$fullName"
  "email": "$email"
  "phone": "$phone"
}""", "Login failed",
            {
                token = getJSONObject("data").getString("token")
                saveToken()
            })
    }

    suspend fun profile(): Boolean {
        return jsonReq("users/profile", onSuccess = {
            profile = getJSONObject("data").run {
                Profile(
                    getString("username"),
                    getString("fullName"),
                    getString("email"),
                    getString("phone"),
                    getString("role"),
                )
            }
        }) == "ok"
    }

    suspend fun getDoctors(specialty: String, search: String): List<Doctor> {
        val src = withContext(Dispatchers.IO) {
            URLEncoder.encode(search, "UTF-8")
        }
        val arr = mutableListOf<Doctor>()
        jsonReq("doctors?specialty=${specialty.lowercase()}&search=$src", onSuccess = {
            arr.addAll(getJSONArray("data").toList {
                Doctor(
                    getInt("id"),
                    getString("name"),
                    getString("specialty"),
                    getString("experience"),
                    getString("location"),
                )
            })
        })
        return arr
    }

    suspend fun getDoctor(id: Int): Doctor? {
        var doc: Doctor? = null
        jsonReq("doctors/$id", onSuccess = {
            doc = getJSONObject("data").run {
                Doctor(
                    getInt("id"),
                    getString("name"),
                    getString("specialty"),
                    getString("experience"),
                    getString("location"),
                    getString("description"),
                    getJSONArray("expertise").toList {
                        Exp(
                            getString("title"),
                            getString("content")
                        )
                    }
                )
            }
        })
        return doc
    }

    suspend fun bookDoctor(doctorId: Int): String {
        return jsonReq(
            "appointments", "POST", """{
  "doctorId": $doctorId,
  "paymentMethod": "paypal",
  "couponCode": ""
}""", "Booking failed",
            {
            })
    }

    suspend fun getAppointments(): List<Appointment> {
        val arr = mutableListOf<Appointment>()
        jsonReq("appointments", onSuccess = {
            arr.addAll(getJSONArray("data").toList {
                Appointment(
                    getInt("id"),
                    getInt("doctorId"),
                    getString("doctorName"),
                    getString("doctorSpecialty"),
                    getString("paymentMethod"),
                    getString("status"),
                )
            })
        })
        return arr
    }

    suspend fun getSavedDoctors() {
        val arr = mutableListOf<SavedDoctor>()
        jsonReq("saved-doctors", onSuccess = {
            arr.addAll(getJSONArray("data").toList {
                SavedDoctor(
                    getInt("savedId"),
                    getInt("doctorId"),
                    getString("doctorName"),
                    getString("specialty"),
                    getString("experience"),
                    getString("location"),
                )
            })
        })
        savedDoctors.clear()
        savedDoctors.addAll(arr)
    }

    suspend fun addDoctor(id: Int): Boolean {
        return jsonReq(
            "saved-doctors", "POST", """{
  "doctorId": $id
}""", "Bookmarking doctor failed",
            {}) == "ok"
    }

    suspend fun rmDoctor(id: Int): Boolean {
        return jsonReq(
            "saved-doctors/$id", "DELETE", errMsg =  "Bookmarking doctor failed",
            onSuccess = {}) == "ok"
    }
}


fun <T> JSONArray.toList(transform: JSONObject.() -> T): List<T> {
    val arr = mutableListOf<T>()
    for (i in 0 until length()) {
        arr.add(getJSONObject(i).run(transform))
    }
    return arr
}

