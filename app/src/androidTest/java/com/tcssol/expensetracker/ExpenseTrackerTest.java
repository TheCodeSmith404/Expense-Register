package com.tcssol.expensetracker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Espresso UI tests for ExpenseTracker v2.0 core flows.
 *
 * Run with:  ./gradlew connectedAndroidTest
 *
 * Requires: emulator or physical device API 29+
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExpenseTrackerTest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(MainActivity.class);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. App launch – MainActivity and BottomNavigation are visible
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void appLaunchesAndShowsMainActivity() {
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Tap FAB → CreateExpenses opens
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void fabOpensCreateExpenses() {
        onView(withId(R.id.fab)).perform(click());
        // Save button should now be visible
        onView(withId(R.id.button)).check(matches(isDisplayed()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Add a Spend entry → CreateExpenses saves and returns
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void addSpendEntryAndReturn() {
        onView(withId(R.id.fab)).perform(click());

        // Select "Spend" radio (default) and enter an amount
        onView(withId(R.id.radioButtonSpend)).perform(click());
        onView(withId(R.id.edit_text_number)).perform(replaceText("250"), closeSoftKeyboard());

        // Save
        onView(withId(R.id.button)).perform(click());

        // Back in MainActivity – bottom nav should be visible
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Add an Earned entry → returns to MainActivity
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void addEarnedEntryAndReturn() {
        onView(withId(R.id.fab)).perform(click());

        onView(withId(R.id.radioButtonEarned)).perform(click());
        onView(withId(R.id.edit_text_number)).perform(replaceText("1000"), closeSoftKeyboard());

        onView(withId(R.id.button)).perform(click());

        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Navigate to All Entries tab and verify search bar is visible
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void allEntriesTabShowsSearchBar() {
        onView(withId(R.id.nav_all)).perform(click());
        onView(withId(R.id.searchInputLayout)).check(matches(isDisplayed()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Navigate to Summary tab
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void summaryTabIsAccessible() {
        onView(withId(R.id.nav_summary)).perform(click());
        // Summary fragment should load without crash
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. Navigate to P2P tab
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void p2pTabIsAccessible() {
        onView(withId(R.id.nav_p2p)).perform(click());
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. Filter chips are displayed and clickable
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void filterChipsArePresent() {
        onView(withId(R.id.chipAll)).check(matches(isDisplayed()));
        onView(withId(R.id.chipThisMonth)).check(matches(isDisplayed()));
        onView(withId(R.id.chipPreviousMonth)).check(matches(isDisplayed()));

        onView(withId(R.id.chipThisMonth)).perform(click());
        // After clicking "This Month", chip should still be displayed
        onView(withId(R.id.chipThisMonth)).check(matches(isDisplayed()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. Month / Year ExposedDropdownMenu is displayed
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void monthYearDropdownsArePresent() {
        onView(withId(R.id.spinnerSelectMonth)).check(matches(isDisplayed()));
        onView(withId(R.id.spinnerSelectYear)).check(matches(isDisplayed()));
    }
}
