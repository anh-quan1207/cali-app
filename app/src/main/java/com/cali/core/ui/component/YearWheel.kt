package com.cali.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cali.core.ui.theme.Spacing
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** Bánh xe chọn năm sinh: 3 hàng 48dp, hàng giữa là giá trị (S01 bước 1). */
@Composable
fun YearWheel(
    years: IntRange,
    selected: Int,
    onSelected: (Int) -> Unit,
    label: (Int) -> String,
    modifier: Modifier = Modifier,
) {
    val items = remember(years) { years.toList() }
    val start = items.indexOf(selected).coerceAtLeast(0)
    val state = rememberLazyListState(initialFirstVisibleItemIndex = start)
    val rowPx = with(LocalDensity.current) { ROW.toPx() }
    val center by remember {
        derivedStateOf { state.firstVisibleItemIndex + if (state.firstVisibleItemScrollOffset > rowPx / 2) 1 else 0 }
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress to center }
            .distinctUntilChanged()
            .collect { (scrolling, idx) -> if (!scrolling) items.getOrNull(idx)?.let(onSelected) }
    }
    val cs = MaterialTheme.colorScheme
    Box(
        modifier.fillMaxWidth().height(ROW * 3).clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow),
    ) {
        Box(
            Modifier.align(Alignment.Center).padding(horizontal = Spacing.md).fillMaxWidth().height(ROW)
                .clip(MaterialTheme.shapes.medium).background(cs.secondaryContainer),
        )
        LazyColumn(
            state = state,
            flingBehavior = rememberSnapFlingBehavior(state),
            contentPadding = PaddingValues(vertical = ROW),
            modifier = Modifier.fillMaxWidth().height(ROW * 3),
        ) {
            itemsIndexed(items) { i, year ->
                val isCenter = i == center
                Box(
                    Modifier.fillMaxWidth().height(ROW).semantics {
                        contentDescription = label(year)
                        onClick { scope.launch { state.animateScrollToItem(i) }; true }
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isCenter) label(year) else year.toString(),
                        style = if (isCenter) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                        color = if (isCenter) cs.onSecondaryContainer else cs.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private val ROW = 48.dp
