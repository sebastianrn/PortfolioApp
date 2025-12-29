package dev.sebastianrn.portfolioapp.ui.components

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.ui.theme.Charcoal
import dev.sebastianrn.portfolioapp.ui.theme.GoldStart
import dev.sebastianrn.portfolioapp.ui.theme.TextWhite
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.overlayingComponent
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.cornered.Corner
import com.patrykandpatrick.vico.core.component.shape.cornered.MarkerCorneredShape
import com.patrykandpatrick.vico.core.marker.Marker

@Composable
internal fun rememberMarker(): Marker {
    val labelBackgroundColor = Charcoal.toArgb()
    val labelTextColor = TextWhite.toArgb()
    val indicatorInnerColor = GoldStart.toArgb()
    val guidelineColor = GoldStart.copy(alpha = 0.5f).toArgb()

    val labelBackground = remember(labelBackgroundColor) {
        ShapeComponent(
            shape = MarkerCorneredShape(Corner.FullyRounded),
            color = labelBackgroundColor
        ).setShadow(
            radius = 4f,
            dy = 2f,
            applyElevationOverlay = true,
        )
    }

    val label = textComponent(
        color = Color(labelTextColor),
        textSize = 12.sp,
        typeface = Typeface.DEFAULT_BOLD,
        padding = dimensionsOf(8.dp, 4.dp),
        background = labelBackground
    )

    val indicator = overlayingComponent(
        outer = shapeComponent(
            shape = Shapes.pillShape,
            color = Color.White.copy(alpha = 0.3f),
        ),
        inner = shapeComponent(
            shape = Shapes.pillShape,
            color = Color(indicatorInnerColor),
        ),
        innerPaddingAll = 2.dp,
    )

    val guideline = lineComponent(
        color = Color(guidelineColor),
        thickness = 2.dp,
        shape = DashedShape(
            shape = Shapes.pillShape,
            dashLengthDp = 4f,
            gapLengthDp = 4f,
        ),
    )

    return remember(label, indicator, guideline) {
        object : MarkerComponent(label, indicator, guideline) {
            init {
                indicatorSizeDp = 12f
                onApplyEntryColor = { /* Optional dynamic color */ }
            }
            // Removed getInsets() override -> Parent class handles it automatically!
        }
    }
}