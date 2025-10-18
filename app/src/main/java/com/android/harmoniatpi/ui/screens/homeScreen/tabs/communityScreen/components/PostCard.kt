package com.android.harmoniatpi.ui.screens.homeScreen.tabs.communityScreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.userPreferences.Post

@Composable
fun PostCard(
    post: Post,
    onLikeClicked: () -> Unit,
    onCommentClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    isMyPost: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            // Header con información del usuario y opción de borrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${post.name} ${post.lasName}", fontWeight = FontWeight.Bold)

                if (isMyPost) {
                    IconButton(
                        onClick = onDeleteClicked,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Borrar post",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(post.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(post.description)

            if (post.hashtags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    post.hashtags.forEach {
                        AssistChip(onClick = {}, label = { Text("#$it") })
                    }
                }
            }

            // Footer con acciones (like y comentarios)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClicked() }
                ) {
                    Icon(
                        if (post.likes > 0) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                        contentDescription = "Like",
                        tint = if (post.likes > 0) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        post.likes.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.width(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCommentClicked() }
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Comment,
                        contentDescription = "Comentarios"
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        post.comments.size.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}