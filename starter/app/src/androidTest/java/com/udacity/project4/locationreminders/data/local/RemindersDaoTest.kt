package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before
import org.junit.runner.RunWith

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var reminderDatabase: RemindersDatabase

    private val reminder1 = ReminderDTO(
        title = "Reminder 1",
        description = "Description 1",
        location = "Location 1",
        longitude = 12.0,
        latitude = 50.0,
        id = "1"
    )

    private val reminder2 = ReminderDTO(
        title = "Reminder 2",
        description = "Description 2",
        location = "Location 2",
        longitude = 45.5,
        latitude = 10.0,
        id = "2"
    )

    private val reminder3 = ReminderDTO(
        title = "Reminder 3",
        description = "Description 3",
        location = "Location 3",
        longitude = 11.3,
        latitude = 24.0,
        id = "3"
    )

    @Before
    fun setupDB() {
        reminderDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDB() = reminderDatabase.close()

    @Test
    fun saveAllRemindersAndGetAll() = runTest {
        reminderDatabase.reminderDao().apply {
            // Arrange and Act
            saveReminder(reminder1)
            saveReminder(reminder2)
            saveReminder(reminder3)

            val reminders = getReminders()

            // Assert
            assertThat(reminders.size, `is`(3))
        }
    }

    @Test
    fun saveAReminderAndGetIt() = runTest {
        reminderDatabase.reminderDao().apply {
            // Arrange and Act
            saveReminder(reminder1)

            val reminder = getReminderById(reminder1.id)

            // Assert
            assertThat(reminder, `is`(notNullValue()))
            assertThat(reminder?.title, `is`(reminder1.title))
            assertThat(reminder?.description, `is`(reminder1.description))
            assertThat(reminder?.location, `is`(reminder1.location))
            assertThat(reminder?.longitude, `is`(reminder1.longitude))
            assertThat(reminder?.latitude, `is`(reminder1.latitude))
        }
    }

    @Test
    fun removeAllReminders() = runTest {
        reminderDatabase.reminderDao().apply {
            // Arrange and Act
            saveReminder(reminder1)
            saveReminder(reminder2)
            saveReminder(reminder3)

            deleteAllReminders()

            val reminders = getReminders()

            // Assert
            assertThat(reminders.size, `is`(0))
        }
    }
}