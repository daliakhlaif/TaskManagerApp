package com.example.task_manager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.task_manager.view.CategoryTasksActivity

class CategoryTasksViewModelFactory (private val context: CategoryTasksActivity,  private val categoryId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryTasksViewModel::class.java)) {
            return CategoryTasksViewModel(context, categoryId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}