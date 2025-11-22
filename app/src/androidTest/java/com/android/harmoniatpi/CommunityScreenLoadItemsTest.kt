package com.android.harmoniatpi

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.harmoniatpi.utils.grantPermissions
import com.android.harmoniatpi.utils.setOrientationScreenPortrait
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CommunityScreenLoadItemsTest {

    val timeMaxToLogin = 15000L

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        grantPermissions()
        loginUntilCommunityScreen()
    }

    private fun loginUntilCommunityScreen() {
        setOrientationScreenPortrait(composeTestRule)

        composeTestRule.onNodeWithTag("COMENZAR_BUTTON", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag("LOGIN_SCREEN", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithTag("USERNAME_INPUT", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("USERNAME_INPUT", useUnmergedTree = true)
            .performTextInput("testui@gmail.com")

        composeTestRule.onNodeWithTag("PASSWORD_INPUT", useUnmergedTree = true)
            .performTextInput("123456")

        composeTestRule.onNodeWithTag("LOGIN_BUTTON", useUnmergedTree = true)
            .performClick()

        composeTestRule.waitUntil(timeMaxToLogin) {
            composeTestRule.onAllNodesWithTag("CommunityScreen", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("CommunityScreen", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun communityLoadItemsCorrectly() {

        composeTestRule.onNodeWithTag("COMMUNITY_LIST").assertIsDisplayed()

        composeTestRule.waitUntil(timeMaxToLogin) {
            composeTestRule.onAllNodesWithTag("POST_ITEM_0", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("POST_ITEM_0").assertIsDisplayed()

        composeTestRule.onNodeWithTag("POST_ITEM_1").assertIsDisplayed()

        composeTestRule.onNodeWithTag("POST_ITEM_2").assertIsDisplayed()
    }

}