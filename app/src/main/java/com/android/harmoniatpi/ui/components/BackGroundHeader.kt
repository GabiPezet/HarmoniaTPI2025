package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.harmoniatpi.R

@Composable
fun PreviewBackGroundHeader() {
    Image(
        painter = painterResource(id = R.drawable.ic_welcome_header_background),
        contentDescription = "Header Background",
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
fun LoginBackGroundHeader() {
    Image(
        painter = painterResource(id = R.drawable.ic_login_header_background),
        contentDescription = "Header Background",
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth
    )
}


@Composable
fun RegisterBackgroundHeader() {
    Image(
        painter = painterResource(id = R.drawable.ic_register_header_background),
        contentDescription = "Register Header Background",
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth
    )
}


