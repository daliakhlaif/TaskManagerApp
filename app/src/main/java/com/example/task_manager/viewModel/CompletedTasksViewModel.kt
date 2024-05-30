package com.example.task_manager.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.task_manager.model.Task
import com.example.task_manager.repository.TaskDatabaseHelper

class CompletedTasksViewModel (private val context: Context) : ViewModel() {
    private val databaseHelper = TaskDatabaseHelper(context)

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>>
        get() = _tasks

    init {
        getCompletedTasks()
    }

    fun getCompletedTasks() {
        _tasks.postValue(databaseHelper.getCompletedTasks())
    }
}