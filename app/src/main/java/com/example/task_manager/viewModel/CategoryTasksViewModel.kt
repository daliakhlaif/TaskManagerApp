package com.example.task_manager.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.task_manager.model.Category
import com.example.task_manager.model.Task
import com.example.task_manager.repository.TaskDatabaseHelper

class CategoryTasksViewModel(private val context: Context, private val categoryId: Int) : ViewModel(){

    private val databaseHelper = TaskDatabaseHelper(context)

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>>
        get() = _tasks

    init {
        getAllTasksForCategory()
    }

    fun getCategory(id : Int) : Category? {
        return databaseHelper.getCategoryById(id)
    }
     fun getAllTasksForCategory() {
        val tasksForCategory = databaseHelper.getAllTasksForCategory(categoryId)
        _tasks.postValue(tasksForCategory)
    }
}