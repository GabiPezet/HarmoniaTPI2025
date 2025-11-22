package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.harmoniatpi.R

@Composable
private fun BaseHeader(
    id: Int,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = id),
        contentDescription = "Header Background",
        contentScale = ContentScale.Crop,
        alignment = Alignment.BottomCenter,
        modifier = modifier
    )
}

@Composable
fun PreviewBackGroundHeader(modifier: Modifier = Modifier) {
    BaseHeader(id = R.drawable.ic_welcome_header_background, modifier = modifier)
}

@Composable
fun LoginBackGroundHeader(modifier: Modifier = Modifier) {
    BaseHeader(id = R.drawable.ic_login_header_background, modifier = modifier)
}

@Composable
fun RegisterBackgroundHeader(modifier: Modifier = Modifier) {
    BaseHeader(id = R.drawable.ic_register_header_background, modifier = modifier)
}

