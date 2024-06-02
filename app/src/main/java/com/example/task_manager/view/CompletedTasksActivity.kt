package com.example.task_manager.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.task_manager.adapter.CategoriesListAdapter
import com.example.task_manager.adapter.OnTaskItemClickListener
import com.example.task_manager.adapter.TasksListAdapter
import com.example.task_manager.model.Task
import com.example.task_manager.util.GlobalKeys
import com.example.task_manager.viewModel.CompletedTasksViewModel
import com.example.task_manager.viewModel.CompletedTasksViewModelFactory
import com.example.task_manager.viewModel.HomeViewModel
import com.example.task_manager.viewModel.HomeViewModelFactory
import com.example.taskmanagerapp.R
import com.example.taskmanagerapp.databinding.ActivityCompletedTasksBinding
import com.example.taskmanagerapp.databinding.ActivityHomeBinding


class CompletedTasksActivity : AppCompatActivity(), OnTaskItemClickListener {

    private lateinit var binding: ActivityCompletedTasksBinding
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var taskAdapter: TasksListAdapter
    private val viewModel: CompletedTasksViewModel by viewModels {
        CompletedTasksViewModelFactory(this)
    }

    private val taskUpdatedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == HomeActivity.ACTION_TASK_UPDATED) {
                viewModel.getCompletedTasks()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.pop_up, R.anim.pop_down)
        binding = ActivityCompletedTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerBroadcastReceiver()
        initialize()
    }

    private fun initialize() {
        initializeRecyclerView()
        observeViewModel()
    }

    private fun registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(taskUpdatedReceiver, IntentFilter(HomeActivity.ACTION_TASK_UPDATED))
    }

    private fun observeViewModel() {
        viewModel.tasks.observe(this) { tasks ->
            taskAdapter.updateTasks(tasks as MutableList<Task>)
        }
    }

    private fun initializeRecyclerView() {
        recyclerViewTasks = binding.completedTasksRecyclerView
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TasksListAdapter(this, mutableListOf(), this)
        recyclerViewTasks.adapter = taskAdapter
    }

    override fun onItemClick(task: Task) {
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra(GlobalKeys.TASK_ID, task.taskId)
        startActivity(intent)
    }


}
