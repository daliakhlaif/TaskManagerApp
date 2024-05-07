package com.example.task_manager.adapter

import com.example.task_manager.model.Category

interface OnCategoryItemClickListener {
    fun onItemClick(category: Category)
}