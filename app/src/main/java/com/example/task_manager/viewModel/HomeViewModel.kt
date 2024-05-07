package com.example.task_manager.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.task_manager.model.Category
import com.example.task_manager.model.Task
import com.example.task_manager.repository.TaskDatabaseHelper


class HomeViewModel(private val context: Context) : ViewModel() {

    private val databaseHelper = TaskDatabaseHelper(context)

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>>
        get() = _categories

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>>
        get() = _tasks

    private val _upcomingTask = MutableLiveData<Task?>()
    val upcomingTask: LiveData<Task?>
        get() = _upcomingTask


   init {
       getAllUpcomingTasks()
       getAllCategories()
       getFirstUpcomingTask()
   }
    fun getAllCategories() {
        val allCategories = databaseHelper.getAllCategories()
        _categories.postValue(allCategories)
    }

     fun getFirstUpcomingTask() {
        val upcomingTasks = databaseHelper.getAllUpcomingTasks()
        _upcomingTask.postValue(upcomingTasks.firstOrNull())
    }

    fun getAllUpcomingTasks()  {
        val upcomingTasks = databaseHelper.getAllUpcomingTasks()
        _tasks.postValue(upcomingTasks)
    }
}
