package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {


    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource
    private lateinit var scheduler: TestCoroutineScheduler

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
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel() {
        stopKoin()
        scheduler = TestCoroutineScheduler()
        dataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @Test
    fun `loadsReminders success EXPECT ThreeRemindersReturned`() = runTest {
        // Arrange
        dataSource.apply {
            deleteAllReminders()
            saveReminder(reminder1)
            saveReminder(reminder2)
            saveReminder(reminder3)
        }

        remindersListViewModel.apply {
            // Act
            loadReminders()

            // Assert
            assertThat(remindersList.getOrAwaitValue().size, `is`(3))
            assertThat(showNoData.getOrAwaitValue(), `is`(false))
        }
    }

    @Test
    fun `loadsReminders noData EXPECT showNoDataReturnTrue`() = runTest {
        // Arrange
        dataSource.apply {
            deleteAllReminders()
        }

        remindersListViewModel.apply {
            // Act
            loadReminders()

            // Assert
            assertThat(showNoData.getOrAwaitValue(), `is`(true))
        }
    }

    @Test
    fun `loadsReminders failed EXPECT returnError`() = runTest {
        // Arrange
        dataSource.apply {
            shouldReturnError = true
        }

        remindersListViewModel.apply {
            // Act
            loadReminders()

            // Assert
            assertThat(showSnackBar.getOrAwaitValue(), `is`(FakeDataSource.REMINDERS_NOT_FOUND_ERROR_MSG))
        }
    }

    @Test
    fun `loadsReminders success EXPECT showLoading`() = runTest {
        // Arrange
        Dispatchers.setMain(StandardTestDispatcher())
        dataSource.apply {
            deleteAllReminders()
            saveReminder(reminder1)
            saveReminder(reminder2)
            saveReminder(reminder3)
        }

        remindersListViewModel.apply {
            // Act
            loadReminders()

            // Assert
            assertThat(showLoading.getOrAwaitValue(), `is`(true))

            // Resume
            advanceUntilIdle()

            // Assert
            assertThat(showLoading.getOrAwaitValue(), `is`(false))
        }
    }
}