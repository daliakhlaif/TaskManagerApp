package com.example.task_manager.adapter
import com.example.task_manager.model.Task

interface OnTaskItemClickListener {
    fun onItemClick(task: Task)
}