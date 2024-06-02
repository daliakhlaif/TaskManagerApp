package com.example.task_manager.view

import android.app.Activity
import android.app.ActivityOptions
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.task_manager.model.Category
import com.example.task_manager.model.Priority
import com.example.task_manager.model.Task
import com.example.task_manager.util.GlobalKeys.DATE_FORMAT
import com.example.task_manager.util.GlobalKeys.TASK_ID
import com.example.task_manager.util.GlobalMethods.formatDate
import com.example.task_manager.viewModel.TaskViewModel
import com.example.task_manager.viewModel.TaskViewModelFactory
import com.example.taskmanagerapp.R
import com.example.taskmanagerapp.databinding.ActivityTaskBinding
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.properties.Delegates


class TaskActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private lateinit var binding : ActivityTaskBinding
    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0
    private var savedHour = 0
    private var savedMinute = 0
    private val cal : Calendar = Calendar.getInstance()
    private lateinit var taskName : String
    private lateinit var taskDate : LocalDateTime
    private lateinit var taskPriority: Priority
    private var completed : Boolean = false
    private var taskCat by Delegates.notNull<Int>()
    private var isEditMode = false
    private var taskIdToUpdate: Int? = null
    private lateinit var viewModel: TaskViewModel
    private lateinit var selectedChip: Chip
    private lateinit var selectedCategoryChip: Chip
    private var taskId = 0
    private var isDateSelected = false


    companion object {

        const val EXTRA_TASK_ID = "extra_task_id"
        const val ACTION_CATEGORY_ADDED = "com.example.task_manager.ACTION_CATEGORY_UPDATED"
    }

    private val categoryUpdatedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_CATEGORY_ADDED) {
                viewModel.getAllCategories().lastOrNull()?.let { addCategoryChip(it) }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.pop_in, R.anim.pop_out)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        viewModel = TaskViewModelFactory(this).create(TaskViewModel::class.java)
        registerBroadcastReceiver()
        setContentView(binding.root)
        initialize()
    }

    private fun registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(categoryUpdatedReceiver, IntentFilter(ACTION_CATEGORY_ADDED))
    }

    private fun initialize(){
        setupPriorityChips()
        setupSaveButton()
        setupBackButton()
        displayCategories()
        pickDate()
        setupCheckBox()

        if (intent.hasExtra(TASK_ID)) {
            isEditMode = true
            taskIdToUpdate = intent.getIntExtra(TASK_ID, -1)
            taskId = taskIdToUpdate as Int
            fillFieldsForUpdate(taskIdToUpdate!!)
        }
    }

    private fun fillFieldsForUpdate(idToUpdate: Int) {
        viewModel.getTask(idToUpdate)?.let { task ->
            isDateSelected = true
            binding.saveButton.setText(R.string.update_task)
            binding.titleText.setText(R.string.edit_task)
            binding.infoText.setText(task.description)
            binding.button.text = formatDate(task.dueDateTime)
            binding.checkCompleted.isChecked = task.completed
            onPriorityChipClicked(getPriorityChipByValue(task.priority))
            viewModel.getCategory(task.categoryId)?.let { category ->
                getChipByCategoryName(category.name)?.let { onCategoryChipClicked(it) }
            }
        }
    }

    private fun getPriorityChipByValue(priority: Priority): Chip {
        return when (priority) {
            Priority.LOW -> binding.type1
            Priority.MEDIUM -> binding.type2
            Priority.HIGH -> binding.type3
        }
    }
    private fun getChipByCategoryName(categoryName: String): Chip? {
        for (i in 0 until binding.categoryGroup.childCount) {
            val chip = binding.categoryGroup.getChildAt(i) as Chip
            if (chip.text.toString() == categoryName) {
                return chip
            }
        }
        return null
    }

    private fun setupPriorityChips() {
        listOf(binding.type1, binding.type2, binding.type3).forEach { chip ->
            chip.setOnClickListener { onPriorityChipClicked(chip) }
        }
    }


    private fun onPriorityChipClicked(type: Chip) {
        taskPriority = when (type) {
            binding.type1 -> Priority.LOW
            binding.type2 -> Priority.MEDIUM
            binding.type3 -> Priority.HIGH
            else -> Priority.LOW
        }

        type.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.selected_chip_background))

        val chipGroup = binding.priorityGroup
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip !== type) {
                chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey3))
            }
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }


    private fun addCategoryChip(category: Category) {
        val chip = Chip(this)
        chip.text = category.name
        chip.isClickable = true
        chip.isCheckable = false
        chip.setTextColor(ContextCompat.getColor(this, R.color.black))
        chip.setOnClickListener {
            onCategoryChipClicked(chip)
            Toast.makeText(this, "Chosen category: ${category.name}", Toast.LENGTH_SHORT).show()
        }
        binding.categoryGroup.addView(chip)
    }

    private fun displayCategories() {
        viewModel.getAllCategories().forEach { addCategoryChip(it) }

        val addCatChip = binding.categoryGroup.findViewById<Chip>(R.id.addCat)
        addCatChip.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun onCategoryChipClicked(category: Chip) {
        category.setChipBackgroundColorResource(R.color.selected_chip_background)
        taskCat = viewModel.getCategoryByName(category.text.toString())?.categoryId ?: 0
        val chipGroup = binding.categoryGroup
        for (i in 0 until chipGroup.childCount) {
            val otherChip = chipGroup.getChildAt(i) as Chip
            if (otherChip !== category) {
                otherChip.setChipBackgroundColorResource(R.color.grey3)
            }
        }
        selectedCategoryChip = category
    }


    private fun setupCheckBox() {
        binding.checkCompleted.setOnCheckedChangeListener { _, isChecked ->
            completed = isChecked
        }
    }


    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            if (isInputValid()) {
                taskName = binding.infoText.text.toString()
                val dueDateTimeString = binding.button.text
                taskDate = LocalDateTime.parse(dueDateTimeString, DateTimeFormatter.ofPattern(DATE_FORMAT))
                val task = Task(taskId, taskName, taskDate, completed, taskPriority, taskCat)
                if (isEditMode) {
                    viewModel.updateTask(task)
                } else {
                    viewModel.addTask(task)
                }
                finish()
            } else {
                Toast.makeText(this, "Please enter all information", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showAddCategoryDialog() {
        val newCategoryFragment = NewCategoryFragment.newInstance()
        newCategoryFragment.show(supportFragmentManager, "NewCategoryFragment")
    }

    private fun pickDate() {
        binding.button.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.CustomDatePickerDialogTheme,
                this,
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH),
                cal.get(java.util.Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year
        TimePickerDialog(
            this,
            R.style.CustomTimePickerDialogTheme,
            this,
            cal.get(java.util.Calendar.HOUR_OF_DAY),
            cal.get(java.util.Calendar.MINUTE),
            false
        ).show()
    }


    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = minute

        cal.set(savedYear, savedMonth, savedDay, savedHour, savedMinute)

        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val formattedDate = dateFormat.format(cal.time)

        binding.button.text = formattedDate
        isDateSelected = true
    }

    private fun isInputValid(): Boolean {
        return  binding.infoText.text.isNotBlank() &&
                isDateSelected &&
                ::taskPriority.isInitialized &&
                ::selectedCategoryChip.isInitialized
    }

    override fun finish() {
        super.finish()
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.slide_in_left, R.anim.slide_out_right)
    }

}

