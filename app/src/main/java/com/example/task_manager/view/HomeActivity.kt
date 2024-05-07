package com.example.task_manager.view

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.task_manager.adapter.CategoriesListAdapter
import com.example.task_manager.adapter.OnCategoryItemClickListener
import com.example.task_manager.adapter.OnTaskItemClickListener
import com.example.task_manager.adapter.TasksListAdapter
import com.example.task_manager.model.Category
import com.example.task_manager.model.Task
import com.example.task_manager.service.AlarmReceiver
import com.example.task_manager.util.GlobalKeys.CATEGORY_ID
import com.example.task_manager.util.GlobalKeys.TASK_ID
import com.example.task_manager.viewModel.HomeViewModel
import com.example.task_manager.viewModel.HomeViewModelFactory
import com.example.taskmanagerapp.R
import com.example.taskmanagerapp.databinding.ActivityHomeBinding
import java.time.Instant


class HomeActivity : AppCompatActivity(), OnCategoryItemClickListener, OnTaskItemClickListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var categoryAdapter: CategoriesListAdapter
    private lateinit var taskAdapter: TasksListAdapter
    private lateinit var alarmManager :AlarmManager
    private lateinit var pendingIntent :PendingIntent
    private lateinit var task :Task


    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(this)
    }

    companion object {
        const val ACTION_TASK_UPDATED = "com.example.task_manager.ACTION_TASK_SAVED"
        const val NOTIFICATION_CHANNEL_ID = "taskManager"
    }

    private val taskUpdatedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_TASK_UPDATED) {
                viewModel.getAllUpcomingTasks()
                viewModel.getAllCategories()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        recyclerView = binding.CategoriesRecycler
        registerBroadcastReceiver()
        setContentView(binding.root)
        initialize()
    }

    private fun initialize(){
        initializeRecyclerViews()
        setupAddButtonListener()
        observeViewModel()
    }

    private fun registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(taskUpdatedReceiver, IntentFilter(ACTION_TASK_UPDATED))
    }


    private fun observeViewModel() {
        viewModel.categories.observe(this) { categories ->
            categoryAdapter.updateCategories(categories)
        }
        viewModel.tasks.observe(this) { tasks ->
            taskAdapter.updateTasks(tasks)
        }
    }

    private fun initializeRecyclerViews() {
        // Categories RecyclerView
        recyclerView = binding.CategoriesRecycler
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoryAdapter = CategoriesListAdapter(this, emptyList(), this)
        recyclerView.adapter = categoryAdapter

        // Tasks RecyclerView
        recyclerViewTasks = binding.tasksRecycler
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TasksListAdapter(this, emptyList(), this)
        recyclerViewTasks.adapter = taskAdapter
    }

    private fun setupAddButtonListener(){
        binding.addButton.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onItemClick(category: Category) {
        val intent = Intent(this, CategoryTasksActivity::class.java)
        intent.putExtra(CATEGORY_ID, category.categoryId )
        startActivity(intent)
    }

    override fun onItemClick(task: Task) {
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra(TASK_ID, task.taskId)
        startActivity(intent)
    }



}

