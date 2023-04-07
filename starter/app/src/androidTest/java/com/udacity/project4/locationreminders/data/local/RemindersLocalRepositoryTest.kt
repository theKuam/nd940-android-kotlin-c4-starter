package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var reminderLocalRepository: RemindersLocalRepository
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

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        // testing with an in-memory database because it won't survive stopping the process
        reminderDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        reminderLocalRepository =
            RemindersLocalRepository(reminderDatabase.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDB() = reminderDatabase.close()

    @Test
    fun saveAllRemindersAndGetAll(): Unit = runBlocking {
        reminderLocalRepository.apply {
            // Arrange and Act
            saveReminder(reminder1)
            saveReminder(reminder2)
            saveReminder(reminder3)

            val reminders = getReminders() as Result.Success

            // Assert
            assertThat(reminders.data.size, `is`(3))
        }
    }

    @Test
    fun saveAReminderAndGetItById(): Unit = runBlocking {
        reminderLocalRepository.apply {
            // Arrange and Act
            saveReminder(reminder1)

            val reminder = getReminder(reminder1.id) as Result.Success

            // Assert
            reminder.apply {
                assertThat(data.title, `is`(reminder1.title))
                assertThat(data.description, `is`(reminder1.description))
                assertThat(data.location, `is`(reminder1.location))
                assertThat(data.latitude, `is`(reminder1.latitude))
                assertThat(data.longitude, `is`(reminder1.longitude))
                assertThat(data.id, `is`(reminder1.id))
            }
        }
    }

    @Test
    fun removeAllReminders(): Unit = runBlocking {
        reminderLocalRepository.apply {
            // Arrange and Act
            saveReminder(reminder1)
            saveReminder(reminder2)
            saveReminder(reminder3)

            reminderLocalRepository.deleteAllReminders()
            val reminders = getReminders() as Result.Success

            // Assert
            assertThat(reminders.data.size, `is`(0))
        }
    }
}