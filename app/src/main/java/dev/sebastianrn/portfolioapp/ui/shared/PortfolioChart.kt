package dev.sebastianrn.portfolioapp.ui.components

import android.text.Layout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
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
import dev.sebastianrn.portfolioapp.ui.theme.GoldStart
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun PortfolioChart(
    points: List<Pair<Long, Double>>
) {
    if (points.isEmpty()) return

    // Process points to ensure only one data point per day (take the last one)
    val processedPoints = remember(points) {
        if (points.isEmpty()) return@remember emptyList()

        // Group by date (ignoring time) and take the last entry for each day
        val calendar = java.util.Calendar.getInstance()
        points.groupBy { (timestamp, _) ->
            calendar.timeInMillis = timestamp
            // Create a key using year, month, and day
            "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)}-${calendar.get(java.util.Calendar.DAY_OF_MONTH)}"
        }.map { (_, dayPoints) ->
            // Take the last point of each day (sorted by timestamp)
            dayPoints.maxByOrNull { it.first }!!
        }.sortedBy { it.first } // Sort by timestamp ascending
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(processedPoints) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = processedPoints.indices.map { it.toFloat() },
                    y = processedPoints.map { it.second }
                )
            }
        }
    }

    val dateTimeFormatter = remember { SimpleDateFormat("MMM yy", Locale.getDefault()) }

    // Enhanced axis labels with better visibility
    val axisLabelComponent = rememberAxisLabelComponent(
        color = Color.White.copy(alpha = 0.85f),
        textSize = 11.sp
    )

    // Larger indicator for better visibility (thicker stroke gives glow effect)
    val indicatorComponent = rememberLineComponent(
        fill = fill(GoldStart),
        thickness = 16.dp,
        strokeFill = fill(GoldStart.copy(alpha = 0.3f)),
        strokeThickness = 10.dp
    )

    // More visible guideline with gold tint
    val guidelineComponent = rememberLineComponent(
        fill = fill(GoldStart.copy(alpha = 0.6f)),
        thickness = 2.dp
    )

    // Enhanced label component
    val labelComponent = rememberTextComponent(
        color = GoldStart,
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
        if (index in processedPoints.indices) {
            dateTimeFormatter.format(Date(processedPoints[index].first))
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

    // Enhanced gradient with 4 color stops for richer depth
    val gradientColors = arrayOf(
        GoldStart.copy(alpha = 0.7f),
        GoldStart.copy(alpha = 0.5f),
        GoldStart.copy(alpha = 0.25f),
        Color.Transparent
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                rangeProvider = remember { CartesianLayerRangeProvider.fixed(minX = 0.0) },
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(GoldStart)),
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
                itemPlacer = remember(processedPoints) {
                    HorizontalAxis.ItemPlacer.aligned(
                        spacing = { maxOf(1, processedPoints.size / 6) },
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
            .height(230.dp) // Slightly taller for better data visibility
    )
}