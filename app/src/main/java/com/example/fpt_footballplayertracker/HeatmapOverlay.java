package com.example.fpt_footballplayertracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class HeatmapOverlay extends View {
    private List<PointF> playerPositions;
    private Paint paint;

    public HeatmapOverlay(Context context, List<PointF> playerPositions) {
        super(context);
        init(playerPositions);
    }

    public HeatmapOverlay(Context context, AttributeSet attrs, List<PointF> playerPositions) {
        super(context, attrs);
        init(playerPositions);
    }

    private void init(List<PointF> playerPositions) {
        this.playerPositions = playerPositions;
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (PointF position : playerPositions) {
            int density = calculateDensity(position);
            int colorIntensity = Math.min(255, density * 20);

            if (density > 5) {
                paint.setColor(Color.argb(colorIntensity, 255, 0, 0));
            } else {
                paint.setColor(Color.argb(colorIntensity / 2, 0, 255, 0));
            }

            for (int i = 3; i >= 1; i--) {
                paint.setAlpha(colorIntensity / i);
                canvas.drawCircle(position.x, position.y, 30 * i, paint);
            }
        }
    }

    private int calculateDensity(PointF position) {
        int density = 0;
        for (PointF otherPosition : playerPositions) {
            if (otherPosition != position && distance(position, otherPosition) < 100) {
                density++;
            }
        }
        return density;
    }

    private float distance(PointF p1, PointF p2) {
        return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    public void setPlayerPositions(List<PointF> positions) {
        this.playerPositions = positions;
        invalidate();
    }
}
