package com.easy.easyDrwaing.View

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attr: AttributeSet) : View(context, attr) {

    private var mDrawPath: CustomViewPath? = null
    private var mBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.toFloat()
    private var mColor = Color.BLACK
    private var mCanvas: Canvas? = null
    private var mPathArrayList = ArrayList<CustomViewPath>()
    private var mUndoPath = ArrayList<CustomViewPath>()

    init {
        setUpDrawingView()
    }

    fun onRedoClick() {
        if (mUndoPath.size > 0) {
            mPathArrayList.add(mUndoPath.removeAt(mUndoPath.size - 1))
            invalidate()
        }
    }

    fun onUndoClick() {
        if (mPathArrayList.size > 0) {
            mUndoPath.add(mPathArrayList.removeAt(mPathArrayList.size - 1))
            invalidate()
        }
    }

    fun onAllClear() {
        if (mPathArrayList.size > 0) {
            mPathArrayList.clear()
            invalidate()
        }
    }

    private fun setUpDrawingView() {
        mDrawPaint = Paint()
        mDrawPath = CustomViewPath(mColor, mBrushSize)
        mDrawPaint!!.color = mColor
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 15.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mBitmap!!, 0f, 0f, mCanvasPaint)

        for (path in mPathArrayList) {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = mColor
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_MOVE -> {
                mDrawPath!!.lineTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_UP -> {
                mPathArrayList.add(mDrawPath!!)
                mDrawPath = CustomViewPath(mColor, mBrushSize)
            }
            else -> return false

        }
        invalidate()
        return true
    }

    fun setBrushSize(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setBrushColor(newColor: String) {
        mColor = Color.parseColor(newColor)
        mDrawPaint!!.color = mColor
    }

    internal inner class CustomViewPath(var color: Int, var brushThickness: Float) :
        android.graphics.Path()
}