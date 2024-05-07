package com.example.task_manager.view

import androidx.fragment.app.activityViewModels
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.task_manager.viewModel.TaskViewModelFactory
import com.example.task_manager.viewModel.TaskViewModel
import com.example.taskmanagerapp.R
import com.example.taskmanagerapp.databinding.FragmentNewCategoryBinding

class NewCategoryFragment : DialogFragment() {
    private var _binding: FragmentNewCategoryBinding? = null
    private val binding get() = _binding!!
    private var selectedColor = Color.BLACK
    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         setupColorButton()
         setupAddButton()
    }

    private fun setupAddButton() {
        binding.addButton.setOnClickListener {
            addCategory()
        }
    }

    private fun setupColorButton() {
        binding.chooseColorButton.setOnClickListener {
            showColorPickerDialog()
        }
    }

    private fun addCategory() {
        val categoryName = binding.catNameText.text.toString().trim()
        if (categoryName.isEmpty()) {
            binding.catNameText.error = R.string.categoryNameError.toString()
            return
        }
        viewModel.addCategory(categoryName, selectedColor)
        dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomDialogTheme)
    }


    private fun showColorPickerDialog() {
        val colorPickerView = layoutInflater.inflate(R.layout.color_picker_dialog, null)
        val colorSeekBar = colorPickerView.findViewById<SeekBar>(R.id.colorSeekBar)
        val colorPreview = colorPickerView.findViewById<View>(R.id.colorPreview)

        colorSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val hsv = floatArrayOf(progress.toFloat(), 1f, 1f)
                selectedColor = Color.HSVToColor(hsv)
                colorPreview.setBackgroundColor(selectedColor)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.pick_color)
            .setView(colorPickerView)
            .setPositiveButton(R.string.OK) { _, _ ->
                binding.colorView.setBackgroundColor(selectedColor)
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): NewCategoryFragment {
            return NewCategoryFragment()
        }
    }
}
