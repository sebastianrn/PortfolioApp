package dev.sebastianrn.portfolioapp.ui.components.chart

import android.text.Layout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import com.patrykandpatrick.vico.core.common.shape.Shape
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.ui.components.common.TrendChip
import dev.sebastianrn.portfolioapp.util.formatAsPercentage
import dev.sebastianrn.portfolioapp.util.formatCurrency
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Portfolio line chart with a range performance header, trend-colored line
 * and an optional dashed break-even reference line.
 */
@Composable
fun PortfolioChart(
    points: List<Pair<Long, Double>>,
    showTimeRangeSelector: Boolean = true,
    goldColor: Color = MaterialTheme.colorScheme.primary,
    referenceValue: Double? = null,
    referenceLabel: String = "Invested"
) {
    if (points.isEmpty()) return

    // Start on a range that actually has data (stale assets have nothing recent)
    var selectedRange by remember { mutableStateOf(ChartDataProcessor.defaultTimeRange(points)) }

    val filteredPoints = remember(points, selectedRange) {
        ChartDataProcessor.filterPointsByTimeRange(points, selectedRange)
    }

    val firstValue = filteredPoints.firstOrNull()?.second
    val lastValue = filteredPoints.lastOrNull()?.second
    val hasTrend = filteredPoints.size >= 2 && firstValue != null && lastValue != null
    val delta = if (hasTrend) lastValue!! - firstValue!! else 0.0
    val deltaPercent = if (hasTrend && firstValue != 0.0) (delta / firstValue!!) * 100 else 0.0
    val trendUp = delta >= 0

    val chartColor = when {
        !hasTrend -> goldColor
        trendUp -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (hasTrend) {
            RangeDeltaHeader(
                delta = delta,
                deltaPercent = deltaPercent,
                trendUp = trendUp,
                range = selectedRange
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (filteredPoints.isNotEmpty()) {
            VicoLineChart(
                points = filteredPoints,
                timeRange = selectedRange,
                chartColor = chartColor,
                referenceValue = referenceValue,
                referenceLabel = referenceLabel,
                referenceColor = goldColor
            )
        } else {
            EmptyChartState(color = goldColor)
        }

        if (showTimeRangeSelector) {
            Spacer(modifier = Modifier.height(12.dp))
            TimeRangeSelector(
                selectedRange = selectedRange,
                onRangeSelected = { selectedRange = it }
            )
        }
    }
}

@Composable
private fun RangeDeltaHeader(
    delta: Double,
    deltaPercent: Double,
    trendUp: Boolean,
    range: TimeRange
) {
    val trendColor = if (trendUp) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.error
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = delta.formatCurrency(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = trendColor
            )
            Text(
                text = stringResource(range.descriptorRes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TrendChip(
            text = deltaPercent.formatAsPercentage(showSign = true),
            positive = trendUp
        )
    }
}

@Composable
private fun VicoLineChart(
    points: List<Pair<Long, Double>>,
    timeRange: TimeRange,
    chartColor: Color,
    referenceValue: Double?,
    referenceLabel: String,
    referenceColor: Color
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    LaunchedEffect(points) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = points.indices.map { it.toFloat() },
                    y = points.map { it.second }
                )
            }
        }
    }

    val yAxisRange = remember(points, timeRange) {
        ChartDataProcessor.calculateYAxisRange(points, timeRange)
    }

    val dateFormatter = remember(timeRange) {
        DateTimeFormatter.ofPattern(
            ChartDataProcessor.getDateFormatterPattern(timeRange),
            Locale.getDefault()
        )
    }

    val axisLabelComponent = rememberAxisLabelComponent(
        color = onSurfaceVariant,
        textSize = 10.sp
    )

    val yAxisValueFormatter = remember {
        CartesianValueFormatter { _, value, _ ->
            ChartFormatters.formatYAxisValue(value)
        }
    }

    val getFormattedDate = { value: Double ->
        ChartFormatters.formatMarkerDate(value.toInt(), points, dateFormatter)
    }

    val marker = rememberChartMarker(
        chartColor = chartColor,
        getFormattedDate = getFormattedDate
    )

    // Haptic tick as the marker snaps between points while scrubbing
    val haptics = LocalHapticFeedback.current
    val markerVisibilityListener = remember(haptics) {
        object : CartesianMarkerVisibilityListener {
            private var lastX: Double? = null

            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                lastX = targets.firstOrNull()?.x
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val x = targets.firstOrNull()?.x
                if (x != lastX) {
                    lastX = x
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }

            override fun onHidden(marker: CartesianMarker) {
                lastX = null
            }
        }
    }

    // Soft fill so the line carries the design
    val gradientColors = arrayOf(
        chartColor.copy(alpha = 0.30f),
        chartColor.copy(alpha = 0.12f),
        Color.Transparent
    )

    val dashedGuideline = rememberLineComponent(
        fill = fill(onSurface.copy(alpha = 0.10f)),
        thickness = 1.dp,
        shape = remember { DashedShape(Shape.Rectangle, 8f, 6f) }
    )

    // Dashed break-even line (only visible when within the y-range)
    val referenceLineComponent = rememberLineComponent(
        fill = fill(referenceColor.copy(alpha = 0.65f)),
        thickness = 1.dp,
        shape = remember { DashedShape(Shape.Rectangle, 10f, 6f) }
    )
    val referenceLabelComponent = rememberTextComponent(
        color = referenceColor,
        textSize = 10.sp,
        padding = Insets(6f, 2f)
    )
    val referenceLine = referenceValue?.let { ref ->
        remember(ref, referenceLineComponent, referenceLabelComponent, referenceLabel) {
            HorizontalLine(
                y = { ref },
                line = referenceLineComponent,
                labelComponent = referenceLabelComponent,
                label = { referenceLabel }
            )
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                rangeProvider = remember(yAxisRange) {
                    CartesianLayerRangeProvider.fixed(
                        minX = 0.0,
                        minY = yAxisRange.first,
                        maxY = yAxisRange.second
                    )
                },
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = remember(chartColor) {
                            LineCartesianLayer.LineFill.single(fill(chartColor))
                        },
                        stroke = LineCartesianLayer.LineStroke.continuous(thickness = 2.5.dp),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            fill(ShaderProvider.verticalGradient(gradientColors))
                        ),
                        pointConnector = remember { LineCartesianLayer.PointConnector.cubic(0.5f) }
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = yAxisValueFormatter,
                label = axisLabelComponent,
                guideline = dashedGuideline,
                itemPlacer = remember { VerticalAxis.ItemPlacer.count({ 4 }) }
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, value, _ -> getFormattedDate(value) },
                label = axisLabelComponent,
                guideline = null,
                itemPlacer = remember(points, timeRange) {
                    val spacing = ChartDataProcessor.calculateAxisLabelSpacing(points.size)
                    HorizontalAxis.ItemPlacer.aligned(
                        spacing = { spacing },
                        addExtremeLabelPadding = true
                    )
                }
            ),
            layerPadding = {
                cartesianLayerPadding(
                    scalableStart = 8.dp,
                    scalableEnd = 8.dp,
                    unscalableStart = 4.dp,
                    unscalableEnd = 4.dp
                )
            },
            marker = marker,
            markerVisibilityListener = markerVisibilityListener,
            decorations = listOfNotNull(referenceLine)
        ),
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        animateIn = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    )
}

@Composable
private fun rememberChartMarker(
    chartColor: Color,
    getFormattedDate: (Double) -> String
): DefaultCartesianMarker {
    // Pill label with a real background instead of bare floating text
    val labelBackground = rememberShapeComponent(
        fill = fill(MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = CorneredShape.rounded(10f),
        strokeFill = fill(chartColor.copy(alpha = 0.45f)),
        strokeThickness = 1.dp
    )
    val labelComponent = rememberTextComponent(
        color = MaterialTheme.colorScheme.onSurface,
        textSize = 12.sp,
        lineCount = 2,
        textAlignment = Layout.Alignment.ALIGN_CENTER,
        padding = Insets(10f, 6f),
        background = labelBackground
    )

    // Small ring dot on the line
    val indicatorComponent = rememberShapeComponent(
        fill = fill(chartColor),
        shape = CorneredShape.Pill,
        strokeFill = fill(MaterialTheme.colorScheme.surface),
        strokeThickness = 2.dp
    )

    // Hairline vertical guideline
    val guidelineComponent = rememberLineComponent(
        fill = fill(chartColor.copy(alpha = 0.45f)),
        thickness = 1.dp
    )

    return rememberDefaultCartesianMarker(
        label = labelComponent,
        labelPosition = DefaultCartesianMarker.LabelPosition.Top,
        indicator = { indicatorComponent },
        indicatorSize = 12.dp,
        guideline = guidelineComponent,
        valueFormatter = { _, targets ->
            val lineTarget = targets.firstOrNull() as? LineCartesianLayerMarkerTarget
            val entry = lineTarget?.points?.firstOrNull()?.entry
            if (entry != null) {
                val dateStr = getFormattedDate(entry.x)
                val valueStr = ChartFormatters.formatMarkerValue(entry.y)
                "$dateStr\n$valueStr"
            } else {
                "no data"
            }
        }
    )
}

@Composable
private fun EmptyChartState(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.chart_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = color.copy(alpha = 0.5f)
        )
    }
}
