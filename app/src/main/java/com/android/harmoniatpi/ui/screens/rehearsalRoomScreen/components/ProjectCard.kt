package com.android.harmoniatpi.ui.screens.rehearsalRoomScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.harmoniatpi.domain.model.project.Project

@Composable
fun ProjectCard(project: Project) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(project.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            //WaveformPreview(project.audioWaveform)
            Spacer(Modifier.height(8.dp))
            Text(project.description, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(Modifier.height(4.dp))
            Text(project.hashtags.joinToString(" "), fontStyle = FontStyle.Italic, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { /* Likear */ }) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Like"
                    )
                }
                IconButton(onClick = { /* Comentar */ }) {
                    Icon(
                        Icons.Default.ModeComment,
                        contentDescription = "Comment"
                    )
                }
                IconButton(onClick = { /* Compartir */ }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share"
                    )
                }
                IconButton(onClick = { /* Descargar */ }) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download"
                    )
                }
                IconButton(onClick = { /* Settings */ }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        }
    }
}