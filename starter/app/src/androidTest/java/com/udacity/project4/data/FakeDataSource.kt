package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.lang.Exception

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    private val reminders : MutableList<ReminderDTO>? = mutableListOf()
) : ReminderDataSource {

    companion object {
        const val REMINDER_NOT_FOUND_ERROR_MSG = "Reminder not found"
        const val REMINDERS_NOT_FOUND_ERROR_MSG = "Reminders not found"
    }

    var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) return Result.Error(REMINDERS_NOT_FOUND_ERROR_MSG)
        reminders?.let {
            return Result.Success(it)
        }
        return Result.Error(REMINDERS_NOT_FOUND_ERROR_MSG)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders?.let {
            val result = it.find { reminder -> reminder.id == id }
            result?.let { reminder ->
                return Result.Success(reminder)
            }
            return Result.Error(REMINDER_NOT_FOUND_ERROR_MSG)
        }
        return Result.Error(REMINDERS_NOT_FOUND_ERROR_MSG)
    }

    override suspend fun deleteAllReminders() {
        reminders?.let {
            reminders.clear()
        }
    }


}