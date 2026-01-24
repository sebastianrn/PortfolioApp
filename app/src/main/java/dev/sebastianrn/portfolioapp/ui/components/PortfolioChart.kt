package dev.sebastianrn.portfolioapp.ui.components

import android.text.Layout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import dev.sebastianrn.portfolioapp.ui.components.chart.ChartDataProcessor
import dev.sebastianrn.portfolioapp.ui.components.chart.ChartFormatters
import dev.sebastianrn.portfolioapp.ui.components.chart.TimeRange
import dev.sebastianrn.portfolioapp.ui.components.chart.TimeRangeSelector

@Composable
fun PortfolioChart(
    points: List<Pair<Long, Double>>,
    showTimeRangeSelector: Boolean = true,
    goldColor: Color = MaterialTheme.colorScheme.primary
) {
    if (points.isEmpty()) return

    var selectedRange by remember { mutableStateOf(TimeRange.ONE_MONTH) }

    val filteredPoints = remember(points, selectedRange) {
        ChartDataProcessor.filterPointsByTimeRange(points, selectedRange)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showTimeRangeSelector && points.isNotEmpty()) {
            TimeRangeSelector(
                selectedRange = selectedRange,
                onRangeSelected = { selectedRange = it }
            )
        }

        if (filteredPoints.isNotEmpty()) {
            VicoLineChart(
                points = filteredPoints,
                timeRange = selectedRange,
                chartColor = goldColor
            )
        } else {
            EmptyChartState(color = goldColor)
        }
    }
}

@Composable
private fun VicoLineChart(
    points: List<Pair<Long, Double>>,
    timeRange: TimeRange,
    chartColor: Color
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val onSurface = MaterialTheme.colorScheme.onSurface

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
        ChartDataProcessor.getDateFormatter(timeRange)
    }

    val axisLabelComponent = rememberAxisLabelComponent(
        color = onSurface.copy(alpha = 0.85f),
        textSize = 11.sp
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
        points = points,
        getFormattedDate = getFormattedDate
    )

    val gradientColors = arrayOf(
        chartColor.copy(alpha = 0.7f),
        chartColor.copy(alpha = 0.5f),
        chartColor.copy(alpha = 0.25f),
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
                        fill = LineCartesianLayer.LineFill.single(fill(chartColor)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            fill(ShaderProvider.verticalGradient(gradientColors))
                        )
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = yAxisValueFormatter,
                label = axisLabelComponent,
                guideline = rememberLineComponent(
                    fill = fill(onSurface.copy(alpha = 0.15f)),
                    thickness = 0.5.dp
                )
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
private fun rememberChartMarker(
    chartColor: Color,
    points: List<Pair<Long, Double>>,
    getFormattedDate: (Double) -> String
): DefaultCartesianMarker {
    val indicatorComponent = rememberLineComponent(
        fill = fill(chartColor),
        thickness = 16.dp,
        strokeFill = fill(chartColor.copy(alpha = 0.3f)),
        strokeThickness = 10.dp
    )

    val guidelineComponent = rememberLineComponent(
        fill = fill(chartColor.copy(alpha = 0.6f)),
        thickness = 2.dp
    )

    val labelComponent = rememberTextComponent(
        color = chartColor,
        textSize = 13.sp,
        lineCount = 2,
        textAlignment = Layout.Alignment.ALIGN_CENTER
    )

    return rememberDefaultCartesianMarker(
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
            "No data available for this period",
            style = MaterialTheme.typography.bodyMedium,
            color = color.copy(alpha = 0.5f)
        )
    }
}
