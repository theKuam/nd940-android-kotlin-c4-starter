package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var dataSource: ReminderDataSource
    private lateinit var scheduler: TestCoroutineScheduler

    private val reminder1 = ReminderDataItem(
        title = "Reminder 1",
        description = "Description 1",
        location = "Location 1",
        longitude = 12.0,
        latitude = 50.0,
        id = "1"
    )

    private val reminder2 = ReminderDataItem(
        title = "",
        description = "Description 2",
        location = "Location 2",
        longitude = 45.5,
        latitude = 10.0,
        id = "2"
    )

    private val reminder3 = ReminderDataItem(
        title = "Reminder 3",
        description = "Description 3",
        location = "",
        longitude = 0.0,
        latitude = 0.0,
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
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @Test
    fun `onClear EXPECT nulls`() = runTest {
        saveReminderViewModel.apply {
            // Arrange
            validateAndSaveReminder(reminder1)
            validateAndSaveReminder(reminder2)
            validateAndSaveReminder(reminder3)

            // Act
            onClear()

            // Assert
            assertThat(reminderTitle.getOrAwaitValue(), `is`(nullValue()))
            assertThat(reminderDescription.getOrAwaitValue(), `is`(nullValue()))
            assertThat(reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
            assertThat(selectedPOI.getOrAwaitValue(), `is`(nullValue()))
            assertThat(latitude.getOrAwaitValue(), `is`(nullValue()))
            assertThat(longitude.getOrAwaitValue(), `is`(nullValue()))
        }
    }

    @Test
    fun `validateAndSaveReminder EXPECT saveToDataSource`() = runTest  {
        saveReminderViewModel.apply {
            // Arrange

            // Act
            validateAndSaveReminder(reminder1)
            val savedReminder = dataSource.getReminder("1") as Result.Success

            // Assert
            assertThat(savedReminder.data.id, `is`(reminder1.id))
            assertThat(savedReminder.data.title, `is`(reminder1.title))
            assertThat(savedReminder.data.description, `is`(reminder1.description))
            assertThat(savedReminder.data.latitude, `is`(reminder1.latitude))
            assertThat(savedReminder.data.longitude, `is`(reminder1.longitude))
        }
    }

    @Test
    fun `validateAndSaveReminder EXPECT showLoading`() = runTest  {
        saveReminderViewModel.apply {
            // Arrange
            Dispatchers.setMain(StandardTestDispatcher())
            // Act
            validateAndSaveReminder(reminder1)

            // Assert
            assertThat(showLoading.getOrAwaitValue(), `is`(true))

            // Resume
            advanceUntilIdle()

            // Assert
            assertThat(showLoading.getOrAwaitValue(), `is`(false))
        }
    }

    @Test
    fun `validateAndSaveReminder missingTitle EXPECT showSnackBarTitleError`() = runTest  {
        saveReminderViewModel.apply {
            // Arrange

            // Act
            validateAndSaveReminder(reminder2)

            // Assert
            assertThat(showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
        }
    }

    @Test
    fun `validateAndSaveReminder missingLocation EXPECT showSnackBarLocationError`() = runTest  {
        saveReminderViewModel.apply {
            // Arrange

            // Act
            validateAndSaveReminder(reminder3)

            // Assert
            assertThat(showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
        }
    }
}