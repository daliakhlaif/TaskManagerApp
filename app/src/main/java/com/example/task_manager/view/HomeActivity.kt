package com.example.task_manager.view


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.task_manager.adapter.CategoriesListAdapter
import com.example.task_manager.adapter.OnCategoryItemClickListener
import com.example.task_manager.adapter.OnTaskItemClickListener
import com.example.task_manager.adapter.TasksListAdapter
import com.example.task_manager.model.Category
import com.example.task_manager.model.Task
import com.example.task_manager.util.GlobalKeys.CATEGORY_ID
import com.example.task_manager.util.GlobalKeys.TASK_ID
import com.example.task_manager.viewModel.HomeViewModel
import com.example.task_manager.viewModel.HomeViewModelFactory
import com.example.taskmanagerapp.R
import com.example.taskmanagerapp.databinding.ActivityHomeBinding
import com.google.android.material.navigation.NavigationView


class HomeActivity : AppCompatActivity(), OnCategoryItemClickListener, OnTaskItemClickListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var categoryAdapter: CategoriesListAdapter
    private lateinit var taskAdapter: TasksListAdapter
    private lateinit var toggle : ActionBarDrawerToggle
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
        setContentView(binding.root)
        recyclerView = binding.CategoriesRecycler
        registerBroadcastReceiver()
        initialize()
    }




    private fun initialize(){
        setupNavigationDrawer()
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

    private fun setupNavigationDrawer(){
        val drawerLayout : DrawerLayout = binding.drawerLayout
        val navView : NavigationView = binding.navView

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(applicationContext, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    true
                }
                R.id.nav_completed_tasks -> {
                    val intent = Intent(applicationContext, CompletedTasksActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
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

