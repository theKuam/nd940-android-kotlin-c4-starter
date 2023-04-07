package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.common.AuthenticationViewModel
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

    private val dataSource: ReminderDataSource by inject()
    private lateinit var appContext: Application

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
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get()
                )
            }
            single {
                AuthenticationViewModel(
                    appContext
                )
            }
            single { FakeDataSource() as ReminderDataSource }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
    }

    @After
    fun cleanupDb() = runTest { dataSource.deleteAllReminders() }

    @Test
    fun saveRemindersAndDisplayInUI() = runTest {
        // Arrange and Act
        dataSource.saveReminder(reminder1)
        dataSource.saveReminder(reminder2)
        dataSource.saveReminder(reminder3)

        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // Assert
        onView(withText(reminder1.title)).check(matches(isDisplayed()))
        onView(withText(reminder1.title)).check(matches(isDisplayed()))
        onView(withText(reminder1.title)).check(matches(isDisplayed()))

        onView(withText(reminder3.description)).check(matches(isDisplayed()))
        onView(withText(reminder3.description)).check(matches(isDisplayed()))
        onView(withText(reminder3.description)).check(matches(isDisplayed()))

        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
    }

    @Test
    fun deleteRemindersAndDisplayNoDataInUi() = runTest{
        // Arrange and Act
        dataSource.deleteAllReminders()
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        // Assert
        onView(withText(R.string.no_data)).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        onView(withText(reminder1.title)).check(doesNotExist())
        onView(withText(reminder2.title)).check(doesNotExist())
        onView(withText(reminder3.title)).check(doesNotExist())
    }
    // Here, we're trying clickFab, and the destination we want to reach is the Reminder Fragment.
    @Test
    fun clickAddReminderAndNavigateToSaveReminder() = runTest {
        // Arrange and Act
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Assert
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
}