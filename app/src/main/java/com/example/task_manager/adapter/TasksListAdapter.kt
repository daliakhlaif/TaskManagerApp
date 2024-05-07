package com.example.task_manager.adapter

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.task_manager.model.Task
import com.example.task_manager.repository.TaskDatabaseHelper
import com.example.task_manager.util.GlobalMethods.formatDate
import com.example.task_manager.view.HomeActivity
import com.example.taskmanagerapp.databinding.TasksListItemBinding

class TasksListAdapter(
    private val context: Context,
    private var tasks: List<Task>,
    private val onTaskItemClickListener: OnTaskItemClickListener
) : RecyclerView.Adapter<TasksListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TasksListItemBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }
    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return tasks.size
    }

    inner class ViewHolder(private val binding: TasksListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val dbHelper = TaskDatabaseHelper(context)
        fun bind(task: Task) {
            val color = dbHelper.getCategoryById(task.categoryId)?.color
            binding.taskToDo.text = task.description
            binding.taskPriority.text = task.priority.name.toString()

            val border = GradientDrawable()
            border.setColor(Color.WHITE)
            if (color != null) {
                border.setStroke(4, color)
            }
            border.cornerRadius = 18F
            binding.taskPriority.background = border

            binding.dueDate.text = formatDate(task.dueDateTime)
            itemView.setOnClickListener {
                onTaskItemClickListener.onItemClick(task)
            }
            val checkBoxBorder = GradientDrawable()
            if (color != null) {
                checkBoxBorder.setColor(color)
                val colorStateList = ColorStateList.valueOf(color)
                binding.checkBox.buttonTintList = colorStateList
            }

            binding.checkBox.setOnClickListener {
                dbHelper.removeTask(task.taskId)
                tasks = tasks.filter { it != task }
                notifyItemRemoved(position)
                val intent = Intent(HomeActivity.ACTION_TASK_UPDATED)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }

        }
    }

}