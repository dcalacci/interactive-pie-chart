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
//
// Have an ITEM class.  Items have two points, and on drawing, we draw
// an arc inbetween those two points.  Items also have a color and a label?
// OR items are ONE OF [Protein, Dairy, Vegetables ...]  Not sure.
// 
// make sure that when you expand or shrink a specific section, the other 
// sections do something that looks nice - keep the other sections 
// proportional instead of just shrinking them
//
// SCROLLING - DON'T CARE ABOUT IF THEIR LAST MOVEMENT WAS ON A DOT.
// care only about if their FIRST one was.


public class circle extends View {
  private String TAG = "circletouch.circle";
  // view boundary for the circle.
  private RectF mCircleBounds = new RectF();
  private ArrayList<TouchPoint> mPoints = new ArrayList<TouchPoint>();

  // circle positions
  private float mCircleX;
  private float mCircleY;
  private float mCircleRadius;

  // touchPoint info
  private int mTouchPointRadius;
  private int mTouchPointColor;

  // gesture detection
  private GestureDetector mGestureDetector;
  private boolean inScroll = false;


  //paints
  private Paint mCirclePaint;
  private Paint mTouchPointPaint;
  private Paint mSeparatorLinesPaint;
  private Context mContext;

  /**
   * boring constructor with just a context
   */
  public circle(Context c) {
    super(c);
    init();
    // grab the typedarray from attrs
    mContext = c;
  }

  /**
   * constructor with attrs etc.
   */
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
   * Draws the view, takes a canvas.
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

    for (TouchPoint point : mPoints) {
      PointF touchPointCoords = degreesToPointF(point.mDegrees);
      canvas.drawLine(
          mCircleX,
          mCircleY,
          touchPointCoords.x,
          touchPointCoords.y,
          mSeparatorLinesPaint
          );
    }
  }

  /**
   * doing stuff with touch
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    //Log.d(TAG, "ONTOUCHEVENT | received a touchEvent");
    boolean result = mGestureDetector.onTouchEvent(event);

    if (event.getAction() == MotionEvent.ACTION_UP) {
      inScroll = false;
      onScrollFinished();
    }

    if (result) {
      return result;
    }
    return false;

  }

  /**
   * converts a degree value to a coordinate on the edge of the circle
   * @param theta The degree value to convert to a coordinate
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
   * converts a set of coordinates and calculates the degree measurement in
   * relation to the center of the circle - 0 degrees is at the top of the 
   * circle, 180 at the bottom. 
   * note that this can take coordinates that are anywhere in the view
   * @param coords The coordinate to convert to a degree measurement
   */
  private double pointFtoDegrees(PointF coords) {
    return (double) 90 + Math.toDegrees(Math.atan2( coords.y - mCircleY, coords.x - mCircleX));
  }
  /**
   * Same thing as pointFtoDegrees, but separate values for the x and y vals.
   * @param x the x-value of the coordinate
   * @param y the y-value of the coordinate
   * */
  private double coordsToDegrees(float x, float y) {
    return (double) 90 + Math.toDegrees( Math.atan2( y - mCircleY, x - mCircleX));
  }

  /**
   * determines the number of degrees between two points using the circle's
   * center point.
   * @param x1 The x-coordinate of the first point
   * @param y1 The y-coordinate of the first point
   * @param x2 The x-coordinate of the second point
   * @param y2 The y-coordinate of the second point
   */
  public int degreesMovedBetweenPoints(
      float x1, float y1, float x2, float y2) {

    double angle1 = coordsToDegrees(x1, y1);
    double angle2 = coordsToDegrees(x2, y2);
    return (int) (angle1 - angle2);
  }

  /**
   * Initializes some values and all of the fancy paints and listeners
   */
  private void init() {

    // set up the circle paint
    mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mCirclePaint.setStyle(Paint.Style.STROKE);

    // set up the touchpoint paint
    mTouchPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTouchPointPaint.setStyle(Paint.Style.FILL);
    mTouchPointPaint.setColor(mTouchPointColor);

    // set up the separatorLines paint
    mSeparatorLinesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mSeparatorLinesPaint.setStyle(Paint.Style.STROKE);

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

  /**
   * Adds an item to the list of points
   * @param degrees the degree value for the point to add.
   */
  private void addItem(int degrees) {
    // create a new point
    TouchPoint p = new TouchPoint();
    p.mDegrees = degrees;

    // add it to the list of points
    mPoints.add(p);
  }

  /**
   * Container for touch points
   */
  private class TouchPoint {
    public double mDegrees;
    public boolean isBeingTouched = false;
  }

  /**
   * returns true if the given x and y coords are "inside" of point p - in 
   * quotes because we're a little lenient to give the users some wiggle room.
   * @param x The x value of the coordinate to check
   * @param y The y-value of the coordinate to check
   * @param p The TouchPoint to check
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
   * TODO:
   * This should be changed because we only use it with the motionevent values.
   * returns true if d1 is within 30 degrees in front of d2
   * @param d1 the reference degree value
   * @param d2 the degree value to check the position of
   */
  private boolean movingClockwise(double start, double end) {
    double diff = getDifference(end, start);
    return (diff >=180);
  }

  /**
   * returns true if the given point has another point in front of it
   * (clockwise) within 10 degrees or less - should only be called if 
   * the touchPoint is being rotated clockwise.
   * @param p1 the point to check
   */
  private boolean hasPointInFront(TouchPoint p1) {
    for (TouchPoint point : mPoints) {
      double diff = getDifference(p1.mDegrees, point.mDegrees);
      // if greater than 180 then point is in front of p1, and if it's within
      // 10 of 360, the two points are 10 away.
      if ((diff >= 180) &&
          (360 - diff <= 20)) {
        return true;
          }
    }
    return false;
  }

  /**
   * returns true if the given point has another point in behind it
   * (counter-clockwise) within 10 degrees or less - should only be called if 
   * the touchPoint is being rotated counterclockwise.
   * @param p1 the point to check
   */
  private boolean hasPointBehind(TouchPoint p1) {
    for (TouchPoint point : mPoints) {
      double diff = getDifference(p1.mDegrees, point.mDegrees);
      // if greater than 180 then point is in front of p1, and if it's within
      // 10 of 360, the two points are 10 away.
      if ((diff < 180) &&
          (diff <= 20)) {
        return true;
          }
    }
    return false;
  }

  /**
   * returns the difference between two degree values
   * returns a number > 180 if d2 is 
   */
  private double getDifference(double d1, double d2) {
   return (d1-d2)%360;
  }


  /**
   * Handles when the scroll movement is finished
   */
  private void onScrollFinished() {
    for (TouchPoint p : mPoints) {
      p.isBeingTouched = false;
    }
    inScroll = false;
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

        //TODO: is this the correct way to generate the last point touched?
        // calculate the last point in the scroll
        float lastX = e2.getX() + distanceX;
        float lastY = e2.getY() + distanceY;

        // go through the points, see if we're touching one(or more) and
        // then alter their degree values accordingly.
        for (TouchPoint p : mPoints ){
          // if the first touch point is touching a point, then...
          if (isTouchingThisPoint(e1.getX(), e1.getY(), p)) {
            inScroll = true;
            p.isBeingTouched = true;
            // otherwise, if the point "isbeingtouched"...
          } if (p.isBeingTouched) {
            p.mDegrees = coordsToDegrees(e2.getX(), e2.getY());

            // calculate the degree values of the last touch event and the 
            // current touch event
            double lastDegree = coordsToDegrees(lastX, lastY);
            double curDegree = coordsToDegrees(e2.getX(), e2.getY());

            //TODO: clockwise always false.
            boolean clockwise = movingClockwise(lastDegree, curDegree);
            Log.d(TAG, "moving from " +lastDegree +" to " +curDegree);

            double degreeDifference = getDifference(curDegree, lastDegree);

            // if it's greater than 180, then we're moving forward.  make the
            // number positive and small.
            // otherwise make it negative - we're moving in the other direction
            if (degreeDifference >= 180) {
              degreeDifference = (360-degreeDifference);
            } else if (degreeDifference < 180) {
              degreeDifference = degreeDifference*-1;
            }

            // update all the points on the chart.
            for (TouchPoint pt : mPoints) {
              pt.mDegrees = pt.mDegrees + degreeDifference;
            }
          inScroll = true;
          invalidate();
          return true;
          }
        }
        return false;
          }
    // we need to return true here so we can actually scroll.
    public boolean onDown(MotionEvent e) {
      return true;
    }
  }
}
