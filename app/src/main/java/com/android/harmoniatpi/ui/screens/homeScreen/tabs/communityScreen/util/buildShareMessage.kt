package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.util

import com.android.harmoniatpi.domain.model.userPreferences.Post

 fun buildShareMessage(post: Post, isMyPost: Boolean, userName: String, userLastName: String): String {
    return when (isMyPost) {
        true -> {
            if (post.urlCompleteAudio.isNotEmpty()) {
                """
        Hola Soy ${post.name}, te comparto mi proyecto hecho con la aplicación HoloJam:
        
        ${post.title}
        ${post.description}
        
        ${post.urlCompleteAudio}
        
        Espero que te guste.
        
        #${post.hashtags.joinToString(" #")}
        
        """.trimIndent()
            } else {
                """
        Hola Soy ${post.name}, te comparto mi posteo hecho con la aplicación HoloJam:
        
        ${post.title}
        
        ${post.description}
        
        #${post.hashtags.joinToString(" #")}
        """.trimIndent()
            }
        }
        false -> {
            if (post.urlCompleteAudio.isNotEmpty()) {
                """
        Hola Soy $userName ${userLastName}, te comparto el proyecto de ${post.name} ${post.lasName} proyecto hecho con la aplicación HoloJam:
        
        ${post.title}
        ${post.description}
        
        ${post.urlCompleteAudio}
        
        Espero que te guste.
        
        #${post.hashtags.joinToString(" #")}
        
        """.trimIndent()
            } else {
                """
        Hola Soy $userName ${userLastName}, te comparto el post de ${post.name} ${post.lasName} proyecto hecho con la aplicación HoloJam:
        
        ${post.title}
        
        ${post.description}
        
        #${post.hashtags.joinToString(" #")}
        """.trimIndent()
            }
        }
    }


}