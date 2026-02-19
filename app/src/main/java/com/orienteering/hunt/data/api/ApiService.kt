package com.orienteering.hunt.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.delete
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiException(message: String, val code: Int = 0) : Exception(message)

interface ApiService {
    suspend fun register(username: String, email: String, password: String): Result<AuthResponse>
    suspend fun login(username: String, password: String): Result<AuthResponse>
    suspend fun getCurrentUser(): Result<UserDto>
    suspend fun updateProfile(username: String?, email: String?): Result<UserDto>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun getHunts(search: String? = null, difficulty: String? = null): Result<List<HuntDto>>
    suspend fun getHunt(id: String): Result<HuntDto>
    suspend fun startHunt(huntId: String): Result<ProgressDto>
    suspend fun getProgress(): Result<List<ProgressDto>>
    suspend fun getProgress(huntId: String): Result<ProgressDto>
    suspend fun abandonHunt(huntId: String): Result<Unit>
    suspend fun checkIn(huntId: String, latitude: Double, longitude: Double): Result<CheckInResponse>
    suspend fun getLeaderboard(huntId: String): Result<List<LeaderboardEntryDto>>
    suspend fun getGlobalLeaderboard(): Result<List<LeaderboardEntryDto>>
    fun setAuthToken(token: String?)
}

class KtorApiService(
    private val baseUrl: String = "https://orienteering-game.fly.dev"
) : ApiService {
    
    private var authToken: String? = null
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        HttpResponseValidator {
            validateResponse { response ->
                when (response.status.value) {
                    in 400..499 -> {
                        val errorBody = try {
                            json.decodeFromString<ApiError>(response.bodyAsText())
                        } catch (e: Exception) {
                            ApiError(error = response.bodyAsText())
                        }
                        throw ApiException(errorBody.message ?: errorBody.error, response.status.value)
                    }
                    in 500..599 -> {
                        throw ApiException("Server error. Please try again.", response.status.value)
                    }
                }
            }
        }
    }
    
    override fun setAuthToken(token: String?) {
        authToken = token
    }
    
    private suspend fun <T> safeRequest(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: ClientRequestException) {
            val errorBody = try {
                json.decodeFromString<ApiError>(e.response.bodyAsText())
            } catch (ex: Exception) {
                ApiError(error = e.message ?: "Request failed")
            }
            Result.failure(ApiException(errorBody.message ?: errorBody.error, e.response.status.value))
        } catch (e: ServerResponseException) {
            Result.failure(ApiException("Server error. Please try again.", e.response.status.value))
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException(e.message ?: "Network error. Check your connection."))
        }
    }
    
    override suspend fun register(username: String, email: String, password: String): Result<AuthResponse> {
        return safeRequest {
            client.post("$baseUrl/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(username, email, password))
            }.body()
        }
    }
    
    override suspend fun login(username: String, password: String): Result<AuthResponse> {
        return safeRequest {
            client.post("$baseUrl/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }.body()
        }
    }
    
    override suspend fun getCurrentUser(): Result<UserDto> {
        return safeRequest {
            client.get("$baseUrl/api/users/me") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.body()
        }
    }
    
    override suspend fun updateProfile(username: String?, email: String?): Result<UserDto> {
        return safeRequest {
            client.put("$baseUrl/api/users/me") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(UpdateProfileRequest(username, email))
            }.body()
        }
    }
    
    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return safeRequest {
            client.post("$baseUrl/api/users/me/password") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(ChangePasswordRequest(oldPassword, newPassword))
            }
            Unit
        }
    }
    
    override suspend fun getHunts(search: String?, difficulty: String?): Result<List<HuntDto>> {
        return safeRequest {
            val url = buildString {
                append("$baseUrl/api/hunts")
                val params = mutableListOf<String>()
                search?.let { params.add("search=$it") }
                difficulty?.let { params.add("difficulty=$it") }
                if (params.isNotEmpty()) {
                    append("?")
                    append(params.joinToString("&"))
                }
            }
            client.get(url) {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.body()
        }
    }
    
    override suspend fun getHunt(id: String): Result<HuntDto> {
        return safeRequest {
            client.get("$baseUrl/api/hunts/$id") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.body()
        }
    }
    
    override suspend fun startHunt(huntId: String): Result<ProgressDto> {
        return safeRequest {
            client.post("$baseUrl/api/hunts/$huntId/start") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.body()
        }
    }
    
    override suspend fun getProgress(): Result<List<ProgressDto>> {
        return safeRequest {
            client.get("$baseUrl/api/progress") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.body()
        }
    }
    
    override suspend fun getProgress(huntId: String): Result<ProgressDto> {
        return safeRequest {
            client.get("$baseUrl/api/progress/$huntId") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.body()
        }
    }
    
    override suspend fun abandonHunt(huntId: String): Result<Unit> {
        return safeRequest {
            client.delete("$baseUrl/api/progress/$huntId") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }
            Unit
        }
    }
    
    override suspend fun checkIn(huntId: String, latitude: Double, longitude: Double): Result<CheckInResponse> {
        return safeRequest {
            client.post("$baseUrl/api/progress/$huntId/checkin") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(CheckInRequest(latitude, longitude))
            }.body()
        }
    }
    
    override suspend fun getLeaderboard(huntId: String): Result<List<LeaderboardEntryDto>> {
        return safeRequest {
            client.get("$baseUrl/api/leaderboards/$huntId") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.body()
        }
    }
    
    override suspend fun getGlobalLeaderboard(): Result<List<LeaderboardEntryDto>> {
        return safeRequest {
            client.get("$baseUrl/api/leaderboards/global") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.body()
        }
    }
}
