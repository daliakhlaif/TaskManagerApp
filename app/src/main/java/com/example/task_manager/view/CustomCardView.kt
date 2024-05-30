package com.example.task_manager.view

import com.example.taskmanagerapp.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView

class CustomCardView : CardView {

    lateinit var checkBox: CheckBox
    lateinit var taskToDo: TextView
    lateinit var dueDate: TextView
    lateinit var taskPriority: TextView

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.view_custom_card, this, true)
        checkBox = findViewById(R.id.checkBox)
        taskToDo = findViewById(R.id.taskToDo)
        dueDate = findViewById(R.id.dueDate)
        taskPriority = findViewById(R.id.taskPriority)
    }
}
