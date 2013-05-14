package org.dcalacci.android.ipc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class ChartView extends View {
  private String TAG = "circletouch.circle";
  // view boundary for the circle.
  private RectF mCircleBounds = new RectF();
  private ArrayList<TouchPoint> mPoints = new ArrayList<TouchPoint>();

  // True if we can edit the circle. True by default.
  private String mOrigin = "";
  private boolean mIsEditing = true;

  //Items
  private ArrayList<Category> mCategories = new ArrayList<Category>();

  // circle positions
  private float mCircleX;
  private float mCircleY;
  private float mCircleRadius;

  // minimum value between two touchpoints, in radians.
  // 20 degrees in radians
  private final double ANGLE_THRESHOLD = 0.174532*2;
  // 10 degrees in radians
  private final double ANGLE_INTERVAL = 0.174532;

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
  // for categories
  private Paint mCategoryPaint;

  /**
   * Create a new ChartView with just a context
   */
  public ChartView(Context c) {
    super(c);
    init();
    mContext = c;
  }

  /**
   * Create a new ChartView with a predefined set of attrs.
   */
  public ChartView(Context ctx, AttributeSet attrs) {
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
    TypedArray a = ctx.getTheme().
      obtainStyledAttributes(attrs, R.styleable.circle, 0, 0);
    try {
      // resolve values from the typedarray and store into fields
      mTouchPointRadius =
        a.getInteger(R.styleable.circle_touchPointRadius, 25);
      mTouchPointColor =
        a.getInteger(R.styleable.circle_touchPointColor,0xffff0000);
    } finally {
      // release TypedArray
      a.recycle();
    }
    // initialize everything
    init();
  }

  /**
   * Called when the size of the screen is changed; we describe the size of
   * the circle and it's boundary area here.
   */
  public void onSizeChanged(int w, int h, int oldw, int oldh) {
    // calculate the total padding size
    float xpad = (float) (getPaddingLeft() + getPaddingRight());
    float ypad = (float) (getPaddingTop() + getPaddingBottom());

    //figure out the "correct" width and height based on the padding
    float ww = (float) w - xpad;
    float hh = (float) h - ypad;

    // let's make the circle as large as it can be for this view
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
      mTouchPointRadius+=2;
      Log.d(TAG,
            "Touchpoints are a little big. reducing to: " +
            mTouchPointRadius);
      Log.d(TAG, "mCircle center is: " +mCircleX +", "+mCircleY);

    }
  }

  public void setIsEditing(boolean bool) {
    this.mIsEditing = bool;
  }

  public boolean getIsEditing() {
    return this.mIsEditing;
  }

  /**
   * Gets the mOrigin variable
   * @return Whether or not we can edit the chart
   */
  public String getmOrigin() {
    return this.mOrigin;
  }

  /**
   * Sets mCanEdit to the given boolean value
   * @param canEdit the value to set mCanEdit to.
   */
  public void setmOrigin(String origin) {
    this.mOrigin = origin;
  }

  public boolean isSameOrigin(String other){
    return this.mOrigin.equals(other);
  }

  public void setmCategory(ArrayList<Category> categoryList){
    this.mCategories = categoryList;
  }

  public void setmPoints(ArrayList<TouchPoint> pointsList){
    this.mPoints = pointsList;
  }

  /**
   * Adds an item to the list of points
   * @param degrees the degree value for the point to add.
   */
  private void addItem(double rads) {
    // create a new point
    TouchPoint p = new TouchPoint();
    p.setmRads(rads);

    // add it to the list of points
    mPoints.add(p);
    sortListCW(mPoints, 0);
    invalidate();
  }

  /**
   * Adds category to the list of Categories
   * @param category The category to add
   * @param Color The color of the category
   */
  private void addCategory(String category, int color){
    addCategoryHelper(null, null, category, color);
    addPoints();
  }

  /**
   * Adds n items to the chart, splitting the space evenly.
   */
  private void addNItems(int n) {
    double radSections = (Math.PI*2)/n;
    double totalRads = 0;
    for (int i = 0; i < n; i++) {
      totalRads = moveRadCW(totalRads, radSections);
      addItem(totalRads);
    }
  }

  /**
   * Adds points to the charts
   */
  private void addPoints(){
    int cSize = mCategories.size();
    clearPoints();
    addNItems(cSize);
    setPointsToCategories();
  }

  /**
   * Clears all the points
   */
  private void clearPoints(){
    mPoints.clear();
  }

  /**
   * Links the points and categories...duh.
   */
  public void linkPointsAndCategories() {
    for (TouchPoint pt : mPoints) {
      for (Category c : mCategories) {
        if (pt.getmRads() == c.getpCW().getmRads()) {
          c.setpCW(pt);
        } else if (pt.getmRads() == c.getpCCW().getmRads()) {
          c.setpCCW(pt);
        }
      }
    }
  }

  /**
   * Sets the points associated to the category
   */
  private void setPointsToCategories(){
    int size = mPoints.size();
    int i = 0;
    int j = i+1;
    for (Category c : mCategories){

      c.setpCCW(mPoints.get(i));
      if (i == (size - 1)){
        c.setpCW(mPoints.get(0));
      }else{
        c.setpCW(mPoints.get(j));
      }

      i++;
      j++;
    }
  }

  /**
   * Removes the given index from categories ~~cleanly~~ - this means
   * in such a way that we don't reset the chart.
   * @param index The index of the category to remove in mCategories
   */
  private void removeCategory(String cat) {
    Collections.sort(mCategories);

    for (int i=0; i<mCategories.size(); i++) {
      if (mCategories.get(i).getCategory().equalsIgnoreCase(cat)) {
        // remove this points' CW point from the list.
        mPoints.remove(mCategories.get(i).getpCW());
        // set the next categories' CCW point to this categories' CCW point.
        nextCategory(i).setpCCW(mCategories.get(i).getpCCW());
        // finally, remove this category from the list.
        mCategories.remove(i);
      }
    }
  }

  /**
   * Returns the next index in the category list
   * @param index The index of the item that is before the item to return.
   */
  private Category nextCategory(int index) {
    if (index == mCategories.size() - 1) {
      return mCategories.get(0);
    } else {
      return mCategories.get(index+1);
    }
  }

  /**
   * Adds categories Helper
   * @param pCCW - Counter Clockwise point
   * @param pCW - Clockwise point
   * @param category - the Category of the slice
   * @param color - Color of the category
   */
  private void addCategoryHelper(TouchPoint pCCW,
                                 TouchPoint pCW,
                                 String category,
                                 int color){
    Category item = new Category(pCCW, pCW, category, color);
    mCategories.add(item);
  }

  /**
   * Checks whether the category already exists in the list
   * @param category - name of category (ignores UPPER/LOWER case)
   * @return inList - exist in list
   */
  public boolean isCategoryinList(String category){
    boolean inList = false;
    for(Category c : mCategories){
      if (c.getCategory().equalsIgnoreCase(category)){
        inList = true;
      }
    }
    return inList;
  }

  /**
   * Clears the Chart of its contents
   * Clears associated ArrayLists
   */
  public void clearChart() {
    // TODO Auto-generated method stub
    this.mCategories.clear();
    this.mPoints.clear();
    invalidate();
  }

  // /**
  //  * gets the Chart data (Category Name, pCCW, pCW)
  //  * @return JSONArray - Contains JSONObjects of each category
  //  */
  // private JSONArray getChartData(){
  //   Log.d(TAG, mCategories.toString());
  //   String localTag = "ChartView.getChartData";
  //   JSONArray jsonArray = new JSONArray();

  //   for(Category category : mCategories){
  //     Map obj = new LinkedHashMap();

  //     obj.put("Category", category.getCategory());
  //     obj.put("pCCW", category.getpCCW().getmRads());
  //     obj.put("pCW", category.getpCW().getmRads());

  //     JSONObject jsonObject = new JSONObject(obj);
  //     jsonArray.put(jsonObject);

  //   }
  //   return jsonArray;
  //  }

  public JSONArray getChartData() {
    Log.d(TAG, mCategories.toString());
    JSONArray array = new JSONArray();
    for (Category category : mCategories) {
      Map obj = new LinkedHashMap();
      obj.put("name", category.getCategory());
      obj.put("amt", getPercentage(category.getpCCW(), category.getpCW()));
      JSONObject jObj = new JSONObject(obj);
      array.put(jObj);
    }
    return array;
  }

  private Double getPercentage(TouchPoint pCCW, TouchPoint pCW) {
    Double diff = getDifference(pCCW.getmRads(), pCW.getmRads());
    return Math.abs(diff/(2*Math.PI));
  }

  /**
   * Draws the view
   * @param canvas The canvas to draw on, dummy!
   */
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    int size = mCategories.size();
    if (size >= 2){
      for (int i = 0; i < size; i++){
        Category c = mCategories.get(i);

        TouchPoint end = c.getpCW();
        TouchPoint start = c.getpCCW();

        Paint color = new Paint(Paint.ANTI_ALIAS_FLAG);
        color.setColor(c.getColor());
        color.setStyle(Paint.Style.FILL_AND_STROKE);

        float startAngle = (float) radsToDegree(start.getmRads());
        float sweepAngle;
        // get correct rad magnitude
        if (getDifference(start.getmRads(), end.getmRads()) < 0) {
          sweepAngle = (float) (Math.PI*2 + getDifference(start.getmRads(), end.getmRads()));
        } else {
          sweepAngle = (float) (getDifference(start.getmRads(), end.getmRads()));
        }
        // convert to degrees, draw the arc.
        sweepAngle = radsToDegree(sweepAngle);
        canvas.drawArc(mCircleBounds, startAngle, sweepAngle, true, color);

      }

      // drawing the touch-points
      for (TouchPoint point : mPoints) {
        PointF touchPointCoords = radsToPointF(point.getmRads());
        //Draw the separators
        canvas.drawLine(
                        mCircleX,
                        mCircleY,
                        touchPointCoords.x,
                        touchPointCoords.y,
                        mSeparatorLinesPaint
                        );
      }
      if (mIsEditing) {
        Log.d(TAG, "@@onDraw, editing - drawing the touchPoints");
        for (TouchPoint point : mPoints) {
          PointF touchPointCoords = radsToPointF(point.getmRads());
          // draw the touchPoint on the canvas
          canvas.drawCircle(
                            touchPointCoords.x,
                            touchPointCoords.y,
                            mTouchPointRadius,
                            mTouchPointPaint
                            );
        }
      }
    } else if (size == 1) { // only one category
      Category c = mCategories.get(0);
      Paint color = new Paint(Paint.ANTI_ALIAS_FLAG);
      color.setColor(c.getColor());
      color.setStyle(Paint.Style.FILL_AND_STROKE);
      canvas.drawArc(mCircleBounds, 0f, 360f, true, color);
    } else { // no categories
      Paint noCats = new Paint(Paint.ANTI_ALIAS_FLAG);
      noCats.setColor(mContext.getResources().getColor(R.color.Chart_Circle_empty));
      noCats.setStyle(Paint.Style.FILL_AND_STROKE);
      canvas.drawArc(mCircleBounds, 0f, 360f, true, noCats);
    }
    // drawing the circle boundary
    /* canvas.drawCircle( */
    /*     mCircleX, */
    /*     mCircleY, */
    /*     mCircleRadius, */
    /*     mCirclePaint */
    /*     ); */

  }

  /**
   * doing stuff with touch
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    //Log.d(TAG, "ONTOUCHEVENT | received a touchEvent");
    boolean result = mGestureDetector.onTouchEvent(event);

    // if we're not editing, we can't touch anything.
    if (!mIsEditing) {
      Log.d(TAG, "onTouchEvent returning false = !mIsEditing");
      return false;
    }

    // if the user lifts their finger, we're not in a scroll.
    if (event.getAction() == MotionEvent.ACTION_UP) {
      inScroll = false;
      onScrollFinished();
    }

    // return true if mGestureDetector handled the event
    if (result) {
      return result;
    }
    return false;

  }

  private float radsToDegree(double val){
    float toDegree = (float) Math.toDegrees(val);
    return toDegree;
  }

  /**
   * converts a radian value to a coordinate on the edge of the circle
   * @param theta The radian value to convert to a coordinate
   */
  private PointF radsToPointF(double theta) {
    float y = (float)
      (mCircleY + (Math.sin(theta) * mCircleRadius));
    float x = (float)
      (mCircleX + (Math.cos(theta) * mCircleRadius));
    return new PointF(x, y);
  }

  /**
   * Same thing as pointFtoDegrees, but separate values for the x and y vals.
   * @param x the x-value of the coordinate
   * @param y the y-value of the coordinate
   * */
  private double coordsToRads(float x, float y) {
    double rads = (double) Math.atan2((y - mCircleX),(x - mCircleX));
    return rads;
  }

  /**
   * Initializes some values and all of the fancy paints and listeners
   */
  private void init() {

    // set up the circle paint
    mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mCirclePaint.setStyle(Paint.Style.STROKE);

    mCategoryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mCategoryPaint.setStyle(Paint.Style.FILL);

    // set up the touchpoint paint
    mTouchPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTouchPointPaint.setStyle(Paint.Style.FILL);
    mTouchPointPaint.setColor(mTouchPointColor);

    // set up the separatorLines paint
    mSeparatorLinesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mSeparatorLinesPaint.setStyle(Paint.Style.STROKE);

    addCategory("Protein", getResources().getColor(R.color.Protein));
    addCategory("OilSugar", getResources().getColor(R.color.Oil_Sugar));

    printTouchPoints();
    sortListCW(mPoints, 0);
    printTouchPoints();

    // set up the gesture detector
    mGestureDetector = new GestureDetector(
                                           ChartView.this.getContext(),
                                           new GestureListener());

    // Turn off long press--this control doesn't use it, and if long press is
    // enabled, you can't scroll for a bit, pause, then scroll some more
    // (the pause is interpreted as a long press, apparently)
    mGestureDetector.setIsLongpressEnabled(false);

    invalidate();
  }

  private void printTouchPoints() {
    for (int i = 0; i< mPoints.size(); i++) {
      System.out.println(i + ": " + mPoints.get(i).getmRads());
    }
  }

  /**
   * Moves start delta degrees clockwise
   * @param start The start angle in radians
   * @param delta The amount to rotate start by
   */
  public double moveRadCW(double start, double delta) {
    // if we're going from + to - (bottom to top)
    if (start + delta > Math.PI) {
      double radsUntilPI = Math.PI - start;
      delta -= radsUntilPI;
      return -1*(Math.PI - delta);
    } else {
      start += delta;
      return start;
    }
  }

  /**
   * Moves start by delta degrees counter clockwise
   * @param start The start angle in radians
   * @param delta The amount to rotate start by
   */
  public double moveRadCCW(double start, double delta) {
    if (start - delta < -1*Math.PI) {
      double radsUntilPI = Math.PI + start;
      delta -= radsUntilPI;
      return (Math.PI - delta);
    } else {
      start -= delta;
      return start;
    }
  }

  /**
   * returns true if the given x and y coords are "inside" of point p - in
   * quotes because we're a little lenient to give the users some wiggle room.
   * @param x The x value of the coordinate to check
   * @param y The y-value of the coordinate to check
   * @param p The TouchPoint to check
   */
  private boolean isTouchingThisPoint(float x, float y, TouchPoint p) {
    PointF pCoords = radsToPointF((double)p.getmRads());
    double dist = Math.sqrt(
                            Math.pow( (double)pCoords.x - x, 2) +
                            Math.pow( (double)pCoords.y - y, 2));
    // make it * 2 to give users a little breathing room(the dots are small)
    return dist <= mTouchPointRadius*2;
  }

  /**
   * returns true if d1 is within 30 degrees in front of d2
   * @param start the reference radian value
   * @param start the radian value to check the position of
   */
  private boolean movingClockwise(double start, double end) {
    double diff = getDifference(start, end);
    return diff > 0;
  }

  /**
   * returns true if the given point has another point in front of it
   * (clockwise) within ANGLE_THRESHOLD or less - should only be called if
   * the touchPoint is being rotated clockwise.
   * @param p1 the point to check
   */
  private boolean hasPointInFront(TouchPoint p1) {
    for (TouchPoint point : mPoints) {
      // edge case
      if (point.getmRads() < 0 && p1.getmRads() > 0 &&
          ((Math.PI + point.getmRads() + Math.PI - p1.getmRads()) <= ANGLE_THRESHOLD)) {
        return true;
      } else if (point.getmRads() > p1.getmRads() &&
                 point.getmRads() - p1.getmRads() < ANGLE_THRESHOLD) {
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
      // edge case
      if (point.getmRads() > 0 && p1.getmRads() < 0 &&
          (Math.PI - point.getmRads() + Math.PI + p1.getmRads() <= ANGLE_THRESHOLD)) {
        return true;
      } else if (point.getmRads() < p1.getmRads() &&
                 p1.getmRads() - point.getmRads() <= ANGLE_THRESHOLD) {
        return true;
      }
    }
    return false;
  }

  /**
   * returns the difference between two radian values, negative if end is
   * under pi rads away from start, ccw, positive otherwise.
   * @param start The first angle
   * @param end The second angle
   */
  private double getDifference(double start, double end) {
    // if result is greater than pi, subtract it from 2pi.
    double diff;
    //edge case from -pi to pi
    if (end < 0 && start > 0) {
      diff = Math.PI - start + Math.PI + end;
    }
    // reverse case - start is on top, end is on bottom. Also handles reg. case
    else {
      diff = end - start;
    }
    if (diff > Math.PI) {
      diff = diff-Math.PI*2; // negative because it's in ccw rotation
    }
    return diff;
  }

  /**
   * Returns true if the ark between r1 and r2 has passed rm
   * @param r1 The starting radian
   * @param rm The radian we're checking
   * @param r2 The ending radian
   */
  private boolean hasPassed(double r1, double rm, double r2) {
    if (movingClockwise(r1, r2)) {
      if (getDifference(r1, rm) > 0 &&
          getDifference(r2, rm) < 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * on my anonymous inner class grind - this sorts the given arraylist by
   * the rotation distance from refRad to each touchPoint, clockwise.
   * @param pts The arrayList to sort
   * @param refRad The referance radian measurement
   */
  private void sortListCW(
                          ArrayList<TouchPoint> pts,
                          final double refRad) {

    Collections.sort( pts,
                      new Comparator<TouchPoint>() {
                        public int compare(TouchPoint a, TouchPoint b) {
                          // difference is >0 if clockwise, <0 if not.
                          double aDiff = getDifference(refRad, a.getmRads());
                          double bDiff = getDifference(refRad, b.getmRads());
                          if (aDiff < 0) {
                            aDiff = Math.PI*2 + aDiff;
                          }
                          if (bDiff < 0) {
                            bDiff = Math.PI*2 + bDiff;
                          }
                          return (aDiff > bDiff ? 1 : (aDiff == bDiff ? 0 : -1));
                        }
                      }
                      );
  }

  /**
   * on my anonymous inner class grind - this sorts the given arraylist by
   * the rotation distance from refRad to each touchPoint, counter-clockwise.
   * @param pts The arrayList to sort
   * @param refRad The referance radian measurement
   */
  private void sortListCCW(
                           ArrayList<TouchPoint> pts,
                           final double refRad) {

    Collections.sort( pts,
                      new Comparator<TouchPoint>() {
                        public int compare(TouchPoint a, TouchPoint b) {
                          // difference is >0 if clockwise, <0 if not.
                          double aDiff = getDifference(refRad, a.getmRads());
                          double bDiff = getDifference(refRad, b.getmRads());
                          if (aDiff < 0) {
                            aDiff = Math.PI*2 + aDiff;
                          }
                          if (bDiff < 0) {
                            bDiff = Math.PI*2 + bDiff;
                          }
                          return (aDiff > bDiff ? -1 : (aDiff == bDiff ? 0 : 1));
                        }
                      }
                      );
    pts.add(0, mPoints.get(mPoints.size()-1));
    pts.remove(mPoints.size()-1);
  }

  /**
   * Handles when the scroll movement is finished
   */
  private void onScrollFinished() {
    for (TouchPoint p : mPoints) {
      p.isBeingTouched = false;
    }
    printTouchPoints();
    inScroll = false;
  }

  /**
   * Return a list of interpolated radians, given a start and end radian, using
   * the ANGLE_INTERVAL global constant.
   * @param start The start radian
   * @param end The end radian
   * @param cw True if we're moving clockwise
   */
  private ArrayList<Double> interpolate(double start, double finish, boolean cw) {
    ArrayList<Double> interpolated = new ArrayList<Double>();
    double diff = getDifference(start, finish);

    // moving CW
    if (cw) {
      int numAngles = (int)(diff/ANGLE_INTERVAL);
      Log.d(TAG, "@@interpolate | numAngles = "+numAngles);
      for (int i=0; i < numAngles; i++) {
        double toAdd = moveRadCW(start, i*ANGLE_INTERVAL);
        interpolated.add(i, toAdd);
        Log.d(TAG, "@@interpolate_ | adding " +toAdd);
      }
      // make sure the last value is in there
      interpolated.add(finish);

    }else {
      diff = Math.abs(diff);
      int numAngles = (int)(diff/ANGLE_INTERVAL);
      for (int i=0; i < numAngles; i++) {
        double toAdd = moveRadCCW(start, i*ANGLE_INTERVAL);
        interpolated.add(i, toAdd);
        Log.d(TAG, "@@interpolate_ | adding " +toAdd);
      }
    }
    return interpolated;
  }

  /**
   * Moves the point being touched to the given radian, moving all other
   * points accordingly.
   * @param rad The radian to move the point being touched to
   * @param cw True if we're moving clockwise, false otherwise
   */
  private void movePointBeingTouched(double rads, boolean cw) {
    // move point being touched to the given radian value
    mPoints.get(0).setmRads(rads);
    // move other points
    Log.d(TAG, "@@movePointBeingTouched | before for loop");
    printTouchPoints();
    for (TouchPoint pt : mPoints) {
      if (!pt.isBeingTouched) {
        if (cw) {
          if (hasPointBehind(pt)) {
            double prevPointRads = mPoints.get(mPoints.indexOf(pt)-1).getmRads();
            pt.setmRads(moveRadCW(prevPointRads, ANGLE_THRESHOLD));
            Log.d(TAG, "@@movePointBeingTouched | CW moving "+mPoints.indexOf(pt) +" to " +moveRadCW(prevPointRads, ANGLE_THRESHOLD));
          }
        } else {
          if (hasPointInFront(pt)) {
            double nextPointRads = mPoints.get(mPoints.indexOf(pt)-1).getmRads();
            pt.setmRads(moveRadCCW(nextPointRads, ANGLE_THRESHOLD));
            Log.d(TAG, "@@movePointBeingTouched | CCW moving "+mPoints.indexOf(pt) +" to " +moveRadCCW(nextPointRads, ANGLE_THRESHOLD));
          }
        }
      } else { // we're touching it
      }
    }
    Log.d(TAG, "@@movePointBeingTouched | after for loop");
    printTouchPoints();
    invalidate();
  }

  /**
   * Returns true if a point is between curRad and lastRad
   * @param curRad Current radian of the scroll
   * @param lastRad Last radian of the scroll
   * @param cw True if we're moving clockwise, false otherwise
   */
  private boolean skippedPoint(double curRad, double lastRad, boolean cw) {
    boolean skipped = false;
    for (TouchPoint pt : mPoints) {
      if (!pt.isBeingTouched) {
        if (cw && !pt.isBeingTouched && hasPassed(lastRad, pt.getmRads(), curRad)) {
          skipped = true;
        }
        else if (hasPassed(curRad, pt.getmRads(), lastRad)) {
          skipped = true;
        }
      }
    }
    return skipped;
  }

  /**
   * Move all points in mPoints, given the curRad and lastRad values from an
   * onScrollEvent
   * @param curRad The current radian of the onScroll
   * @param lastRad The last radian of the onScroll
   * @param clockwise True if we're moving clockwise, false otherwise
   */
  private void movePoints(double curRad, double lastRad, boolean clockwise) {
    Log.d(TAG, "@@_movePoints( "+curRad+", "+lastRad+", clockwise: "+clockwise+" )");
    // mPoints is sorted clockwise or ccw from the point being touched(0)
    // Get the radians moved. should be positive if clockwise==true
    if (skippedPoint(curRad, lastRad, clockwise)) {
      Log.d(TAG, "@@_movePoints | >>SKIPPED<<");

      /* double diff = getDifference(lastRad, curRad); */
      // if we've moved more than our designated interval
      /* if (Math.abs(diff) > ANGLE_INTERVAL) { */
      Log.d(TAG, "@@_movePoints | diff > interval. Interpolating...");
      // interpolate the values
      ArrayList<Double> interpolated = interpolate(mPoints.get(0).getmRads(),
                                                   curRad, clockwise);
      for (Double rad : interpolated) {
        Log.d(TAG, "@@_movePoints | moving pt being touched to : "+
              rad + " from " + mPoints.get(0).getmRads());
        movePointBeingTouched(rad, clockwise);
      }
      Log.d(TAG, "@@_movePoints | done interpolating");
    } else {
      movePointBeingTouched(curRad, clockwise);
    }
    invalidate();
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

      if (!mIsEditing) { return false;}
      // if we can't edit, fuhgeddaboutit
      /* if (isSameOrigin(TAG_FROMHISTORY)) { return false;} */

      float lastX = e2.getX() + distanceX;
      float lastY = e2.getY() + distanceY;

      // calculate the degree values of the last touch event and the
      // current touch event
      double lastRad = coordsToRads(lastX, lastY);
      double curRad = coordsToRads(e2.getX(), e2.getY());

      // have we moved clockwise?
      boolean clockwise = movingClockwise(lastRad, curRad);

      // if we're not in a scroll already, figure out which one is being
      // touched
      if (!inScroll) {
        for (TouchPoint p : mPoints) {
          // mark the point being touched
          if (isTouchingThisPoint(e1.getX(), e1.getY(), p)) {
            inScroll = true;
            p.isBeingTouched = true;
          }
        }
      }
      if (inScroll) {
        // Then, sort mPoints accordingly
        double touchRads = 0;;
        for (TouchPoint p : mPoints) {
          if (p.isBeingTouched) {
            touchRads = p.getmRads();
          }
        }
        if (clockwise) {
          sortListCW(mPoints, touchRads);
        } else {
          sortListCCW(mPoints, touchRads);
        }

        movePoints(curRad, lastRad, clockwise);
        invalidate();
        return true;
      }
      invalidate();
      return false;
    }

    // we need to return true here so we can actually scroll.
    public boolean onDown(MotionEvent e) {
      for (TouchPoint p : mPoints) {
        if (isTouchingThisPoint(e.getX(), e.getY(), p)) {
          Vibrator v =
            (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
          v.vibrate(25);
          inScroll = true;
          p.isBeingTouched = true;
        }
      }

      return true;
    }
  }
}
