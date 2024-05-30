package com.example.task_manager.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.task_manager.model.Category
import com.example.task_manager.model.Priority
import com.example.task_manager.model.Task
import java.time.LocalDateTime

class TaskDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "task_manager.db"

        private const val TABLE_CATEGORY = "category"
        private const val TABLE_TASK = "task"

        private const val KEY_ID = "id"

        private const val KEY_CATEGORY_NAME = "name"
        private const val KEY_CATEGORY_COLOR = "color"

        private const val KEY_TASK_DESCRIPTION = "description"
        private const val KEY_TASK_DUE_DATETIME = "due_datetime"
        private const val KEY_TASK_COMPLETED = "completed"
        private const val KEY_TASK_PRIORITY = "priority"
        private const val KEY_TASK_CATEGORY_ID = "category_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createCategoryTable = ("CREATE TABLE $TABLE_CATEGORY ("
                + "$KEY_ID INTEGER PRIMARY KEY,"
                + "$KEY_CATEGORY_NAME TEXT,"
                + "$KEY_CATEGORY_COLOR INTEGER)")

        val createTaskTable = ("CREATE TABLE $TABLE_TASK ("
                + "$KEY_ID INTEGER PRIMARY KEY,"
                + "$KEY_TASK_DESCRIPTION TEXT,"
                + "$KEY_TASK_DUE_DATETIME TEXT,"
                + "$KEY_TASK_COMPLETED INTEGER,"
                + "$KEY_TASK_PRIORITY TEXT,"
                + "$KEY_TASK_CATEGORY_ID INTEGER,"
                + "FOREIGN KEY($KEY_TASK_CATEGORY_ID) REFERENCES $TABLE_CATEGORY($KEY_ID))")

        db.execSQL(createCategoryTable)
        db.execSQL(createTaskTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASK")
        onCreate(db)
    }

    fun addCategory(category: Category): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_CATEGORY_NAME, category.name)
            put(KEY_CATEGORY_COLOR, category.color)
        }
        val categoryId = db.insert(TABLE_CATEGORY, null, values)
        db.close()
        return categoryId
    }

    fun addTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_TASK_DESCRIPTION, task.description)
            put(KEY_TASK_DUE_DATETIME, task.dueDateTime.toString())
            put(KEY_TASK_COMPLETED, if (task.completed) 1 else 0)
            put(KEY_TASK_PRIORITY, task.priority.name)
            put(KEY_TASK_CATEGORY_ID, task.categoryId)
        }
        val taskId = db.insert(TABLE_TASK, null, values)
        db.close()

        return taskId
    }

    private fun getTasksByQuery(query: String, vararg args: String): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(query, args)
            cursor?.use {
                while (it.moveToNext()) {
                    val taskIdIndex = it.getColumnIndex(KEY_ID)
                    val descriptionIndex = it.getColumnIndex(KEY_TASK_DESCRIPTION)
                    val dueDateTimeIndex = it.getColumnIndex(KEY_TASK_DUE_DATETIME)
                    val completedIndex = it.getColumnIndex(KEY_TASK_COMPLETED)
                    val priorityIndex = it.getColumnIndex(KEY_TASK_PRIORITY)
                    val categoryIdIndex = it.getColumnIndex(KEY_TASK_CATEGORY_ID)

                    if (taskIdIndex >= 0 && descriptionIndex >= 0 && dueDateTimeIndex >= 0 &&
                        completedIndex >= 0 && priorityIndex >= 0 && categoryIdIndex >= 0
                    ) {

                        val taskId = it.getInt(taskIdIndex)
                        val description = it.getString(descriptionIndex)
                        val dueDateTimeString = it.getString(dueDateTimeIndex)
                        val completed = it.getInt(completedIndex) == 1
                        val priorityString = it.getString(priorityIndex)
                        val categoryId = it.getInt(categoryIdIndex)

                        val dueDateTime = LocalDateTime.parse(dueDateTimeString)
                        val priority = Priority.valueOf(priorityString)

                        val task = Task(taskId, description, dueDateTime, completed, priority, categoryId)
                        tasks.add(task)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }

        return tasks
    }

    fun getAllTasksForCategory(categoryId: Int): List<Task> {
        val selectQuery = "SELECT * FROM $TABLE_TASK WHERE $KEY_TASK_CATEGORY_ID = ?"
        return getTasksByQuery(selectQuery, categoryId.toString())
    }
    fun getAllUpcomingTasks(): List<Task> {
        val currentDateTime = LocalDateTime.now()
        val selectQuery =
            "SELECT * FROM $TABLE_TASK WHERE $KEY_TASK_DUE_DATETIME > ? AND $KEY_TASK_COMPLETED = 0 ORDER BY $KEY_TASK_DUE_DATETIME"
        return getTasksByQuery(selectQuery, currentDateTime.toString())
            .filter { it.dueDateTime.isAfter(currentDateTime) }
            .sortedBy { it.dueDateTime }
    }

    fun getCategoryById(categoryId: Int): Category? {
        var category: Category? = null
        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_CATEGORY WHERE $KEY_ID = ?"
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, arrayOf(categoryId.toString()))
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(KEY_CATEGORY_NAME)
                    val colorIndex = it.getColumnIndex(KEY_CATEGORY_COLOR)

                    if (nameIndex >= 0 && colorIndex >= 0) {
                        val name = it.getString(nameIndex)
                        val color = it.getInt(colorIndex)
                        category = Category(categoryId, name, color)
                    }
                }
            }
        } catch (e: Exception) {

            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return category
    }

    fun getTaskCountForCategory(categoryId: Int): Int {
        val db = readableDatabase
        var count = 0
        val selectQuery = "SELECT COUNT(*) FROM $TABLE_TASK WHERE $KEY_TASK_CATEGORY_ID = ?"
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, arrayOf(categoryId.toString()))
            cursor?.use {
                if (it.moveToFirst()) {
                    count = it.getInt(0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }

        return count
    }

    fun getAllCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        val selectQuery = "SELECT * FROM $TABLE_CATEGORY"
        val db = readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
            cursor?.use {
                while (it.moveToNext()) {
                    val categoryIdIndex = it.getColumnIndex(KEY_ID)
                    val nameIndex = it.getColumnIndex(KEY_CATEGORY_NAME)
                    val colorIndex = it.getColumnIndex(KEY_CATEGORY_COLOR)

                    if (categoryIdIndex >= 0 && nameIndex >= 0 && colorIndex >= 0) {
                        val categoryId = it.getInt(categoryIdIndex)
                        val name = it.getString(nameIndex)
                        val color = it.getInt(colorIndex)
                        val category = Category(categoryId, name, color)
                        categories.add(category)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }

        return categories
    }

    fun getTaskById(taskId: Int): Task? {
        var task: Task? = null
        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_TASK WHERE $KEY_ID = ?"
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, arrayOf(taskId.toString()))
            cursor?.use {
                if (it.moveToFirst()) {
                    val descriptionIndex = it.getColumnIndex(KEY_TASK_DESCRIPTION)
                    val dueDateTimeIndex = it.getColumnIndex(KEY_TASK_DUE_DATETIME)
                    val completedIndex = it.getColumnIndex(KEY_TASK_COMPLETED)
                    val priorityIndex = it.getColumnIndex(KEY_TASK_PRIORITY)
                    val categoryIdIndex = it.getColumnIndex(KEY_TASK_CATEGORY_ID)

                    if (descriptionIndex >= 0 && dueDateTimeIndex >= 0 &&
                        completedIndex >= 0 && priorityIndex >= 0 && categoryIdIndex >= 0
                    ) {

                        val description = it.getString(descriptionIndex)
                        val dueDateTimeString = it.getString(dueDateTimeIndex)
                        val completed = it.getInt(completedIndex) == 1
                        val priorityString = it.getString(priorityIndex)
                        val categoryId = it.getInt(categoryIdIndex)

                        val dueDateTime = LocalDateTime.parse(dueDateTimeString)
                        val priority = Priority.valueOf(priorityString)

                        task = Task(taskId.toInt(), description, dueDateTime, completed, priority, categoryId)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return task
    }

    fun updateTask(task: Task): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_TASK_DESCRIPTION, task.description)
            put(KEY_TASK_DUE_DATETIME, task.dueDateTime.toString())
            put(KEY_TASK_COMPLETED, if (task.completed) 1 else 0)
            put(KEY_TASK_PRIORITY, task.priority.name)
            put(KEY_TASK_CATEGORY_ID, task.categoryId)
        }
        val rowsAffected = db.update(
            TABLE_TASK,
            values,
            "$KEY_ID = ?",
            arrayOf(task.taskId.toString())
        )
        db.close()
        return rowsAffected
    }

    fun getCategoryByName(name: String): Category? {
        var category: Category? = null
        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_CATEGORY WHERE $KEY_CATEGORY_NAME = ?"
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, arrayOf(name))
            cursor?.use {
                if (it.moveToFirst()) {
                    val categoryIdIndex = it.getColumnIndex(KEY_ID)
                    val colorIndex = it.getColumnIndex(KEY_CATEGORY_COLOR)

                    if (categoryIdIndex >= 0 && colorIndex >= 0) {
                        val categoryId = it.getInt(categoryIdIndex)
                        val color = it.getInt(colorIndex)
                        category = Category(categoryId, name, color)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return category
    }
    fun removeTask(taskId: Int): Int {
        val db = writableDatabase
        val rowsAffected = db.delete(
            TABLE_TASK,
            "$KEY_ID = ?",
            arrayOf(taskId.toString())
        )
        db.close()
        return rowsAffected
    }

    fun getCompletedTasks(): List<Task> {
        val selectQuery = "SELECT * FROM $TABLE_TASK WHERE $KEY_TASK_COMPLETED = 1"
        return getTasksByQuery(selectQuery)
    }
}
