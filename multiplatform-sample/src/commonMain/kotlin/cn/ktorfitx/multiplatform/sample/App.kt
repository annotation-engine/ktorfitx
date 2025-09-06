package cn.ktorfitx.multiplatform.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.ktorfitx.multiplatform.sample.generated.resources.Res
import cn.ktorfitx.multiplatform.sample.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import kotlin.math.sqrt

@Composable
fun App() {
	BoxWithConstraints(
		modifier = Modifier
			.fillMaxSize()
			.background(Color(0xFF0B0F1A)),
		contentAlignment = Alignment.Center
	) {
		ParticleInteractiveBackground()
		val scale by remember(this.maxWidth, this.maxHeight) {
			derivedStateOf {
				val scale = this.maxWidth / this.maxHeight
				if (scale > 350f / 500f) {
					this.maxHeight / 500.dp
				} else {
					this.maxWidth / 350.dp
				}
			}
		}
		Box(
			modifier = Modifier
				.scale(scale)
				.size(350.dp, 500.dp)
				.padding(24.dp)
		) {
			CanvasBackgroundLines()
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(top = 24.dp, bottom = 12.dp),
				verticalArrangement = Arrangement.SpaceBetween
			) {
				Column(
					verticalArrangement = Arrangement.spacedBy(16.dp),
					horizontalAlignment = Alignment.Start
				) {
					Image(
						painter = painterResource(Res.drawable.logo),
						contentDescription = "Logo",
						modifier = Modifier.size(48.dp)
					)
					
					Row(
						verticalAlignment = Alignment.Bottom
					) {
						Text(
							text = "KTORFIT X",
							fontSize = 30.sp,
							fontWeight = FontWeight.Black,
							color = Color(0xFFE3E8F0),
							letterSpacing = 1.4.sp
						)
						Spacer(Modifier.width(12.dp))
						Text(
							text = "v3.2.3-3.1.0",
							fontSize = 14.sp,
							fontWeight = FontWeight.Normal,
							color = Color(0xFFB0BFD9),
							letterSpacing = 1.sp
						)
					}
					
					Text(
						text = "Annotation Engine for Ktor",
						fontSize = 14.sp,
						fontWeight = FontWeight.Medium,
						color = Color(0xFF9BA9C0),
						letterSpacing = 1.1.sp
					)
				}
				
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.background(Color(0xBB171E2E), shape = RoundedCornerShape(12.dp))
						.border(
							width = 1.dp,
							color = Color(0xFF38425B),
							shape = RoundedCornerShape(12.dp)
						)
						.padding(16.dp),
					verticalArrangement = Arrangement.spacedBy(12.dp)
				) {
					LibVersionRow("kotlin", "v2.2.10")
					LibVersionRow("ktor", "v3.2.3")
					LibVersionRow("ksp", "v2.2.10-2.0.2")
				}
				
				Text(
					text = "Apache License 2.0",
					fontSize = 11.sp,
					color = Color(0xFF7A87A4)
				)
			}
		}
	}
}

@Composable
fun LibVersionRow(name: String, version: String) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = "• $name",
			color = Color.White,
			fontWeight = FontWeight.Normal,
			fontSize = 14.sp
		)
		Spacer(modifier = Modifier.weight(1f))
		Text(
			text = version,
			color = Color.White,
			fontWeight = FontWeight.Light,
			fontSize = 13.sp
		)
	}
}

@Composable
fun CanvasBackgroundLines() {
	Canvas(modifier = Modifier.fillMaxSize()) {
		val strokeColor = Color(0x992B3A52)
		val strokeWidth = 1.dp.toPx()
		
		drawLine(
			color = strokeColor,
			start = Offset(0f, size.height * 0.2f),
			end = Offset(size.width, size.height * 0.1f),
			strokeWidth = strokeWidth
		)
		drawLine(
			color = strokeColor,
			start = Offset(0f, size.height * 0.45f),
			end = Offset(size.width, size.height * 0.25f),
			strokeWidth = strokeWidth
		)
		
		drawCircle(
			color = strokeColor,
			center = Offset(size.width * 0.3f, size.height * 0.15f),
			radius = 18.dp.toPx(),
			style = Stroke(width = strokeWidth)
		)
		
		val trianglePath1 = Path().apply {
			moveTo(size.width * 0.4f, size.height * 0.34f)
			lineTo(size.width * 0.45f, size.height * 0.4f)
			lineTo(size.width * 0.35f, size.height * 0.4f)
			close()
		}
		drawPath(
			path = trianglePath1,
			color = strokeColor,
			style = Stroke(width = strokeWidth)
		)
		
		drawRect(
			color = strokeColor,
			topLeft = Offset(size.width * 0.65f, size.height * 0.15f),
			size = Size(24.dp.toPx(), 24.dp.toPx()),
			style = Stroke(width = strokeWidth)
		)
		
		drawRect(
			color = strokeColor,
			topLeft = Offset(size.width * 0.75f, size.height * 0.45f),
			size = Size(24.dp.toPx(), 24.dp.toPx()),
			style = Stroke(width = strokeWidth)
		)
		
		drawCircle(
			color = strokeColor,
			center = Offset(size.width * 0.75f, size.height * 0.9f),
			radius = 18.dp.toPx(),
			style = Stroke(width = strokeWidth)
		)
		
		val trianglePath2 = Path().apply {
			moveTo(size.width * 0.2f, size.height * 0.79f)
			lineTo(size.width * 0.25f, size.height * 0.85f)
			lineTo(size.width * 0.15f, size.height * 0.85f)
			close()
		}
		drawPath(
			path = trianglePath2,
			color = strokeColor,
			style = Stroke(width = strokeWidth)
		)
		
		drawLine(
			color = strokeColor,
			start = Offset(0f, size.height * 0.9f),
			end = Offset(size.width, size.height * 0.9f),
			strokeWidth = strokeWidth
		)
	}
}

@Composable
fun ParticleInteractiveBackground() {
	BoxWithConstraints {
		val density = LocalDensity.current
		val width = this.maxWidth
		val height = this.maxHeight
		val particles = remember(width, height) {
			with(density) {
				generateHexagonalParticles(width.toPx(), height.toPx(), 8.dp.toPx())
			}
		}
		var mousePosition by remember { mutableStateOf(Offset.Unspecified) }
		
		MouseTrackingBox { mousePosition = it }
		
		Canvas(
			modifier = Modifier.fillMaxSize()
		) {
			particles.forEach { particle ->
				val distance = if (mousePosition.isSpecified) {
					(particle.position - mousePosition).getDistance()
				} else Float.MAX_VALUE
				
				val influence = (1f - (distance / 150f).coerceIn(0f, 1f))
				
				drawCircle(
					color = Color(0xFF3DA9FC).copy(alpha = 0.1f + influence * 0.9f),
					radius = 2f + influence * 4f,
					center = particle.position
				)
			}
		}
	}
}

data class Particle(val position: Offset)

fun generateHexagonalParticles(
	width: Float,
	height: Float,
	radius: Float,
): List<Particle> {
	val particles = mutableListOf<Particle>()
	
	val dx = radius * 2f
	val dy = sqrt(3f) * radius
	
	var y = 0f
	var row = 0
	
	while (y < height + dy) {
		val offsetX = if (row % 2 == 0) 0f else dx / 2f
		var x = offsetX
		
		while (x < width + dx) {
			particles.add(Particle(Offset(x, y)))
			x += dx
		}
		y += dy * 0.5f
		row++
	}
	
	return particles
}

@Composable
fun MouseTrackingBox(
	onMove: (Offset) -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.pointerInput(Unit) {
				awaitPointerEventScope {
					while (true) {
						val event = awaitPointerEvent()
						val position = event.changes.firstOrNull()?.position
						if (position != null) {
							onMove(position)
						}
					}
				}
			}
	)
}