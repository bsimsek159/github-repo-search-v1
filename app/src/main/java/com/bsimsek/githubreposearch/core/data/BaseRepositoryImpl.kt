package com.bsimsek.githubreposearch.core.data

import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import javax.inject.Singleton

@Singleton
open class BaseRepositoryImpl {

    suspend fun <T : Any> handleApiCall(call: suspend () -> Response<T?>): T? {
        val result = getApiResult(call)
        var data: T? = null
        when (result) {
            is DataHolder.Loading -> DataHolder.Loading
            is DataHolder.Success -> data = result.data
            is DataHolder.Fail -> DataHolder.Fail(result.e)
            is DataHolder.NoInternetConnection -> DataHolder.Fail(NoInternetException())
        }

        return data
    }

    private suspend fun <T : Any> getApiResult(call: suspend () -> Response<T?>): DataHolder<T> {
        val result: DataHolder<T>
        result = try {
            val response = call.invoke()
            if (response.isSuccessful)
                DataHolder.Success(response.body()!!)
            else
                DataHolder.Fail(IOException(setErrorMessage(response)))
        } catch (exception: IOException) {
            if (exception is NoInternetException)
                DataHolder.Fail(exception)
            else
                DataHolder.Fail(exception)
        }
        return result
    }

    private fun <T : Any> setErrorMessage(response: Response<T?>): String {
        val code = response.code().toString()
        val message = try {
            val jObjError = JSONObject(response.errorBody()?.string())
            jObjError.getJSONObject("error").getString("message")
        } catch (e: Exception) {
            e.message
        }
        return if (message.isNullOrEmpty()) " error code = $code " else " error code = $code  & error message = $message "
    }
}