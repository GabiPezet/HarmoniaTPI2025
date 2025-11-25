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
class ShowProjectScreenTest {

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

        composeTestRule.waitUntil(3000) {
            composeTestRule.onAllNodesWithTag("PREVIEW_SCREEN", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("PREVIEW_SCREEN", useUnmergedTree = true)
            .assertIsDisplayed()

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
    fun showProjectScreenTagTest() {

        composeTestRule.onNodeWithTag("COMMUNITY_LIST").assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("BOTTOM_TAB_RehearsalRoomRoute", useUnmergedTree = true)
            .performClick()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("ProjectsScreen", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("ProjectsScreen", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("BOTTOM_TAB_CommunityScreenRoute", useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag("CommunityScreen", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("BOTTOM_TAB_RehearsalRoomRoute", useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag("ProjectsScreen", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("BOTTOM_TAB_CommunityScreenRoute", useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag("CommunityScreen", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("BOTTOM_TAB_RehearsalRoomRoute", useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag("ProjectsScreen", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("BOTTOM_TAB_CommunityScreenRoute", useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag("CommunityScreen", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("BOTTOM_TAB_RehearsalRoomRoute", useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag("ProjectsScreen", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("EmptyListMessage", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("AddProjectButton", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("AddProjectButton", useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag("CreateProjectDialog", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("ProjectTitleInput")
            .performTextInput("Mi nuevo proyecto")

        composeTestRule
            .onNodeWithTag("ProjectDescriptionInput")
            .performTextInput("Descripción de prueba")

        composeTestRule
            .onNodeWithTag("ProjectHashtagsInput")
            .performTextInput("#testing")

        // 4. Confirmar creación
        composeTestRule
            .onNodeWithTag("CreateProjectConfirmButton")
            .performClick()

        // 5. Verificar que el diálogo desaparece
        composeTestRule
            .onNodeWithTag("CreateProjectDialog")
            .assertDoesNotExist()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("ProjectManagementScreen", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("ProjectManagementScreen", useUnmergedTree = true)
            .assertIsDisplayed()

        // 👉 Esperar 5 segundos
        Thread.sleep(5000)

        // 👉 Usar la tecla BACK nativa del sistema
        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        // Esperar a que vuelva
        composeTestRule.waitUntil(3000) {
            composeTestRule.onAllNodesWithTag("ProjectsScreen", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("ProjectsScreen")
            .assertIsDisplayed()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("POST_ITEM_0", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("POST_ITEM_0", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("MoreOptions", useUnmergedTree = true)
            .performClick()

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("POST_ITEM_0", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("MoreOptionsMenuDropdown", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(3000)

        composeTestRule
            .onNodeWithTag("DeleteMenuItem", useUnmergedTree = true)
            .performClick()

        Thread.sleep(5000)

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("DeleteProjectDialog", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("DeleteProjectDialog", useUnmergedTree = true)
            .assertIsDisplayed()

                composeTestRule
                    .onNodeWithTag("CONFIRM_BUTTON", useUnmergedTree = true)
                    .performClick()

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("EmptyListMessage", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("EmptyListMessage", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(5000)

        composeTestRule
            .onNodeWithTag("BOTTOM_TAB_CommunityScreenRoute", useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag("CommunityScreen", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("MENU_BUTTON", useUnmergedTree = true)
            .performClick()

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("CONTENT_MAIN_MENU", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("CONTENT_MAIN_MENU", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(5000)

        composeTestRule
            .onNodeWithTag("MenuOptionItemProfile", useUnmergedTree = true)
            .performClick()

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("UserDetailProfile", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("UserDetailProfile", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(5000)

        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        /* vuelve a la pantalla principal */

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("CONTENT_MAIN_MENU", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("CONTENT_MAIN_MENU", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(5000)

        composeTestRule
            .onNodeWithTag("MenuOptionItemNotifications", useUnmergedTree = true)
            .performClick()

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("NOTIFICATION_SCREEN", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("NOTIFICATION_SCREEN", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(5000)

        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        /* vuelve a la pantalla principal */

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("CONTENT_MAIN_MENU", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("CONTENT_MAIN_MENU", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(5000)

        composeTestRule
            .onNodeWithTag("MenuOptionItemSettings", useUnmergedTree = true)
            .performClick()

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("USER_PREFERENCES_SCREEN", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("USER_PREFERENCES_SCREEN", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(5000)

        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        /* vuelve a la pantalla principal */

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("CONTENT_MAIN_MENU", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("CONTENT_MAIN_MENU", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(5000)

        composeTestRule
            .onNodeWithTag("MenuOptionItemMyPosts", useUnmergedTree = true)
            .performClick()

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("MY_POSTS_SCREEN", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("MY_POSTS_SCREEN", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(5000)

        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        /* vuelve a la pantalla principal */

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("CONTENT_MAIN_MENU", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("CONTENT_MAIN_MENU", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(5000)

        composeTestRule
            .onNodeWithTag("MenuOptionItemPremium", useUnmergedTree = true)
            .performClick()

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("PaymentMarketScreen", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("PaymentMarketScreen", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(5000)

        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        /* vuelve a la pantalla principal */

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("CONTENT_MAIN_MENU", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("CONTENT_MAIN_MENU", useUnmergedTree = true)
            .assertIsDisplayed()

        Thread.sleep(5000)

        composeTestRule
            .onNodeWithTag("MenuOptionItemCloseSession", useUnmergedTree = true)
            .performClick()

        composeTestRule.waitUntil(2000) {
            composeTestRule.onAllNodesWithTag("showCloseSessionDialog", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("showCloseSessionDialog", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("CONFIRM_BUTTON", useUnmergedTree = true)
            .performClick()
    }

}