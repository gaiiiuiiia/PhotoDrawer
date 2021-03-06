package com.example.photodrawer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.collections.ArrayList
import kotlin.math.abs

class PaintView (
    context: Context?,
    attrs: AttributeSet? = null)
    : View(context, attrs)
{
    private var mX = 0f
    private var mY = 0f
    private var path: Path? = null
    private val paint: Paint = Paint()
    private val paths = ArrayList<FingerPath>()
    private val stash = ArrayList<FingerPath>()
    private lateinit var bitmap: Bitmap
    private val bitmapPaint = Paint(Paint.DITHER_FLAG)
    private var strokeWidth = BRUSH_SIZE
    private var currentColor = DEFAULT_COLOR
    private var bgColor = DEFAULT_BG_COLOR
    private var destRect: Rect? = null
    private var isBitmapCreated = false

    /** прослушивает события кнопок Undo и Redo */
    private var undoRedoListener: UndoRedoListener? = null

    companion object
    {
        var BRUSH_SIZE = 10
        const val DEFAULT_COLOR = Color.BLACK
        const val DEFAULT_BG_COLOR = Color.WHITE
        private const val TOUCH_TOLERANCE = 4f
    }

    /**
     * Для того, чтобы корректно получить размеры области, так как во время инициализции, ее размеры равын нулю
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
    {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!isBitmapCreated && w > 0 && h > 0) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            isBitmapCreated = true
        }
    }

    init
    {
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = DEFAULT_COLOR
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.xfermode = null
        paint.alpha = 0xff
    }

    fun init(fileName: String? = null)
    {
        if (fileName != null) {
            val options = BitmapFactory.Options()
            options.inMutable = true
            bitmap = BitmapFactory.decodeFile(fileName, options)
            isBitmapCreated = true
        } else {
            if (width > 0 && height > 0) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                isBitmapCreated = true
            }
        }
        destRect = Rect(0, 0, width, height)
    }

    fun setListener(listener: UndoRedoListener)
    {
        undoRedoListener = listener
    }

    private fun notifyListener()
    {
        undoRedoListener?.simpleUndoRedoNotify(mapOf(
            "undoable" to (paths.size > 0),
            "redoable" to (stash.size > 0),
        ))
    }

    fun getPaths(): ArrayList<FingerPath>
    {
        return paths
    }

    fun getStash(): ArrayList<FingerPath>
    {
        return stash
    }

    fun undo()
    {
        if (paths.size > 0) {
            val lastElem = paths.last()
            stash.add(lastElem)
            paths.remove(lastElem)

            invalidate()
        }
    }

    fun redo()
    {
        if (stash.size > 0) {
            val lastElem = stash.last()
            paths.add(lastElem)
            stash.remove(lastElem)

            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas)
    {
        canvas.save()
        destRect?.apply {
            right = width
            bottom = height
        }
        destRect?.let {
            canvas.drawColor(bgColor)
            canvas.drawBitmap(bitmap, null, it, bitmapPaint)
        }
        for (fp in paths) {
            paint.color = fp.color
            paint.strokeWidth = fp.strokeWidth.toFloat()
            canvas.drawPath(fp.path, paint)
        }
        canvas.restore()
        notifyListener()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean
    {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
            }
        }
        invalidate()
        return true
    }

    private fun touchStart(x: Float, y: Float)
    {
        path = Path()
        path?.let {
            paths.add(FingerPath(currentColor, strokeWidth, it))
            it.reset()
            it.moveTo(x, y)
            mX = x
            mY = y
        }
    }

    private fun touchMove(x: Float, y: Float)
    {
        val dx = abs(x - mX)
        val dy = abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touchUp()
    {
        path!!.lineTo(mX, mY)
    }
}