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
import com.example.task_manager.adapter.OnTaskItemClickListener
import com.example.task_manager.adapter.TasksListAdapter
import com.example.task_manager.model.Task
import com.example.task_manager.util.GlobalKeys
import com.example.task_manager.util.GlobalKeys.CATEGORY_ID
import com.example.task_manager.viewModel.CategoryTasksViewModel
import com.example.task_manager.viewModel.CategoryTasksViewModelFactory
import com.example.taskmanagerapp.databinding.ActivityCategoryTasksBinding


class CategoryTasksActivity : AppCompatActivity(), OnTaskItemClickListener {

    private lateinit var binding: ActivityCategoryTasksBinding
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TasksListAdapter
    private var categoryId : Int = 0
    private val viewModel: CategoryTasksViewModel by viewModels {
        CategoryTasksViewModelFactory(this, categoryId)
    }

    private val taskUpdatedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == HomeActivity.ACTION_TASK_UPDATED) {
                viewModel.getAllTasksForCategory()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryTasksBinding.inflate(layoutInflater)
        categoryId = intent.getIntExtra(CATEGORY_ID, -1)
        setContentView(binding.root)
        registerBroadcastReceiver()
        initialize()
    }

    private fun initialize() {
        setupBackButton()
        initializeRecyclerView()
        observeViewModel()
        initializeName(categoryId)
    }

    private fun registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(taskUpdatedReceiver, IntentFilter(HomeActivity.ACTION_TASK_UPDATED))
    }
    
    private fun initializeName(id :Int){
       binding.categoryName.text = viewModel.getCategory(id)?.name ?: " "
    }

    private fun observeViewModel(){
        viewModel.tasks.observe(this) { tasks ->
            taskAdapter.updateTasks(tasks)
        }
    }
    private fun initializeRecyclerView() {
        recyclerViewTasks = binding.recycler
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TasksListAdapter(this, emptyList(), this)
        recyclerViewTasks.adapter = taskAdapter
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    override fun onItemClick(task: Task) {
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra(GlobalKeys.TASK_ID, task.taskId)
        startActivity(intent)
    }
}
