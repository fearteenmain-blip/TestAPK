package com.example.testapk

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Устанавливаем наш кастомный View (игровое поле)
        setContentView(GameView(this))
    }
}

// Этот класс отвечает за всё: и за логику, и за отрисовку
class GameView(context: Context) : View(context) {

    // Размер поля (7x7)
    private val gridSize = 7
    private var cellSize = 0f

    // Кисти для рисования
    private val gridPaint = Paint().apply {
        color = Color.argb(50, 255, 0, 255) // Полупрозрачный розовый
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val linePaint = Paint().apply {
        strokeWidth = 15f // Толщина провода
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private val endpointPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Данные для теста (в реальности будут генерироваться)
    private val flows = mapOf(
        Color.RED to listOf(0 to 0, 6 to 6),
        Color.CYAN to listOf(0 to 6, 6 to 0),
        Color.YELLOW to listOf(3 to 3, 3 to 5)
    )

    // Состояние текущего пути
    private var selectedColor: Int? = null
    private var currentPath = mutableListOf<Pair<Int, Int>>()
    private val paths = mutableMapOf<Int, List<Pair<Int, Int>>>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Вычисляем размер клетки, чтобы поле поместилось на экране
        cellSize = Math.min(w, h) / (gridSize + 1).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Рисуем сетку
        for (i in 0..gridSize) {
            val pos = i * cellSize + cellSize / 2
            canvas.drawLine(cellSize / 2, pos, gridSize * cellSize + cellSize / 2, pos, gridPaint)
            canvas.drawLine(pos, cellSize / 2, pos, gridSize * cellSize + cellSize / 2, gridPaint)
        }

        // 2. Рисуем готовые пути (провода)
        for ((color, path) in paths) {
            linePaint.color = color
            for (i in 0 until path.size - 1) {
                val (r1, c1) = path[i]
                val (r2, c2) = path[i + 1]
                canvas.drawLine(
                    c1 * cellSize + cellSize / 2,
                    r1 * cellSize + cellSize / 2,
                    c2 * cellSize + cellSize / 2,
                    r2 * cellSize + cellSize / 2,
                    linePaint
                )
            }
        }

        // 3. Рисуем текущий путь, который пользователь тянет прямо сейчас
        if (currentPath.size > 1) {
            linePaint.color = selectedColor ?: Color.WHITE
            for (i in 0 until currentPath.size - 1) {
                val (r1, c1) = currentPath[i]
                val (r2, c2) = currentPath[i + 1]
                canvas.drawLine(
                    c1 * cellSize + cellSize / 2,
                    r1 * cellSize + cellSize / 2,
                    c2 * cellSize + cellSize / 2,
                    r2 * cellSize + cellSize / 2,
                    linePaint
                )
            }
        }

        // 4. Рисуем точки (разъёмы)
        for ((color, endpoints) in flows) {
            endpointPaint.color = color
            for ((r, c) in endpoints) {
                canvas.drawCircle(
                    c * cellSize + cellSize / 2,
                    r * cellSize + cellSize / 2,
                    cellSize / 3f, // Радиус точки
                    endpointPaint
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Переводим координаты касания в координаты сетки
        val col = (event.x / cellSize).toInt()
        val row = (event.y / cellSize).toInt()

        if (row !in 0 until gridSize || col !in 0 until gridSize) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Проверяем, попали ли мы в какую-нибудь точку (разъём)
                for ((color, endpoints) in flows) {
                    if (endpoints.contains(row to col)) {
                        selectedColor = color
                        currentPath.clear()
                        currentPath.add(row to col)
                        invalidate() // Перерисовать экран
                        return true
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // Если мы тянем линию, добавляем новые клетки
                if (selectedColor != null) {
                    val last = currentPath.lastOrNull() ?: return false
                    if (last == (row to col)) return true

                    // Проверяем, что двигаемся в соседнюю клетку (по горизонтали или вертикали)
                    val dr = Math.abs(row - last.first)
                    val dc = Math.abs(col - last.second)
                    if (dr + dc == 1) {
                        // Проверяем, не занята ли клетка другим цветом
                        if (isCellAvailable(row, col, selectedColor!!)) {
                            currentPath.add(row to col)
                            
                            // Проверяем, не дошли ли до конца
                            val endpoints = flows[selectedColor]
                            if (endpoints != null && endpoints.contains(row to col) && currentPath.size > 1) {
                                // Путь завершен!
                                paths[selectedColor!!] = currentPath.toList()
                                selectedColor = null
                                currentPath.clear()
                            }
                            invalidate()
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Отпустили палец — сбрасываем текущий путь
                selectedColor = null
                currentPath.clear()
                invalidate()
            }
        }
        return true
    }

    // Проверка, свободна ли клетка для рисования
    private fun isCellAvailable(row: Int, col: Int, color: Int): Boolean {
        // Нельзя наступать на чужие точки
        for ((c, endpoints) in flows) {
            if (c != color && endpoints.contains(row to col)) return false
        }
        // Нельзя наступать на уже нарисованные пути
        for ((_, path) in paths) {
            if (path.contains(row to col)) return false
        }
        // Нельзя пересекать самого себя
        return !currentPath.contains(row to col)
    }
}
