package com.example.myapplication.ui.custom

import android.app.DatePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.example.myapplication.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

class DateInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val edtDate: TextInputEditText
    private val tilDate: TextInputLayout

    init {
        orientation = VERTICAL
        inflate(context, R.layout.view_date_input, this)

        tilDate = findViewById(R.id.tilDate)
        edtDate = findViewById(R.id.edtDate)

        // Read Custom Attribute
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.DateInputView)
            tilDate.hint = ta.getString(R.styleable.DateInputView_hint)
            ta.recycle()
        }

        setupDatePicker()
    }

    private fun setupDatePicker() {
        edtDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            DatePickerDialog(
                context,
                { _, y, m, d ->
                    edtDate.setText("%02d/%02d/%04d".format(d, m + 1, y))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    fun setHint(text: String) {
        tilDate.hint = text
    }

    fun getDate(): String = edtDate.text.toString()
}

