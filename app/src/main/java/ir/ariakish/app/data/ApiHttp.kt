package ir.ariakish.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal data class HttpResult(val code: Int, val body: String)

internal object ApiHttp {
    suspend fun get(url: String, token: String? = null): HttpResult = request("GET", url, null, token)
    suspend fun post(url: String, body: JSONObject, token: String? = null): HttpResult = request("POST", url, body.toString(), token)
    suspend fun put(url: String, body: JSONObject, token: String? = null): HttpResult = request("PUT", url, body.toString(), token)

    private suspend fun request(method: String, url: String, body: String?, token: String?): HttpResult = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Ariakish-Android/1.0")
            if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        try {
            if (body != null) connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            HttpResult(code, response)
        } finally {
            connection.disconnect()
        }
    }

    fun message(result: HttpResult, fallback: String): String = try {
        JSONObject(result.body).optString("message").ifBlank { fallback }
    } catch (_: Exception) { fallback }
}
