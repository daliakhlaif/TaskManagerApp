package com.example.task_manager.viewModel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.task_manager.model.Category
import com.example.task_manager.model.Task
import com.example.task_manager.repository.TaskDatabaseHelper
import com.example.task_manager.service.AlarmReceiver
import com.example.task_manager.view.HomeActivity
import com.example.task_manager.view.TaskActivity
import com.example.task_manager.view.TaskActivity.Companion.ACTION_CATEGORY_ADDED
import java.time.ZoneId

class TaskViewModel(private val context: Context) : ViewModel() {
    private val dbHelper = TaskDatabaseHelper(context)

    fun addCategory(name: String, color: Int) {
        val category = Category(0, name, color)
        sendCategoryAddedBroadcast()
        dbHelper.addCategory(category)
    }

    fun getAllCategories() : List<Category>{
        return dbHelper.getAllCategories()
    }

    fun addTask(task: Task) {
        val insertedTask = dbHelper.addTask(task)
        sendTaskUpdatedBroadcast()
        dbHelper.getTaskById(insertedTask.toInt())?.let { scheduleAlarmForTask(it) }
    }

    private fun scheduleAlarmForTask(task: Task) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(TaskActivity.EXTRA_TASK_ID, task.taskId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val dueDateTimeMillis = task.dueDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            dueDateTimeMillis,
            pendingIntent
        )
    }

    private fun cancelAlarmForTask(task: Task) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.taskId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    fun getTask(taskId: Int): Task? {
        return dbHelper.getTaskById(taskId)
    }

    fun getCategoryByName(categoryName: String): Category? {
        return dbHelper.getCategoryByName(categoryName)
    }

    fun getCategory(categoryId: Int): Category? {
        return dbHelper.getCategoryById(categoryId)
    }
    fun updateTask(task: Task) {
        cancelAlarmForTask(task)
        dbHelper.updateTask(task)
        sendTaskUpdatedBroadcast()
        scheduleAlarmForTask(task)
    }

    private fun sendCategoryAddedBroadcast() {
        val intent = Intent(ACTION_CATEGORY_ADDED)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun sendTaskUpdatedBroadcast() {
        val intent = Intent(HomeActivity.ACTION_TASK_UPDATED)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}