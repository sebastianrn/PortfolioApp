package dev.sebastianrn.portfolioapp.ui.components

import android.text.Layout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// Time range enum
enum class TimeRange(val label: String, val days: Int) {
    ONE_WEEK("1W", 7),
    ONE_MONTH("1M", 30),
    SIX_MONTHS("6M", 180),
    ONE_YEAR("1Y", 365),
    ALL("ALL", Int.MAX_VALUE)
}

@Composable
fun PortfolioChart(
    points: List<Pair<Long, Double>>,
    showTimeRangeSelector: Boolean = true,
    goldColor: Color = Color(0xFFFFD700)
) {
    if (points.isEmpty()) return

    // State for selected time range
    var selectedRange by remember { mutableStateOf(TimeRange.ONE_MONTH) }

    // Filter points based on selected time range
    val filteredPoints = remember(points, selectedRange) {
        if (points.isEmpty()) return@remember emptyList()

        val cutoffTime = if (selectedRange == TimeRange.ALL) {
            0L
        } else {
            System.currentTimeMillis() - (selectedRange.days * 24 * 60 * 60 * 1000L)
        }

        val filtered = points.filter { it.first >= cutoffTime }

        // Process to ensure only one data point per day (take the last one)
        val calendar = Calendar.getInstance()
        filtered.groupBy { (timestamp, _) ->
            calendar.timeInMillis = timestamp
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
        }.map { (_, dayPoints) ->
            dayPoints.maxByOrNull { it.first }!!
        }.sortedBy { it.first }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Time Range Selector
        if (showTimeRangeSelector && points.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeRange.entries.forEach { range ->
                    TimeRangeChip(
                        label = range.label,
                        selected = selectedRange == range,
                        onClick = { selectedRange = range },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Chart
        if (filteredPoints.isNotEmpty()) {
            EnhancedVicoChart(
                points = filteredPoints,
                timeRange = selectedRange,
                goldColor = goldColor
            )
        } else {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No data available for this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = goldColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun EnhancedVicoChart(
    points: List<Pair<Long, Double>>,
    timeRange: TimeRange,
    goldColor: Color
) {
    val modelProducer = remember { CartesianChartModelProducer() }

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

    // Calculate dynamic Y-axis range based on the data points
    val yAxisRange = remember(points, timeRange) {
        if (points.isEmpty()) {
            0.0 to 100.0
        } else {
            val values = points.map { it.second }
            val minValue = values.minOrNull() ?: 0.0
            val maxValue = values.maxOrNull() ?: 100.0
            val range = maxValue - minValue

            // Add padding: 5% below min and 5% above max for better visualization
            // For shorter time ranges, use tighter padding to emphasize value changes
            val paddingFactor = when (timeRange) {
                TimeRange.ONE_WEEK -> 0.10  // 10% padding for weekly view (more zoomed in)
                TimeRange.ONE_MONTH -> 0.08 // 8% padding for monthly view
                TimeRange.SIX_MONTHS -> 0.05 // 5% padding
                TimeRange.ONE_YEAR, TimeRange.ALL -> 0.03 // 3% padding for longer views
            }

            val padding = range * paddingFactor
            // Ensure minimum range of at least 1% of the average value to avoid flat lines
            val minRange = ((minValue + maxValue) / 2) * 0.01
            val adjustedPadding = maxOf(padding, minRange / 2)

            val adjustedMin = maxOf(0.0, minValue - adjustedPadding) // Y-axis min shouldn't go below 0
            val adjustedMax = maxValue + adjustedPadding

            adjustedMin to adjustedMax
        }
    }

    // Date formatter based on time range
    val dateFormatter = remember(timeRange) {
        when (timeRange) {
            TimeRange.ONE_WEEK -> SimpleDateFormat("EEE", Locale.getDefault()) // Mon, Tue
            TimeRange.ONE_MONTH -> SimpleDateFormat("MMM dd", Locale.getDefault()) // Jan 15
            TimeRange.SIX_MONTHS -> SimpleDateFormat("MMM dd", Locale.getDefault())
            TimeRange.ONE_YEAR, TimeRange.ALL -> SimpleDateFormat("MMM yy", Locale.getDefault()) // Jan 25
        }
    }

    // Enhanced axis labels
    val axisLabelComponent = rememberAxisLabelComponent(
        color = Color.White.copy(alpha = 0.85f),
        textSize = 11.sp
    )

    // Larger indicator with glow
    val indicatorComponent = rememberLineComponent(
        fill = fill(goldColor),
        thickness = 16.dp,
        strokeFill = fill(goldColor.copy(alpha = 0.3f)),
        strokeThickness = 10.dp
    )

    // Visible guideline
    val guidelineComponent = rememberLineComponent(
        fill = fill(goldColor.copy(alpha = 0.6f)),
        thickness = 2.dp
    )

    // Enhanced label
    val labelComponent = rememberTextComponent(
        color = goldColor,
        textSize = 13.sp,
        lineCount = 2,
        textAlignment = Layout.Alignment.ALIGN_CENTER
    )

    val yAxisFormatter = { value: Double ->
        when {
            value >= 1_000_000 -> "${(value / 1_000_000).roundToInt()}M"
            value >= 1_000 -> "${(value / 1_000).roundToInt()}k"
            else -> value.toInt().toString()
        }
    }

    val yAxisValueFormatter = remember {
        CartesianValueFormatter { _, value, _ -> yAxisFormatter(value) }
    }

    val markerYValueFormatter = { value: Double ->
        val formatted = NumberFormat.getInstance(Locale.GERMAN).format(value.roundToInt())
        "CHF $formatted"
    }

    val getFormattedDate = { value: Double ->
        val index = value.toInt()
        if (index in points.indices) {
            dateFormatter.format(Date(points[index].first))
        } else {
            "–"
        }
    }

    val marker = rememberDefaultCartesianMarker(
        label = labelComponent,
        labelPosition = DefaultCartesianMarker.LabelPosition.Top,
        indicator = { indicatorComponent },
        indicatorSize = 14.dp,
        guideline = guidelineComponent,
        valueFormatter = { _, targets ->
            val lineTarget = targets.firstOrNull() as? LineCartesianLayerMarkerTarget
            val entry = lineTarget?.points?.firstOrNull()?.entry
            if (entry != null) {
                val dateStr = getFormattedDate(entry.x)
                val valueStr = markerYValueFormatter(entry.y)
                "$dateStr\n$valueStr"
            } else {
                "no data"
            }
        }
    )

    // Enhanced 4-color gradient
    val gradientColors = arrayOf(
        goldColor.copy(alpha = 0.7f),
        goldColor.copy(alpha = 0.5f),
        goldColor.copy(alpha = 0.25f),
        Color.Transparent
    )

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
                    LineCartesianLayer.Line(
                        fill = LineCartesianLayer.LineFill.single(fill(goldColor)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            fill(
                                ShaderProvider.verticalGradient(gradientColors)
                            )
                        )
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = yAxisValueFormatter,
                label = axisLabelComponent,
                guideline = rememberLineComponent(
                    fill = fill(Color.White.copy(alpha = 0.15f)),
                    thickness = 0.5.dp
                )
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, value, _ -> getFormattedDate(value) },
                label = axisLabelComponent,
                guideline = null,
                itemPlacer = remember(points, timeRange) {
                    // Adjust spacing based on data points
                    val spacing = when {
                        points.size <= 7 -> 1 // Show all labels for week view
                        points.size <= 30 -> maxOf(1, points.size / 6)
                        else -> maxOf(1, points.size / 5)
                    }
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
            marker = marker
        ),
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        animateIn = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
    )
}

@Composable
private fun TimeRangeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goldColor = Color(0xFFFFD700)
    val backgroundColor = Color(0xFF1A1A1A)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) goldColor else backgroundColor,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.Black else Color.White.copy(alpha = 0.7f)
            )
        }
    }
}