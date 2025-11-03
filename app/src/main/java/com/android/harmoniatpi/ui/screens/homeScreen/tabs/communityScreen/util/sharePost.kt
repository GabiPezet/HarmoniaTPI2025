package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.util

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.android.harmoniatpi.domain.model.userPreferences.Post

fun sharePost(
    post: Post,
    context: Context,
    isMyPost: Boolean,
    userName: String,
    userLastName: String
) {
    val shareMessage = buildShareMessage(post,isMyPost,userName,userLastName)

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareMessage)
        type = "text/plain"
    }

    val shareChooser = Intent.createChooser(shareIntent, "Compartir proyecto")
    ContextCompat.startActivity(context, shareChooser, null)
}

