package dev.sebastianrn.portfolioapp.ui.components

import android.text.Layout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
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
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
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
fun PortfolioChart(points: List<Pair<Long, Double>>) {

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

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
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
                        spacing = { 5 },
                        addExtremeLabelPadding = true
                    )
                }
            ),
            marker = marker
        ),
        model = model,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}