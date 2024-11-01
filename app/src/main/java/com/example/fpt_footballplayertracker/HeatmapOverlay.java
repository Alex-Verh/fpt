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
            paint.setColor(Color.argb(10, 255, 0, 0));
            canvas.drawCircle(position.x, position.y, 100, paint);

            paint.setColor(Color.argb(30, 255, 0, 0));
            canvas.drawCircle(position.x, position.y, 70, paint);

            paint.setColor(Color.argb(60, 255, 0, 0));
            canvas.drawCircle(position.x, position.y, 50, paint);

            paint.setColor(Color.argb(100, 255, 0, 0));
            canvas.drawCircle(position.x, position.y, 30, paint);

            paint.setColor(Color.argb(50, 0, 255, 0));
            canvas.drawCircle(position.x, position.y, 90, paint);

            paint.setColor(Color.argb(10, 0, 255, 0));
            canvas.drawCircle(position.x, position.y, 50, paint);
        }
    }

    public void setPlayerPositions(List<PointF> positions) {
        this.playerPositions = positions;
        invalidate();
    }
}
