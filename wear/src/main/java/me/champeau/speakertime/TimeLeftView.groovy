package me.champeau.speakertime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import groovy.transform.CompileStatic

@CompileStatic
class TimeLeftView extends View {

    private Paint circlePaint
    private Paint elapsedTimePaint
    private float cx, cy, radius

    int elapsedPercent = 0

    TimeLeftView(Context context) {
        super(context)
        init()
    }

    TimeLeftView(Context context, AttributeSet attrs) {
        super(context, attrs)
        init()
    }

    TimeLeftView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr)
        init()
    }

    public void setElapsedPercent(int perc) {
        elapsedPercent = perc
        invalidate()
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh)
        float xpad = (float) (paddingLeft + paddingRight)
        float ypad = (float) (paddingTop + paddingBottom)
        float ww = (float) w - xpad
        float hh = (float) h - ypad
        radius = (float) Math.min(ww, hh)/2f
        cx = (float) ww/2f
        cy = (float) hh/2f

    }

    private void init() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG)
        elapsedTimePaint = new Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint.color = Color.rgb(0, 153, 0)
        circlePaint.strokeWidth = 24f
        circlePaint.style = Paint.Style.STROKE
        elapsedTimePaint.color = Color.RED
        elapsedTimePaint.strokeWidth = 48f
        elapsedTimePaint.style = Paint.Style.STROKE
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(cx, cy, radius, circlePaint)
        def rect = new RectF(0,0,width, height)
        canvas.drawArc(rect, -90f, (float) 3.6f*elapsedPercent, false, elapsedTimePaint)
    }
}