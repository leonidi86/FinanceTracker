package com.example.financetracker.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val FLING_VELOCITY_THRESHOLD = 1000f

/**
 * Контейнер, который свайпом вправо сдвигает [content] и открывает под ним [actions] шириной [actionsWidth].
 * В [actions] передаётся функция закрытия, чтобы после нажатия на действие вернуть карточку на место.
 */
@Composable
fun SwipeToRevealBox(
    actions: @Composable (close: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    actionsWidth: Dp = 80.dp,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val maxOffset = with(LocalDensity.current) { actionsWidth.toPx() }
    val offset = remember { Animatable(0f) }
    val close: () -> Unit = { scope.launch { offset.animateTo(0f) } }

    Box(modifier = modifier.height(IntrinsicSize.Min)) {
        if (offset.value.roundToInt() > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(actionsWidth)
            ) {
                actions(close)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .background(MaterialTheme.colorScheme.surface)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch { offset.snapTo((offset.value + delta).coerceIn(0f, maxOffset)) }
                    },
                    onDragStopped = { velocity ->
                        val shouldOpen = velocity > FLING_VELOCITY_THRESHOLD ||
                            (velocity >= -FLING_VELOCITY_THRESHOLD && offset.value > maxOffset / 2)
                        offset.animateTo(if (shouldOpen) maxOffset else 0f, initialVelocity = velocity)
                    }
                )
        ) {
            content()
        }
    }
}
