package edu.madcourse.circletouch;
import java.util.ArrayList;
import java.util.List;

import edu.madcourse.circletouch.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Scroller;

public class circle extends View {
  private String TAG = "circletouch.circle";
  // view boundary for the circle.
  private RectF mCircleBounds = new RectF();
  private List<Point> mPoints = new ArrayList<Point>();

  // circle positions
  private float mCircleX;
  private float mCircleY;
  private float mCircleRadius;

  // touchPoint info
  private int mTouchPointRadius;
  private int mTouchPointColor;

  // gesture detection
  private Scroller mScroller;
  private GestureDetector mGestureDetector;


  //paints
  private Paint mCirclePaint;
  private Paint mTouchPointPaint;

  private Context mContext;

  public circle(Context c) {
    super(c);
    init();
    // grab the typedarray from attrs
    mContext = c;
  }
  public circle(Context ctx, AttributeSet attrs) {
    super(ctx, attrs);

    // attrs contains the raw values for the XML attributes
    // that were specified in the layout, which don't include
    // attributes set by styles or themes, and which may have
    // unresolved references. Call obtainStyledAttributes()
    // to get the final values for each attribute.
    //
    // This call uses R.styleable.PieChart, which is an array of
    // the custom attributes that were declared in attrs.xml.
    TypedArray a = ctx.getTheme().obtainStyledAttributes(
        attrs,
        R.styleable.circle,
        0, 0);

    try {
      // resolve values from the typedarray and store into fields
      mTouchPointRadius = 
        a.getInteger(R.styleable.circle_touchPointRadius, 40);
      mTouchPointColor = 
        a.getInteger(R.styleable.circle_touchPointColor,0xffff0000); 

      // mTextWidth = a.getDimension(R.styleable.PieChart_labelWidth,
      // 0.0f);
    } finally {
      // release TypedArray
      a.recycle();

      Log.d(TAG,
          "mTouchPointRadius is: " + mTouchPointRadius);
      Log.d(TAG, 
          "mTouchPointColor is: " + mTouchPointColor);
    }
    init();

  }

  /**
   * Called when the size of the screen is changed; we describe the size of
   * the circle and it's boundary area here.
   */
  public void onSizeChanged(int w, int h, int oldw, int oldh) {
    float xpad = (float) (getPaddingLeft() + getPaddingRight());
    float ypad = (float) (getPaddingTop() + getPaddingBottom());

    //figure out the "correct" width and height
    float ww = (float) w - xpad;
    float hh = (float) h - ypad;

    // let's make the circle as large as it can be
    float diameter = (float) Math.min(ww, hh);

    // make the rectf for the boundary
    mCircleBounds = new RectF(
        0.0f,
        0.0f,
        diameter,
        diameter);

    // offset the boundary rect in accordance with the padding
    mCircleBounds.offsetTo(getPaddingLeft(), getPaddingTop());

    // calculate the circle's coordinates and stuff based on the boundary
    mCircleRadius = diameter/2f;
    mCircleX      = mCircleBounds.left + mCircleBounds.width()/2;
    mCircleY      = mCircleBounds.top + mCircleBounds.height()/2;

    // if the touchpoints are gonna go out of the padding, fix it.
    float farthest = mTouchPointRadius + diameter;
    if (farthest > w - xpad/2 || farthest > h-ypad/2) {
      mTouchPointRadius = (int)Math.min(xpad, ypad)/4;
      Log.d(TAG, 
          "Touchpoints are a little big. reducing to: " +
          mTouchPointRadius);

    }
  }

  /**
   * Draws the view
   */
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);



    for (Point point : mPoints) {
      PointF touchPointCoords = degreesToPointF(point.mDegrees);

      Log.d(TAG, 
          "adding a circle with coords: " +
          touchPointCoords.x +
          ", " +
          touchPointCoords.y);

      Log.d(TAG,
          "and radius: " + mTouchPointRadius);

      canvas.drawCircle(
          touchPointCoords.x,
          touchPointCoords.y,
          mTouchPointRadius,
          mTouchPointPaint
          );
    }

    Log.d(TAG, 
        "drawing the large circle with radius: " +
        mCircleRadius +
        "and x, y: " +
        +mCircleX + 
        ", " +
        mCircleY);

    // drawing the circle
    canvas.drawCircle(
        mCircleX,
        mCircleY,
        mCircleRadius,
        mCirclePaint
        );

  }

  /**
   * converts a degree value to a coordinate on the edge of the circle
   */
  private PointF degreesToPointF(int theta) {
    float y = (float)
      (mCircleY +
       mCircleRadius *
       Math.cos( (theta*Math.PI) / 180f )
      );
    float x = (float)
      (mCircleX +
       mCircleRadius *
       Math.sin( (theta * Math.PI) / 180f )
      );
    Log.d(TAG,
        theta +
        " degrees is " +
        x +
        ", " +
        y);

    return new PointF(x, y);
  }

  /**
   * converts a coordinate on the edge of the circle to a degree value
   */
  private int pointFtoDegrees(PointF coords) {
    return (int) Math.atan2( coords.y - mCircleY, coords.x - mCircleX);
  }
  /**
   * converts a coordinate on the edge of the circle to a degree value
   */
  private int coordsToDegrees(float x, float y) {
    return (int) Math.atan2( y - mCircleY, x - mCircleX);
  }

  /**
   * determines the number of degrees between two points using the circle's
   * center point.
   */
  public double angleBetween2Lines(float x1, float y1, float x2, float y2) {
    double angle1 = coordsToDegrees(x1, y1);
    double angle2 = coordsToDegrees(x2, y2);
    return angle1 - angle2;
  }

  private void init() {

    // set up the circle paint
    mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mCirclePaint.setStyle(Paint.Style.STROKE);

    // set up the touchpoint paint
    mTouchPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTouchPointPaint.setStyle(Paint.Style.FILL);
    mTouchPointPaint.setColor(mTouchPointColor);

    // add some touch points
    addItem(0);
    addItem(90);
    addItem(270);
    invalidate();
  }

  private void addItem(int degrees) {
    // create a new point
    Point p = new Point();
    p.mDegrees = degrees;

    // add it to the list of points
    mPoints.add(p);

    // alert that data has been changed.
  }

  /**
   * Container for touch points
   */
  private class Point {
    public int mDegrees;
  }
}
