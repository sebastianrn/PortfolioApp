package dev.sebastianrn.portfolioapp.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.text.Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import dev.sebastianrn.portfolioapp.ui.theme.GoldStart
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun PortfolioChart(
    points: List<Pair<Long, Double>>,
    isFullScreen: Boolean = false,
    onZoomClick: () -> Unit = {}
) {
    val context = LocalContext.current

    if (points.isEmpty()) return

    val model = remember(points) {
        CartesianChartModel(
            LineCartesianLayerModel.build {
                series(
                    x = points.indices.map { it.toFloat() },
                    y = points.map { it.second }
                )
            }
        )
    }

    val dateTimeFormatter = remember { SimpleDateFormat("dd.MM.yy", Locale.getDefault()) }

    val axisLabelComponent = rememberAxisLabelComponent(
        color = Color.White,
        textSize = 12.sp
    )

    val indicatorComponent = rememberLineComponent(
        fill = fill(GoldStart),
        thickness = 14.dp,
        strokeThickness = 8.dp,
    )

    val guidelineComponent = rememberLineComponent(
        fill = fill(GoldStart),
        thickness = 1.5.dp
    )

    val labelComponent = rememberTextComponent(
        color = GoldStart,
        textSize = 12.sp,
        lineCount = 2,
        textAlignment = Layout.Alignment.ALIGN_CENTER
    )

    val yAxisFormatter = { value: Double ->
        if (value < 1000) {
            value.toInt().toString()
        } else {
            val thousands = (value / 1000.0).roundToInt()
            "${thousands}k"
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
            dateTimeFormatter.format(Date(points[index].first))
        } else {
            "–"
        }
    }

    val marker = rememberDefaultCartesianMarker(
        label = labelComponent,
        labelPosition = DefaultCartesianMarker.LabelPosition.Top,
        indicator = { indicatorComponent },
        indicatorSize = 12.dp,
        guideline = guidelineComponent,
        valueFormatter = { _, targets ->
            val lineTarget =
                targets.firstOrNull() as? com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
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

    DisposableEffect(isFullScreen) {
        val activity = context as? Activity
        if (isFullScreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose { }
    }

    Box(
        modifier = if (isFullScreen) Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        else Modifier.fillMaxWidth().height(200.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    rangeProvider = remember { CartesianLayerRangeProvider.fixed(minX = 0.0) },
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(fill(GoldStart)),
                            areaFill = LineCartesianLayer.AreaFill.single(
                                fill(
                                    ShaderProvider.verticalGradient(
                                        arrayOf(GoldStart.copy(alpha = 0.5f), Color.Transparent)
                                    )
                                )
                            )
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = yAxisValueFormatter,
                    label = axisLabelComponent,
                    guideline = null
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = { _, value, _ -> getFormattedDate(value) },
                    label = axisLabelComponent,
                    guideline = null,
                    itemPlacer = remember {
                        HorizontalAxis.ItemPlacer.aligned(
                            // If you have 200 points, spacing = 5 is too crowded.
                            // This shows roughly 6-7 labels across the whole chart.
                            spacing = { if (points.size > 1) points.size / 6 else 1 },
                            addExtremeLabelPadding = false
                        )
                    }
                ),
                layerPadding = {
                    cartesianLayerPadding(0.dp, 0.dp)
                },
                marker = marker
            ),
            model = model,
            scrollState = rememberVicoScrollState(scrollEnabled = isFullScreen),
            zoomState = rememberVicoZoomState(zoomEnabled = isFullScreen),
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onZoomClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(
                imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = "Zoom",
                tint = Color.White
            )
        }
    }
}