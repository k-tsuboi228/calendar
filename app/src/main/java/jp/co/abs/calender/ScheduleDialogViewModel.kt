package jp.co.abs.calender

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScheduleDialogViewModel : ViewModel() {
    val hourText = MutableLiveData("")
    val minuteText = MutableLiveData("")
    val scheduleText = MutableLiveData("")
}