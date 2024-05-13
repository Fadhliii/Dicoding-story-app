package com.anisanurjanah.dicodingstoryapp.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.anisanurjanah.dicodingstoryapp.data.pref.UserPreference
import com.anisanurjanah.dicodingstoryapp.data.remote.response.StoryItem
import com.anisanurjanah.dicodingstoryapp.data.remote.retrofit.ApiService
import com.anisanurjanah.dicodingstoryapp.data.Result
import com.anisanurjanah.dicodingstoryapp.data.remote.response.GeneralResponse
import com.anisanurjanah.dicodingstoryapp.data.remote.response.LoginResponse
import com.anisanurjanah.dicodingstoryapp.data.remote.response.LoginResult
import com.anisanurjanah.dicodingstoryapp.data.remote.response.StoriesResponse
import com.anisanurjanah.dicodingstoryapp.data.remote.retrofit.ApiConfig
import com.anisanurjanah.dicodingstoryapp.utils.reduceFileImage
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class StoryRepository private constructor(
    private var apiService: ApiService,
    private val userPreference: UserPreference
) {

    private var token: String? = null

    private suspend fun getToken(): String? = token ?: runBlocking {
        userPreference.getToken().first()
    }.also { token = it }

    fun register(name: String, email: String, password: String): LiveData<Result<GeneralResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.register(name, email, password)

            emit(Result.Success(response))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, GeneralResponse::class.java)

            emit(Result.Error(errorResponse.message.toString()))
        } catch (e: Exception) {
            Log.d(TAG, "register: ${e.message}")

            emit(Result.Error(e.message.toString()))
        }
    }

    fun login(email: String, password: String): LiveData<Result<LoginResult>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, password)
            val loginResult = response.loginResult

            if (loginResult != null) {
                emit(Result.Success(loginResult))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, LoginResponse::class.java)

            emit(Result.Error(errorResponse.message.toString()))
        } catch (e: Exception) {
            Log.d(TAG, "login: ${e.message}")

            emit(Result.Error(e.message.toString()))
        }
    }

    fun getAllStories(): LiveData<Result<List<StoryItem>>> = liveData {
        emit(Result.Loading)
        try {
            val token = getToken()
            apiService = ApiConfig.getApiService(token.toString())

            val response = apiService.getStories()
            val storyItem = response.listStory ?: emptyList()

            emit(Result.Success(storyItem.filterNotNull()))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, StoriesResponse::class.java)

            emit(Result.Error(errorResponse.message.toString()))
        } catch (e: Exception) {
            Log.d(TAG, "getAllStories: ${e.message}")

            emit(Result.Error(e.message.toString()))
        }
    }

    fun uploadNewStory(file: File?, description: String): LiveData<Result<GeneralResponse>> = liveData {
        emit(Result.Loading)
        try {
            val imageFile = reduceFileImage(file!!)
            Log.d("Image File", "showImage: ${imageFile.path}")

            val descriptionBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())

            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            val response = apiService.uploadNewStory(multipartBody, descriptionBody)

            emit((Result.Success(response)))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, GeneralResponse::class.java)

            emit(Result.Error(errorResponse.message.toString()))
        } catch (e: Exception) {
            Log.d(TAG, "uploadNewStory: ${e.message}")

            emit(Result.Error(e.message.toString()))
        }
    }

    companion object {
        private const val TAG = "StoryRepository"
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService, userPreference: UserPreference): StoryRepository {
            return instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, userPreference).also { instance = it }
            }
        }
    }
}