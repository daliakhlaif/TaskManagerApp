package com.example.task_manager.adapter

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.task_manager.model.Task
import com.example.task_manager.repository.TaskDatabaseHelper
import com.example.task_manager.util.GlobalMethods.formatDate
import com.example.task_manager.view.CustomCardView
import com.example.task_manager.view.HomeActivity
import com.example.taskmanagerapp.R

class TasksListAdapter(
    private val context: Context,
    private var tasks: List<Task>,
    private val onTaskItemClickListener: OnTaskItemClickListener
) : RecyclerView.Adapter<TasksListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.tasks_list_item, parent, false)
        return ViewHolder(view as CustomCardView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    inner class ViewHolder(private val customCardView: CustomCardView) :
        RecyclerView.ViewHolder(customCardView) {
        private val dbHelper = TaskDatabaseHelper(context)

        fun bind(task: Task) {
            val color = dbHelper.getCategoryById(task.categoryId)?.color
            customCardView.taskToDo.text = task.description
            customCardView.taskPriority.text = task.priority.name.toString()

            val border = GradientDrawable()
            border.setColor(Color.WHITE)
            if (color != null) {
                border.setStroke(4, color)
            }
            border.cornerRadius = 18F
            customCardView.taskPriority.background = border

            customCardView.dueDate.text = formatDate(task.dueDateTime)
            itemView.setOnClickListener {
                onTaskItemClickListener.onItemClick(task)
            }

            val checkBoxBorder = GradientDrawable()
            if (color != null) {
                checkBoxBorder.setColor(color)
                val colorStateList = ColorStateList.valueOf(color)
                customCardView.checkBox.buttonTintList = colorStateList
            }

            customCardView.checkBox.isChecked = task.completed

            customCardView.checkBox.setOnClickListener {
                Handler(Looper.getMainLooper()).post {
                    task.completed = !task.completed
                     dbHelper.updateTask(task)
                        removeTask(adapterPosition)
                    notifyDataSetChanged()
                }
            }
        }

        private fun removeTask(position: Int) {
            (tasks as MutableList<Task>).removeAt(position)
            notifyItemRemoved(position)
            val intent = Intent(HomeActivity.ACTION_TASK_UPDATED)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}
