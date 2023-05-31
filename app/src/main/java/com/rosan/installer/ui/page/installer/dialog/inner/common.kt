package com.rosan.installer.ui.page.installer.dialog.inner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.HourglassDisabled
import androidx.compose.material.icons.twotone.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.ui.page.installer.dialog.DialogViewModel
import com.rosan.installer.util.help

val pausingIcon: @Composable () -> Unit = {
    Icon(
        imageVector = Icons.TwoTone.HourglassDisabled, contentDescription = null
    )
}

val workingIcon: @Composable () -> Unit = {
    Icon(
        imageVector = Icons.TwoTone.HourglassEmpty, contentDescription = null
    )
}

val errorText: ((installer: InstallerRepo, viewModel: DialogViewModel) -> (@Composable () -> Unit)) =
    { installer, viewModel ->
        {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onErrorContainer) {
                LazyColumn(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .fillMaxWidth()
                        .padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(installer.error.help(), fontWeight = FontWeight.Bold)
                    }
                    item {
                        SelectionContainer {
                            Text(installer.error.stackTraceToString().trim())
                        }
                    }
                }
            }
        }
    }