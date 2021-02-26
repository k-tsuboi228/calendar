package jp.co.abs.calender

import android.text.InputFilter
import android.widget.EditText
import androidx.databinding.BindingAdapter

@BindingAdapter("android:text_filter")
fun EditText.setTextFilter(regex: String) {
    val inputFilter = InputFilter { source, start, end, dest, dStart, dEnd ->
        val string = dest.toString() + source.toString()
        if (string.matches(Regex(regex))) {
            source
        } else {
            ""
        }
    }
    filters = arrayOf(inputFilter)
}
