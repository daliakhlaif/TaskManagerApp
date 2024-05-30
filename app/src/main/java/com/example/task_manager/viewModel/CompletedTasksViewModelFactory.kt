package com.example.task_manager.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.task_manager.view.CompletedTasksActivity

class CompletedTasksViewModelFactory (private val context: CompletedTasksActivity) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompletedTasksViewModel::class.java)) {
            return CompletedTasksViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}