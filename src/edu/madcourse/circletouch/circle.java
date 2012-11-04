package edu.madcourse.circletouch;
import java.util.ArrayList;
import java.util.List;

import edu.madcourse.circletouch.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


//TODO
// Make dragging dots easier - fast movements can screw it up very easily
// Add the arcs in for different "food groups"
// Make the dots "move" each other or stop moving when we hit another dot
// Add a selection box on the bottom for the different food groups
// Make it PRETTY


public class circle extends View {
  private String TAG = "circletouch.circle";
  // view boundary for the circle.
  private RectF mCircleBounds = new RectF();
  private List<TouchPoint> mPoints = new ArrayList<TouchPoint>();

  // circle positions
  private float mCircleX;
  private float mCircleY;
  private float mCircleRadius;

  // touchPoint info
  private int mTouchPointRadius;
  private int mTouchPointColor;

  // gesture detection
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

    mContext = ctx;

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

      Log.d(TAG, "90 degrees in coords is: " +
          degreesToPointF(90).x +
          ", " +
          degreesToPointF(90).y);

      Log.d(TAG, "those points in degrees is: " +
          pointFtoDegrees(degreesToPointF(90)));    }
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
      Log.d(TAG, "mCircle center is: " +mCircleX +", "+mCircleY);

    }
  }

  /**
   * Draws the view
   */
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    for (TouchPoint point : mPoints) {
      PointF touchPointCoords = degreesToPointF(point.mDegrees);

      // draw the touchPoint on the canvas
      canvas.drawCircle(
          touchPointCoords.x,
          touchPointCoords.y,
          mTouchPointRadius,
          mTouchPointPaint
          );
    }

    // drawing the circle
    canvas.drawCircle(
        mCircleX,
        mCircleY,
        mCircleRadius,
        mCirclePaint
        );

  }

  /**
   * doing stuff with touch
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    Log.d(TAG, "ONTOUCHEVENT | received a touchEvent");
    boolean result = mGestureDetector.onTouchEvent(event);
    return result;

  }

  /**
   * converts a degree value to a coordinate on the edge of the circle
   */
  private PointF degreesToPointF(double theta) {
    // had to play around with this a little to get what I wanted 
    float y = (float)
      (mCircleY -
       (mCircleRadius *
       Math.cos( (theta*Math.PI) / 180f ))
      );
    float x = (float)
      (mCircleX +
       (mCircleRadius *
       Math.sin( (theta * Math.PI) / 180f ))
      );
    return new PointF(x, y);
  }

  // I have no idea why I have to add 90 to these calculations but I do
  /**
   * converts a coordinate on the edge of the circle to a degree value
   */
  private double pointFtoDegrees(PointF coords) {
    return (double)90+ Math.toDegrees(Math.atan2( coords.y - mCircleY, coords.x - mCircleX));
  }
  /**
   * converts a coordinate on the edge of the circle to a degree value
   */
  private double coordsToDegrees(float x, float y) {
    return 90 + Math.toDegrees( Math.atan2( y - mCircleY, x - mCircleX));
  }

  /**
   * determines the number of degrees between two points using the circle's
   * center point.
   */
  public int degreesMovedBetweenPoints(
      float x1, float y1, float x2, float y2) {

    double angle1 = coordsToDegrees(x1, y1);
    double angle2 = coordsToDegrees(x2, y2);
    return (int) (angle1 - angle2);
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
    addItem(180);
    addItem(90);
    addItem(270);

    // set up the gesture detector
    mGestureDetector = new GestureDetector(
        circle.this.getContext(), 
        new GestureListener());

    // Turn off long press--this control doesn't use it, and if long press is
    // enabled, you can't scroll for a bit, pause, then scroll some more 
    // (the pause is interpreted as a long press, apparently)
    mGestureDetector.setIsLongpressEnabled(false);

    invalidate();
  }

  private void addItem(int degrees) {
    // create a new point
    TouchPoint p = new TouchPoint();
    p.mDegrees = degrees;

    // add it to the list of points
    mPoints.add(p);

    // alert that data has been changed.
  }

  /**
   * Container for touch points
   */
  private class TouchPoint {
    public double mDegrees;
  }

  /**
   * returns true if the given x and y coords are inside of the point p
   */
  private boolean isTouchingThisPoint(float x, float y, TouchPoint p) {
    PointF pCoords = degreesToPointF((double)p.mDegrees);
    double dist = Math.sqrt( 
        Math.pow( (double)pCoords.x - x, 2) +
        Math.pow( (double)pCoords.y - y, 2));
    // make it * 2 to give users a little breathing room(the dots are small)
    return dist <= mTouchPointRadius*2;
  }

  /**
   * return true if we removed a point that contained the given coords, false
   * otherwise
   */
  private boolean removePoint(float x, float y) {
    for (int i = 0; i< mPoints.size(); i++) {
      if (isTouchingThisPoint(x, y, mPoints.get(i))) {
        mPoints.remove(i);
        return true;
      }
    }
    return false;
  }

  /**
   * let's track some gestures
   */
  private class GestureListener extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onScroll(
        MotionEvent e1,
        MotionEvent e2,
        float distanceX,
        float distanceY) {

      // calculate the last point in the scroll
      float lastX = e2.getX() - distanceX;
      float lastY = e2.getY() - distanceY;

      Log.d(TAG, "SCROLL | last point in the scroll was: " +
          lastX +
          ", " +
          lastY);

      Log.d(TAG, "SCROLL | current point in the scroll is: " +
          e2.getX()+
          ", " +
          e2.getY());

      // remove the point we're touching (if we're touching one at all),
      // and then create a new one with an updated degree value.
      if (removePoint(lastX, lastY) || removePoint(e1.getX(), e1.getY())) {
        Log.d(TAG, "SCROLL | we were touching a point!");

        TouchPoint pointToAdd = new TouchPoint();
        pointToAdd.mDegrees = coordsToDegrees(
            e2.getX(), 
            e2.getY());

        Log.d(TAG, "SCROLL | adding point at: " + pointToAdd.mDegrees);
        mPoints.add(pointToAdd);
        invalidate();
        return true;
      }
      invalidate();
      // we aren't touching a point.
      return false;
        }

    @Override
    public boolean onDown(MotionEvent e) {
      return true;
    }
  }
}
