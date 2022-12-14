/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ocr_play.util

import android.graphics.*
import android.util.Log
import com.example.ocr_play.util.GraphicOverlay.Graphic
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.Line
import kotlin.math.max
import kotlin.math.min

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
class TextGraphic
constructor(
    overlay: GraphicOverlay?,
    private val text: Text,
    private val shouldGroupTextInBlocks: Boolean,
    private val showLanguageTag: Boolean,
    private val showConfidence: Boolean
) : Graphic(overlay!!) {

    private val rectPaint: Paint = Paint()
    private val textPaint: Paint
    private val labelPaint: Paint

    init {
        rectPaint.color = MARKER_COLOR
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = STROKE_WIDTH
        textPaint = Paint()
        textPaint.color = TEXT_COLOR
        textPaint.textSize = TEXT_SIZE
        labelPaint = Paint()
        labelPaint.color = MARKER_COLOR
        labelPaint.style = Paint.Style.FILL
        // Redraw the overlay, as this graphic has been added.
        postInvalidate()
    }

    /** Draws the text block annotations for position, size, and raw value on the supplied canvas. */
    override fun draw(canvas: Canvas?) {
        if(canvas == null) return
        var line: Line?
        var foundAll: Boolean = true
        var color: Int = TEXT_COLOR
        line = findValue("Volume", text)
        if(line != null){
            drawLine("Volume", line, Color.GREEN, canvas)
        } else {
            foundAll = false
        }
        line = findValue("Compliance", text)
        if(line != null){
            drawLine("Compliance", line, Color.RED, canvas)
        } else {
            foundAll = false
        }
        line = findValue("Pressure", text)
        if(line != null){
            drawLine("Pressure", line, Color.YELLOW, canvas)
        } else {
            foundAll = false
        }
        line = findValue("Gradient", text)
        if(line != null){
            drawLine("Gradient", line, Color.MAGENTA, canvas)
        } else {
            foundAll = false
        }
        if(foundAll){
            Log.d(TAG, "All blocks found")
        }
    }

    private fun drawLine(label: String, line: Line, color: Int, canvas: Canvas){
        val rect = RectF(line.boundingBox)
        drawText(
            getFormattedText(line.text, label),
            rect,
            TEXT_SIZE + 2 * STROKE_WIDTH,
            canvas,
            color
        )
    }

    private fun findValue(target: String, text: Text): Line? {
        for(textBlock in text.textBlocks){
            for(line in textBlock.lines){
                if(line.text == target){
                    var minY: Int = getMinY(line.cornerPoints!!)
                    var maxY: Int = getMaxY(line.cornerPoints!!)
                    return findValueInYRange(text, line, minY, maxY)
                }
            }
        }
        return null
    }

    private fun getMinY(points: Array<Point>): Int {
        var minY = Int.MAX_VALUE
        for(point in points){
            if(point.y < minY){
                minY = point.y
            }
        }
        return minY
    }

    private fun getMaxY(points: Array<Point>): Int {
        var maxY = Int.MIN_VALUE
        for(point in points){
            if(point.y > maxY){
                maxY = point.y
            }
        }
        return maxY
    }

    private fun findValueInYRange(text: Text, ignore: Line, minY: Int, maxY: Int): Line? {
        for(textBlock in text.textBlocks){
            for(line in textBlock.lines){
                if(line.text != ignore.text){
                    var midY: Float = getMidY(line.cornerPoints!!)
                    if(midY > minY && midY < maxY){
                        return line
                    }
                }
            }
        }
        return null
    }

    private fun getMidY(points: Array<Point>): Float {
        var total: Int = 0
        for(point in points){
            total += point.y
        }
        return total / 4.0f
    }

    private fun getFormattedText(text: String, label: String): String {
        return String.format("%s\n%s", label, text)
    }

    private fun drawText(text: String, rect: RectF, textHeight: Float, canvas: Canvas, color: Int) {
        // If the image is flipped, the left will be translated to right, and the right to left.
        textPaint.color = color;
        rectPaint.color = color;
        val x0 = translateX(rect.left)
        val x1 = translateX(rect.right)
        rect.left = min(x0, x1)
        rect.right = max(x0, x1)
        rect.top = translateY(rect.top)
        rect.bottom = translateY(rect.bottom)
        canvas.drawRect(rect, rectPaint)
        val textWidth = textPaint.measureText(text)
        canvas.drawRect(
            /* left = */ rect.left - STROKE_WIDTH,
            /* top = */ rect.top - textHeight,
            /* right = */ rect.left + textWidth + 2 * STROKE_WIDTH,
            /* bottom = */ rect.top,
            /* paint = */ labelPaint
        )
        // Renders the text at the bottom of the box.
        canvas.drawText(text, rect.left, rect.top - STROKE_WIDTH, textPaint)
    }

    companion object {
        private const val TAG = "TextGraphic"
        private const val TEXT_WITH_LANGUAGE_TAG_FORMAT = "%s:%s"
        private const val TEXT_COLOR = Color.BLACK
        private const val MARKER_COLOR = Color.WHITE
        private const val TEXT_SIZE = 24.0f
        private const val STROKE_WIDTH = 2.0f
    }
}
