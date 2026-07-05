package com.elendheim.samplegrabber.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elendheim.samplegrabber.GrabberViewModel
import com.elendheim.samplegrabber.R
import com.elendheim.samplegrabber.RecorderState
import com.elendheim.samplegrabber.audio.SampleRecorder
import com.elendheim.samplegrabber.data.Sample
import java.util.Locale

@Composable
fun MainScreen(viewModel: GrabberViewModel, onMicTap: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val samples by viewModel.samples.collectAsState()
    val playingUri by viewModel.playingUri.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header()
            RecordZone(state = state, onMicTap = onMicTap)
            StatusLine(state = state)
            Spacer(Modifier.height(20.dp))
            SampleList(
                samples = samples,
                playingUri = playingUri,
                onPlay = viewModel::togglePlay,
                onDelete = viewModel::delete,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_squiggle),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecordZone(state: RecorderState, onMicTap: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val recording = state as? RecorderState.Recording
    val level = recording?.level ?: 0f
    val progress = (recording?.elapsedMs ?: 0) /
        (SampleRecorder.MAX_SECONDS * 1000f)

    val pulse by animateFloatAsState(
        targetValue = if (recording != null) 1f + level * 0.12f else 1f,
        animationSpec = spring(stiffness = 800f),
        label = "pulse"
    )
    val ringAlpha by animateFloatAsState(
        targetValue = if (recording != null) 0.25f + level * 0.75f else 0f,
        animationSpec = tween(80),
        label = "ringAlpha"
    )
    val buttonColor by animateColorAsState(
        targetValue = if (recording != null) Color(0xFFFF6B57) else MaterialTheme.colorScheme.primary,
        label = "buttonColor"
    )

    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .padding(top = 28.dp)
            .size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val radius = size.minDimension / 2 - stroke
            // level glow
            drawCircle(
                color = primary.copy(alpha = ringAlpha * 0.3f),
                radius = radius * (0.78f + level * 0.22f),
                center = Offset(size.width / 2, size.height / 2)
            )
            // progress track and arc
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(stroke, stroke),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    color = primary,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }

        Surface(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onMicTap()
            },
            enabled = state !is RecorderState.Saving,
            shape = CircleShape,
            color = buttonColor,
            modifier = Modifier
                .size(120.dp)
                .scale(pulse)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = if (recording != null) Icons.Rounded.Stop else Icons.Rounded.Mic,
                    contentDescription = stringResource(
                        if (recording != null) R.string.stop_button else R.string.record_button
                    ),
                    tint = Ink,
                    modifier = Modifier.size(52.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusLine(state: RecorderState) {
    val text = when (state) {
        is RecorderState.Recording -> {
            val seconds = state.elapsedMs / 1000
            val tenths = state.elapsedMs % 1000 / 100
            String.format(Locale.US, "0:%02d.%d", seconds, tenths)
        }

        is RecorderState.Saving -> stringResource(R.string.saving)
        else -> stringResource(R.string.tap_to_grab)
    }
    val hint = when {
        state is RecorderState.Recording && state.stopping &&
            state.elapsedMs < SampleRecorder.MIN_SECONDS * 1000 ->
            stringResource(R.string.min_length_hint)

        state is RecorderState.Recording -> stringResource(R.string.recording_hint)
        else -> ""
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = if (state is RecorderState.Recording) FontFamily.Monospace else null
            ),
            modifier = Modifier.padding(top = 16.dp)
        )
        if (hint.isNotEmpty()) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SampleList(
    samples: List<Sample>,
    playingUri: android.net.Uri?,
    onPlay: (Sample) -> Unit,
    onDelete: (Sample) -> Unit,
    modifier: Modifier = Modifier
) {
    if (samples.isEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.empty_list_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.empty_list_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp)
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.your_grabs),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(samples, key = { it.uri }) { sample ->
                SampleRow(
                    sample = sample,
                    playing = playingUri == sample.uri,
                    onPlay = { onPlay(sample) },
                    onDelete = { onDelete(sample) }
                )
            }
        }
    }
}

@Composable
private fun SampleRow(
    sample: Sample,
    playing: Boolean,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            IconButton(onClick = onPlay) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playing) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(
                            if (playing) R.string.stop_sample else R.string.play_sample
                        ),
                        tint = Ink,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sample.name.removeSuffix(".wav"),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = String.format(
                        Locale.US,
                        "%.1f s | %d KB",
                        sample.durationMs / 1000f,
                        sample.sizeBytes / 1024
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.delete_sample),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
