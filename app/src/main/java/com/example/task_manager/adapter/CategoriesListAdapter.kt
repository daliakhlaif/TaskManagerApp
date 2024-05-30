package com.example.task_manager.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.task_manager.model.Category
import com.example.task_manager.repository.TaskDatabaseHelper
import com.example.taskmanagerapp.R
import com.example.taskmanagerapp.databinding.CategoriesListItemBinding

class CategoriesListAdapter(
    private val context: Context,
    private var categories: List<Category>,
    private val onCategoryItemClickListener: OnCategoryItemClickListener
) : RecyclerView.Adapter<CategoriesListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoriesListItemBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int {
        return categories.size
    }


    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
    inner class ViewHolder(private val binding: CategoriesListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val dbHelper = TaskDatabaseHelper (context)

        private val categoryName = binding.category
        fun bind(category: Category) {
            categoryName.text = category.name
            binding.color.setBackgroundColor(category.color)
            val count = dbHelper.getTaskCountForCategory(category.categoryId)

            binding.tasks.text = if (count > 1) {
                context.getString(R.string.tasks, count.toString())
            } else {
                context.getString(R.string.string_task, count.toString())
            }
            itemView.setOnClickListener {
                onCategoryItemClickListener.onItemClick(category)
            }
        }
    }
}

