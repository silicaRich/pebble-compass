package pebblecompass.example.app;

    import android.content.Context;
    import android.graphics.Canvas;
    import android.graphics.Color;
    import android.graphics.Paint;
    import android.view.View;

    public class CompassView extends View {

        private Paint paint;
        private float position = 0;
        String charDir="";

        public CompassView(Context context) {
            super(context);
            init();
        }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setTextSize(30
        );
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLUE);
    }

    // CompassView will use the canvas to draw itself.
    // Canvas class has methods for drawing text, lines etc
    @Override
    protected void onDraw(Canvas canvas) {
        int xPoint = getMeasuredWidth() / 2;
        int yPoint = getMeasuredHeight() / 2;

        float radius = (float) (Math.max(xPoint, yPoint) * 0.6);
        canvas.drawCircle(xPoint, yPoint, radius, paint);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);

        // 3.143 is a good approximation for the circle
        canvas.drawLine(xPoint,
        yPoint,
        (float) (xPoint + radius
        * Math.sin((double) (-position) / 180 * 3.143)),
        (float) (yPoint - radius
        * Math.cos((double) (-position) / 180 * 3.143)), paint);

        canvas.drawText("N",(float) (xPoint + radius
                * Math.sin((double) (-position) / 180 * 3.143)),
                (float) (yPoint - radius
                        * Math.cos((double) (-position) / 180 * 3.143)), paint);
        //canvas.drawText(String.valueOf(position), xPoint, yPoint, paint);
        canvas.drawText("You are facing "+position, 60, 100, paint);
        canvas.drawText("degrees from magnetic north.", 60, 150, paint);
        canvas.drawText("I.e. You are facing "+charDir, 60, 200, paint);
     //   canvas.drawText("Open your compass watchapp if you have not done so yet.", 60, 350, paint);

        }

    public void updateData(float position) {
        this.position = position;
        invalidate();
    }

}