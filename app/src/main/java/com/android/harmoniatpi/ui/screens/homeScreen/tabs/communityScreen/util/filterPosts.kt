package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.util

import com.android.harmoniatpi.domain.model.userPreferences.Post

fun filterPosts(posts: List<Post>, query: String): List<Post> {
    if (query.isBlank()) return posts
    val normalized = query.normalizeText()

    return posts.filter { post ->
        post.title.normalizeText().contains(normalized) ||
                post.description.normalizeText().contains(normalized) ||
                post.name.normalizeText().contains(normalized) ||
                post.lasName.normalizeText().contains(normalized) ||
                post.hashtags.any { it.normalizeText().contains(normalized) }
    }
}
