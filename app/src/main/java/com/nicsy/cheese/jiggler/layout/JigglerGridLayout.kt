package com.nicsy.cheese.jiggler.layout

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.nicsy.cheese.jiggler.R
import kotlin.math.cos
import kotlin.math.sin

class JigglerGridLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class JiggleMode {
        BASIC, ZIGZAG, CIRCLE, MICRO
    }

    enum class TileType {
        BASIC, GRID_COMPLEX, STRIPE_HORIZONTAL, STRIPE_DIAGONAL, DOT_PATTERN, WIDE_STRIPES
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var squareSize = 100f // 기본 셀 크기 (px)
    private var currentMode = JiggleMode.BASIC
    private var currentTileType = TileType.BASIC
    private var animator: ValueAnimator? = null

    // 누적 이동 거리 (시간에 따라 계속 증가만 함)
    private var scrollYDistance = 0f
    private var scrollXDistance = 0f

    /**
     * 지글러 시작
     */
    fun startJiggle(
        mode: JiggleMode = JiggleMode.BASIC,
        speedMultiplier: Float = 0.5f,
        tileType: TileType = TileType.BASIC
    ) {
        currentMode = mode
        currentTileType = tileType

        // 36dp를 픽셀로 변환
        val dp36 = 36f * context.resources.displayMetrics.density
        
        // 패턴 종류에 따라 기본 셀 크기 조정
        squareSize = when (tileType) {
            TileType.STRIPE_HORIZONTAL -> dp36 // 각 줄이 36dp
            TileType.WIDE_STRIPES -> dp36 * 2  // 더 굵은 패턴은 2배 (72dp)
            else -> 100f // 기본 바둑판 등
        }

        stopJiggle()

        // 0부터 1,000,000px 까지 끊김 없이 한 방향으로 계속 이동
        val maxDistance = 1_000_000f

        // 속도 계산 (1초당 이동할 pixel 거리)
        val pixelsPerSecond = 150f * speedMultiplier.coerceAtLeast(0.1f)
        val totalDuration = ((maxDistance / pixelsPerSecond) * 1000).toLong()

        animator = ValueAnimator.ofFloat(0f, maxDistance).apply {
            duration = totalDuration
            interpolator = LinearInterpolator() // 멈춤이나 가속 없이 일정하게 스크롤
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                val currentValue = animation.animatedValue as Float

                when (currentMode) {
                    JiggleMode.BASIC -> {
                        scrollYDistance = currentValue
                        scrollXDistance = 0f
                    }
                    JiggleMode.ZIGZAG -> {
                        scrollYDistance = currentValue
                        scrollXDistance = sin(currentValue / squareSize) * squareSize
                    }
                    JiggleMode.CIRCLE -> {
                        val angle = currentValue / squareSize
                        scrollXDistance = cos(angle) * squareSize
                        scrollYDistance = sin(angle) * squareSize
                    }
                    JiggleMode.MICRO -> {
                        // 미세 떨림: 아주 작은 범위에서 빠르게 진동
                        scrollYDistance = (currentValue % 10f) - 5f
                        scrollXDistance = (sin(currentValue) * 5f)
                    }
                }

                invalidate() // 화면 다시 그리기
            }
            start()
        }
    }

    fun stopJiggle() {
        animator?.cancel()
        animator = null
        scrollYDistance = 0f
        scrollXDistance = 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        if (w == 0f || h == 0f) return

        // 바둑판의 검은색/흰색 패턴 주기 (정사각형 2칸 = squareSize * 2)
        val patternUnit = squareSize * 2f

        // Y축 이동 거리를 패턴 주기로 나눈 나머지값만 추출하여 끊김 없는 연속 화면 구성
        val offsetY = scrollYDistance % patternUnit
        val offsetX = scrollXDistance % patternUnit

        // 화면 위쪽 밖에서부터 바둑판을 그리기 시작하여 빈 공간이 생기지 않도록 함
        val startY = -patternUnit + offsetY
        val startX = -patternUnit + offsetX

        var y = startY
        var row = 0

        while (y < h + patternUnit) {
            var x = startX
            var col = 0

            while (x < w + patternUnit) {
                drawTile(canvas, x, y, row, col)
                x += squareSize
                col++
            }
            y += squareSize
            row++
        }
    }

    private fun drawTile(canvas: Canvas, x: Float, y: Float, row: Int, col: Int) {
        val isBlack = (row + col) % 2 == 0
        // 테마에 따라 변하는 리소스 색상 사용
        val primaryColor = ContextCompat.getColor(context, if (isBlack) R.color.grid_primary else R.color.grid_secondary)
        val secondaryColor = ContextCompat.getColor(context, if (isBlack) R.color.grid_secondary else R.color.grid_primary)

        when (currentTileType) {
            TileType.BASIC -> {
                // 단순 바둑판 (흰/검)
                paint.color = primaryColor
                canvas.drawRect(x, y, x + squareSize, y + squareSize, paint)
            }
            TileType.GRID_COMPLEX -> {
                // 1. 기본 배경 사각형
                paint.color = primaryColor
                canvas.drawRect(x, y, x + squareSize, y + squareSize, paint)

                // 2. 고대비 마우스 추적용 세부 패턴 추가
                paint.color = secondaryColor

                // 중앙에 작은 점 (Dot)
                val centerX = x + squareSize / 2f
                val centerY = y + squareSize / 2f
                canvas.drawCircle(centerX, centerY, 8f, paint)

                // 십자선 (Crosshair)
                paint.strokeWidth = 2f
                canvas.drawLine(x + 10f, centerY, x + squareSize - 10f, centerY, paint)
                canvas.drawLine(centerX, y + 10f, centerX, y + squareSize - 10f, paint)

                if (!isBlack) {
                    canvas.drawRect(x, y, x + 15f, y + 15f, paint)
                    canvas.drawRect(
                        x + squareSize - 15f,
                        y + squareSize - 15f,
                        x + squareSize,
                        y + squareSize,
                        paint
                    )
                }
            }
            TileType.STRIPE_HORIZONTAL -> {
                // 가로 스트라이프: 현재 행(row)에 따라 검정/흰색 교차
                paint.color = if (row % 2 == 0) Color.BLACK else Color.WHITE
                canvas.drawRect(x, y, x + squareSize, y + squareSize, paint)
            }
            TileType.STRIPE_DIAGONAL -> {
                // 대각선 스트라이프
                paint.color = primaryColor
                canvas.drawRect(x, y, x + squareSize, y + squareSize, paint)
                
                paint.color = secondaryColor
                paint.strokeWidth = 10f
                canvas.drawLine(x, y, x + squareSize, y + squareSize, paint)
                canvas.drawLine(x, y + squareSize / 2f, x + squareSize / 2f, y + squareSize, paint)
                canvas.drawLine(x + squareSize / 2f, y, x + squareSize, y + squareSize / 2f, paint)
            }
            TileType.DOT_PATTERN -> {
                paint.color = primaryColor
                canvas.drawRect(x, y, x + squareSize, y + squareSize, paint)
                
                paint.color = secondaryColor
                val dotSpacing = squareSize / 3f
                for (i in 1..2) {
                    for (j in 1..2) {
                        canvas.drawCircle(x + i * dotSpacing, y + j * dotSpacing, 12f, paint)
                    }
                }
            }
            TileType.WIDE_STRIPES -> {
                // 와이드 가로 스트라이프: 36dp * 2 두께로 흑백 교차
                paint.color = if (row % 2 == 0) Color.BLACK else Color.WHITE
                canvas.drawRect(x, y, x + squareSize, y + squareSize, paint)
            }
        }
    }
}
