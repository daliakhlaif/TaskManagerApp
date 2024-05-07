package com.example.task_manager.service

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.task_manager.repository.TaskDatabaseHelper
import com.example.task_manager.view.HomeActivity
import com.example.task_manager.view.HomeActivity.Companion.NOTIFICATION_CHANNEL_ID
import com.example.task_manager.view.TaskActivity
import com.example.taskmanagerapp.R

class AlarmReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        val taskId = intent.getIntExtra(TaskActivity.EXTRA_TASK_ID, -1)

        val taskTitle = getTaskTitle(context, taskId)

        val contentIntent = createContentIntent(context, taskId)

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_task_24)
            .setContentTitle(context.getString(R.string.content_title))
            .setContentText(context.getString(R.string.content_text, taskTitle))
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
            return
        }
        notificationManager.notify(taskId, builder.build())
    }

    private fun getTaskTitle(context: Context, taskId: Int): String {
        val dbHelper = TaskDatabaseHelper (context)
        val task = dbHelper.getTaskById(taskId)
        if (task != null) {
            return task.description
        }
        return " "
    }

    private fun createNotificationChannel(context: Context) {
        val channelName = context.getString(R.string.notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            importance
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createContentIntent(context: Context, taskId: Int): PendingIntent {
        val intent = Intent(context, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(TaskActivity.EXTRA_TASK_ID, taskId)
        return PendingIntent.getActivity(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100

    }

}