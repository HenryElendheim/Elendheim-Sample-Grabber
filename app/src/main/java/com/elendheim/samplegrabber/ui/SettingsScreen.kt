package com.elendheim.samplegrabber.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elendheim.samplegrabber.R
import com.elendheim.samplegrabber.data.ExportFormat
import com.elendheim.samplegrabber.data.Settings

@Composable
fun SettingsScreen(
    format: ExportFormat,
    recordSeconds: Int,
    onFormatChange: (ExportFormat) -> Unit,
    onRecordSecondsChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = stringResource(R.string.save_format),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(R.string.save_format_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExportFormat.entries.forEach { option ->
                FilterChip(
                    selected = format == option,
                    onClick = { onFormatChange(option) },
                    label = { Text(option.extension.uppercase()) }
                )
            }
        }

        Text(
            text = stringResource(R.string.record_length),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 28.dp)
        )
        Text(
            text = stringResource(R.string.record_length_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Settings.LENGTH_OPTIONS.forEach { option ->
                FilterChip(
                    selected = recordSeconds == option,
                    onClick = { onRecordSecondsChange(option) },
                    label = { Text(stringResource(R.string.seconds_short, option)) }
                )
            }
        }
    }
}
