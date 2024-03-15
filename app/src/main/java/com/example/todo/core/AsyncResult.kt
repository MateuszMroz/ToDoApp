package com.example.todo.core

sealed class AsyncResult<out T> {
    data object Loading : AsyncResult<Nothing>()

    data class Error(val errorMessage: Int) : AsyncResult<Nothing>()

    data class Success<out T>(val data: T) : AsyncResult<T>()
}
