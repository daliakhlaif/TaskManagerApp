package com.example.task_manager.model

import java.time.LocalDateTime

data class Task (
    var taskId :Int,
    var description : String,
    var dueDateTime: LocalDateTime,
    var completed : Boolean,
    var priority :  Priority,
    var categoryId: Int
    )