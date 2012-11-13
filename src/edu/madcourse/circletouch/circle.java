package edu.madcourse.circletouch;
import java.util.ArrayList;
import java.util.List;

import edu.madcourse.circletouch.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


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
<<<<<<< HEAD
	private String TAG = "circletouch.circle";
	private Canvas canvas;
	// view boundary for the circle.
	private RectF mCircleBounds = new RectF();
	private ArrayList<TouchPoint> mPoints = new ArrayList<TouchPoint>();

	//Items 
	private ArrayList<Category> mCategories = new ArrayList<Category>();

	// circle positions
	private float mCircleX;
	private float mCircleY;
	private float mCircleRadius;

	// angle stuff
	private final double ANGLE_THRESHOLD = 0.174532;

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
			Log.d(TAG, 
					"Touchpoints are a little big. reducing to: " +
							mTouchPointRadius);
			Log.d(TAG, "mCircle center is: " +mCircleX +", "+mCircleY);

		}
	}

	/**
	 * Draws the view
	 * @param canvas The canvas to draw on, dummy!
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		this.canvas = canvas;
		Log.d(TAG, "mPoints :" + mPoints.toString());

		for (TouchPoint point : mPoints) {
			PointF touchPointCoords = radsToPointF(point.mRads);

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

		// drawing the touch-points
		for (TouchPoint point : mPoints) {
			PointF touchPointCoords = radsToPointF(point.mRads);
			canvas.drawLine(
					mCircleX,
					mCircleY,
					touchPointCoords.x,
					touchPointCoords.y,
					mSeparatorLinesPaint
					);
		}

//		for (Category c : mCategories){
//			TouchPoint start = c.getpCCW();
//			TouchPoint end = c.getpCW();
//			
//			PointF touchPointCoordsStart = radsToPointF(start.mRads);
//			PointF touchPointCoordsEnd = radsToPointF(end.mRads);
//			
//			double angle = radsMovedBetweenPoints(touchPointCoordsStart.x, touchPointCoordsStart.y, touchPointCoordsEnd.x, touchPointCoordsEnd.y);
//			canvas.drawArc(mCircleBounds, startAngle, sweepAngle, useCenter, c.getColor());
//		}
	}

	/**
	 * doing stuff with touch
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//Log.d(TAG, "ONTOUCHEVENT | received a touchEvent");
		boolean result = mGestureDetector.onTouchEvent(event);

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

	/**
	 * converts a degree value to a coordinate on the edge of the circle
	 * @param theta The degree value to convert to a coordinate
	 * need to add pi/2 to the values because these calculations work for a 
	 * circle where 0 degrees is due north, not east.
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
		// range of atan2 output is -pi to pi...it's weird.
		return rads;
	}

	/**
	 * determines the number of degrees between two points using the circle's
	 * center point.
	 * @param x1 The x-coordinate of the first point
	 * @param y1 The y-coordinate of the first point
	 * @param x2 The x-coordinate of the second point
	 * @param y2 The y-coordinate of the second point
	 */
	public double radsMovedBetweenPoints(
			float x1, float y1, float x2, float y2) {
		double angle1 = coordsToRads(x1, y1);
		double angle2 = coordsToRads(x2, y2);
		return (double) (angle2-angle1);
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
		//		addItem(Math.PI);
		//		addItem(Math.PI/2);
		//		addItem(Math.PI/3);

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
	private void addItem(double rads) {
		// create a new point
		TouchPoint p = new TouchPoint();
		p.mRads = rads;

		// add it to the list of points
		mPoints.add(p);
		invalidate();
	}

	/**
	 * Container for touch points
	 */
	private class TouchPoint {
		public double mRads;
		public boolean isBeingTouched = false;

		/**
		 * Moves this touchPoint clockwise the given number of radians
		 */
		public void moveRads(double rads) {
			mRads += rads;
			// if we're going from + to -
			if (Math.abs(mRads) > Math.PI) {
				mRads *= -1;
				mRads += rads;
			}
		}
	}

	/**
	 * Adds category to the list of Categories
	 * TODO: COLOR IN SLICES
	 * @param category
	 * @param Color
	 */
	private void addCategory(String category, int color){
		addCategoryHelper(null, null, category, color);
		addPoints();		
	}

	/**
	 * Adds points to the charts
	 */
	private void addPoints(){
		int cSize = mCategories.size();		

		double rads = (Math.PI * 2) / (cSize);
		double rads_sum = 0;

		clearPoints();

		if(cSize != 1){
			for (int i = 0; i < cSize; i++){
				rads_sum += rads;
				addItem(rads_sum);	
			}
			setPointsToCategories();
		}
	}

	/**
	 * Clears all the points 
	 */
	private void clearPoints(){
		mPoints.clear();
	}

	/**
	 * Sets the points associated to the category
	 */
	private void setPointsToCategories(){
		int size = mPoints.size();
		for (Category c : mCategories){
			for(int i = 0; i < size; i++){
				c.setpCCW(mPoints.get(i));
				if (i == (size - 1)){
					c.setpCW(mPoints.get(0));
				}else{
					c.setpCW(mPoints.get(i+1));
				}
			}
		}
	}

	/**
	 * Removes the specified category from the list
	 * @param category
	 */
	private void removeCategory(String category){
		int index = 0;
		for(Category c : mCategories){
			if(c.getCategory().equalsIgnoreCase(category)){
				break;
			}else{
				index = index + 1;
			}
		}
		mCategories.remove(index);
		if(mCategories.size() < 2){
			clearPoints();
			Log.d(TAG, "ALL GONE: " + mPoints.toString());
		}else{
			addPoints();
			setPointsToCategories();
		}
	}

	/**
	 * Adds categories Helper
	 * @param pCCW - Counter Clockwise point
	 * @param pCW - Clockwise point
	 * @param category - the Category of the slice
	 * @param color - Color of the category
	 */
	private void addCategoryHelper(TouchPoint pCCW, TouchPoint pCW, String category,  int color){
		Category item = new Category(pCCW, pCW, category, color);

		mCategories.add(item);
	}

	/**
	 * Container for "Items" on the pie chart
	 * @param pCCW - the Counter Clockwise Point
	 * @param pCW - the Clockwise Point
	 * @param category - One of: Protein, Grains, Vegetable, Oil/Fat/Sweets, Dairy, Fruits	
	 * @param color - Color of the slice 
	 */

	//foo
	private class Category {
		private TouchPoint pCCW;
		private TouchPoint pCW;
		private String category;
		private int color;

		public Category(TouchPoint ccw, TouchPoint cw, String category, int c ){
			this.pCCW = ccw;
			this.pCW = cw;
			this.category = category;
			this.color = c;
		}

		public TouchPoint getpCCW(){
			return this.pCCW;
		}

		public TouchPoint getpCW(){
			return this.pCW;
		}

		public void setpCCW(TouchPoint ccw){
			this.pCCW = ccw;
		}

		public void setpCW(TouchPoint cw){
			this.pCW = cw;
		}

		public String getCategory(){
			return this.category;
		}

		public int getColor(){
			return this.color;
		}

		public void setColor(int color){
			this.color = color;
		}
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

	public void onProteinClicked(View v){
		String category = "Protein";
		TextView box = (TextView) v.findViewById(R.id.protein_box);
		TextView text = (TextView) v.findViewById(R.id.protein_label);

		boolean inList = isCategoryinList(category);

		if(inList){
			// Deselect
			box.setBackgroundColor(getResources().getColor(R.color.Protein_Grayed));
			text.setTextColor(getResources().getColor(R.color.Protein_Grayed));
			removeCategory(category);
		}else{
			// Select
			box.setBackgroundColor(getResources().getColor(R.color.Protein));
			text.setTextColor(getResources().getColor(R.color.Protein));
			addCategory(category, getResources().getColor(R.color.Protein));
		}		 
		invalidate();
	}

	public void onVegetableClicked(View v){
		String category = "Vegetable";
		TextView box = (TextView) v.findViewById(R.id.vegetable_box);
		TextView text = (TextView) v.findViewById(R.id.vegetable_label);

		boolean inList = isCategoryinList(category);

		if(inList){
			// Deselect
			box.setBackgroundColor(getResources().getColor(R.color.Vegetable_Grayed));
			text.setTextColor(getResources().getColor(R.color.Vegetable_Grayed));
			removeCategory(category);
		}else{
			// Select
			box.setBackgroundColor(getResources().getColor(R.color.Vegetable));
			text.setTextColor(getResources().getColor(R.color.Vegetable));
			addCategory(category, getResources().getColor(R.color.Vegetable));
		}	
		invalidate();
	}

	public void onDairyClicked(View v){
		String category = "Dairy";
		TextView box = (TextView) v.findViewById(R.id.dairy_box);
		TextView text = (TextView) v.findViewById(R.id.dairy_label);

		boolean inList = isCategoryinList(category);

		if(inList){
			// Deselect
			box.setBackgroundColor(getResources().getColor(R.color.Dairy_Grayed));
			text.setTextColor(getResources().getColor(R.color.Dairy_Grayed));
			removeCategory(category);
		}else{
			// Select
			box.setBackgroundColor(getResources().getColor(R.color.Dairy));
			text.setTextColor(getResources().getColor(R.color.Dairy));
			addCategory(category, getResources().getColor(R.color.Dairy));
		}	
		invalidate();
	}

	public void onFruitClicked(View v){
		String category = "Fruit";
		TextView box = (TextView) v.findViewById(R.id.fruit_box);
		TextView text = (TextView) v.findViewById(R.id.fruit_label);

		boolean inList = isCategoryinList(category);

		if(inList){
			// Deselect
			box.setBackgroundColor(getResources().getColor(R.color.Fruit_Grayed));
			text.setTextColor(getResources().getColor(R.color.Fruit_Grayed));
			removeCategory(category);
		}else{
			// Select
			box.setBackgroundColor(getResources().getColor(R.color.Fruit));
			text.setTextColor(getResources().getColor(R.color.Fruit));
			addCategory(category, getResources().getColor(R.color.Fruit));
		}
		invalidate();
	}

	public void onGrainClicked(View v){
		String category = "Grain";
		TextView box = (TextView) v.findViewById(R.id.grain_box);
		TextView text = (TextView) v.findViewById(R.id.grain_label);

		boolean inList = isCategoryinList(category);

		if(inList){
			// Deselect
			box.setBackgroundColor(getResources().getColor(R.color.Grain_Grayed));
			text.setTextColor(getResources().getColor(R.color.Grain_Grayed));
			removeCategory(category);
		}else{
			// Select
			box.setBackgroundColor(getResources().getColor(R.color.Grain));
			text.setTextColor(getResources().getColor(R.color.Grain));
			addCategory(category, getResources().getColor(R.color.Grain));
		}	
		invalidate();
	}

	public void onOilSugarClicked(View v){
		String category = "OilSugar";
		TextView box = (TextView) v.findViewById(R.id.oil_box);
		TextView text = (TextView) v.findViewById(R.id.oil_label);

		boolean inList = isCategoryinList(category);

		if(inList){
			// Deselect
			box.setBackgroundColor(getResources().getColor(R.color.Oil_Sugar_Grayed));
			text.setTextColor(getResources().getColor(R.color.Oil_Sugar_Grayed));
			removeCategory(category);
		}else{
			// Select
			box.setBackgroundColor(getResources().getColor(R.color.Oil_Sugar));
			text.setTextColor(getResources().getColor(R.color.Oil_Sugar));
			addCategory(category, getResources().getColor(R.color.Oil_Sugar));
		}	
		invalidate();
	}


	/**
	 * returns true if the given x and y coords are "inside" of point p - in 
	 * quotes because we're a little lenient to give the users some wiggle room.
	 * @param x The x value of the coordinate to check
	 * @param y The y-value of the coordinate to check
	 * @param p The TouchPoint to check
	 */
	private boolean isTouchingThisPoint(float x, float y, TouchPoint p) {
		PointF pCoords = radsToPointF((double)p.mRads);
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
	 * @param start the reference radian value
	 * @param start the radian value to check the position of
	 */
	private boolean movingClockwise(double start, double end) {

		double diff = getDifference(start, end);
		// Log.d(TAG, "CLOCKWISE: starting at " +start +", ending at: "+end);
		// Log.d(TAG, "CLOCKWISE: diff is " +diff);
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
			if (point.mRads < 0 && p1.mRads > 0 &&
					((Math.PI + point.mRads + Math.PI - p1.mRads) < ANGLE_THRESHOLD)) {
				return true;
			} else if (point.mRads > p1.mRads &&
					point.mRads - p1.mRads < ANGLE_THRESHOLD) {
				return true;
			}
		}
		return false;
	}

	// TODO: change this to handle radians
	/**
	 * returns true if the given point has another point in behind it
	 * (counter-clockwise) within 10 degrees or less - should only be called if 
	 * the touchPoint is being rotated counterclockwise.
	 * @param p1 the point to check
	 */
	private boolean hasPointBehind(TouchPoint p1) {
		for (TouchPoint point : mPoints) {
			// edge case
			if (point.mRads > 0 && p1.mRads < 0 &&
					(Math.PI - point.mRads + Math.PI + p1.mRads < ANGLE_THRESHOLD)) {
				return true;
			} else if (point.mRads < p1.mRads &&
					p1.mRads - point.mRads < ANGLE_THRESHOLD) {
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
		// reverse case - start is on top, end is on bottom. Also handles the
		// regular case.
		else {
			diff = end - start;
		}
		if (diff > Math.PI) {
			diff = diff-Math.PI*2; // negative because it's in ccw rotation
		}
		return diff;
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

			// let's mark the point we're touching
			for (TouchPoint p : mPoints ){
				// if the first touch point is touching a point, then...
				if (isTouchingThisPoint(e1.getX(), e1.getY(), p)) {
					inScroll = true;
					p.isBeingTouched = true;
					// otherwise, if the point "isbeingtouched"...
				} if (p.isBeingTouched) {
					p.mRads = coordsToRads(e2.getX(), e2.getY());

					// calculate the degree values of the last touch event and the 
					// current touch event
					double lastRad = coordsToRads(lastX, lastY);
					double curRad = coordsToRads(e2.getX(), e2.getY());

					// difference between the current position being touched
					// and the last position
					double radDifference = getDifference(lastRad, curRad);

					// have we moved clockwise?
					boolean clockwise = movingClockwise(lastRad, curRad);

					if (clockwise) {
						Log.d(TAG, "CLOCKWISE");
					} else{
						Log.d(TAG, "NOT CLOCKWISE");
					}


					// if clockwise and points in front, move everything.
					if (clockwise && hasPointInFront(p)) {
						for (TouchPoint pt : mPoints) {
							if (!pt.isBeingTouched && hasPointBehind(pt)) {
								Log.d(TAG, "hasPointInFront");
								pt.moveRads(radDifference);
								//pt.mRads += radDifference;
							}
						}
					} else if (!clockwise && hasPointBehind(p)) {
						for (TouchPoint pt : mPoints) {
							if(!pt.isBeingTouched && hasPointInFront(pt)) {
								Log.d(TAG, "hasPointBehind");
								pt.moveRads(radDifference);
							}
						}
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
=======
  private String TAG = "circletouch.circle";
  // view boundary for the circle.
  private RectF mCircleBounds = new RectF();
  private ArrayList<TouchPoint> mPoints = new ArrayList<TouchPoint>();

  // circle positions
  private float mCircleX;
  private float mCircleY;
  private float mCircleRadius;

  // angle stuff
  private final double ANGLE_THRESHOLD = 0.349064;

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
  @SuppressWarnings("unused")
  private Context mContext;

  /**
   * boring constructor with just a context
   */
  public circle(Context c) {
    super(c);
    init();
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
        a.getInteger(R.styleable.circle_touchPointRadius, 15);
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
      boolean what = isBetween(0, Math.PI/3, Math.PI/2);
      Log.d(TAG, "did it work? :" +what);
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
      Log.d(TAG, 
          "Touchpoints are a little big. reducing to: " +
          mTouchPointRadius);
      Log.d(TAG, "mCircle center is: " +mCircleX +", "+mCircleY);

    }
  }

  /**
   * Draws the view
   * @param canvas The canvas to draw on, dummy!
   */
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    for (TouchPoint point : mPoints) {
      PointF touchPointCoords = radsToPointF(point.mRads);

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

    // drawing the touch-points
    for (TouchPoint point : mPoints) {
      PointF touchPointCoords = radsToPointF(point.mRads);
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

  /**
   * converts a degree value to a coordinate on the edge of the circle
   * @param theta The degree value to convert to a coordinate
   * need to add pi/2 to the values because these calculations work for a 
   * circle where 0 degrees is due north, not east.
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
    // range of atan2 output is -pi to pi...it's weird.
    return rads;
  }

  /**
   * determines the number of degrees between two points using the circle's
   * center point.
   * @param x1 The x-coordinate of the first point
   * @param y1 The y-coordinate of the first point
   * @param x2 The x-coordinate of the second point
   * @param y2 The y-coordinate of the second point
   */
  public double radsMovedBetweenPoints(
      float x1, float y1, float x2, float y2) {
    double angle1 = coordsToRads(x1, y1);
    double angle2 = coordsToRads(x2, y2);
    return (double) (angle2-angle1);
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
    addItem(Math.PI);
    addItem(Math.PI/2);
    addItem(Math.PI/3);
    //addItem(-1*Math.PI/2);

    printTouchPoints();
    SortByDistance(mPoints, 0);
    printTouchPoints();

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
  private void printTouchPoints() {
    for (TouchPoint p : mPoints) {
      System.out.println("" + p.mRads);
    }
  }

  /**
   * Adds an item to the list of points
   * @param degrees the degree value for the point to add.
   */
  private void addItem(double rads) {
    // create a new point
    TouchPoint p = new TouchPoint();
    p.mRads = rads;

    // add it to the list of points
    mPoints.add(p);
  }

  /**
   * Container for touch points
   */
  private class TouchPoint implements Comparable<TouchPoint> {
    public double mRads;
    public boolean isBeingTouched = false;

    @Override
      /**
       * positive if getDifference(this, tp) is positive, 0 if it's 0,
       * negative if it's negative.
       * @param tp The touchpoint to compare this to
       */
      public int compareTo(TouchPoint tp) throws ClassCastException {
        if (getDifference(this.mRads, tp.mRads) == 0) {
          return 0;
        } else {
          return getDifference(this.mRads, tp.mRads) > 0 ? 1 : -1;
        }
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
      Log.d(TAG, "Moving clockwise, going to other half...");
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
    // from negative to positive (top to bottom)
    if (start - delta < Math.PI && start < 0) {
      Log.d(TAG, "Moving ccw, going to other half...");
      double radsUntilPI = Math.PI + start;
      delta -= radsUntilPI;
      return Math.PI - delta;
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
    PointF pCoords = radsToPointF((double)p.mRads);
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
      if (point.mRads < 0 && p1.mRads > 0 &&
          ((Math.PI + point.mRads + Math.PI - p1.mRads) < ANGLE_THRESHOLD)) {
        return true;
      } else if (point.mRads > p1.mRads &&
          point.mRads - p1.mRads < ANGLE_THRESHOLD) {
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
      if (point.mRads > 0 && p1.mRads < 0 &&
          (Math.PI - point.mRads + Math.PI + p1.mRads <= ANGLE_THRESHOLD)) {
        return true;
      } else if (point.mRads < p1.mRads &&
          p1.mRads - point.mRads <= ANGLE_THRESHOLD) {
        return true;
          }
    }
    return false;
  }

  private TouchPoint pointBehind(TouchPoint p1) {
    for (TouchPoint point : mPoints) {
      // edge case
      if (point.mRads > 0 && p1.mRads < 0 &&
          (Math.PI - point.mRads + Math.PI + p1.mRads <= ANGLE_THRESHOLD)) {
        return point;
      } else if (point.mRads < p1.mRads &&
          p1.mRads - point.mRads <= ANGLE_THRESHOLD) {
        return point;
          }
    }
    throw new RuntimeException("No point behind " +p1.toString());
  }

  private TouchPoint pointInFront(TouchPoint p1) {
        for (TouchPoint point : mPoints) {
      // edge case
      if (point.mRads < 0 && p1.mRads > 0 &&
          ((Math.PI + point.mRads + Math.PI - p1.mRads) < ANGLE_THRESHOLD)) {
        return point;
      } else if (point.mRads > p1.mRads &&
          point.mRads - p1.mRads < ANGLE_THRESHOLD) {
        return point;
          }
    }
    throw new RuntimeException("No point in front of " + p1.toString());
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
   * Returns true if rm is in the arc that is less than pi between r1 and r2.
   * r1 is start, r2 is end, we're testing to see if rm is in the middle.
   * @param r1 The first angle
   * @param rm The angle in question
   * @param r2 The second angle
   */
  private boolean isBetween(double r1, double rm, double r2) {
    // if the arc from r1 to r2 is less than pi but greater than 0
    if (movingClockwise(r1, r2)){
      // if the arc from rm to r2 is + but less than the arc from r2 to r1
      if (getDifference(r1, rm) > 0) {
        if( (getDifference(r1, rm) < getDifference(r1, r2))) {
          Log.d(TAG, rm + " IS BETWEEN " + r1 +" AND " +r2);
          return true;
        }
        else { return false; }
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
  private boolean hasPassed(double r1, double rm, double r2) {
    Log.d(TAG, "haspassed>>>>>>HASPASSED CALLED");
    if (movingClockwise(r1, r2)) {
      Log.d(TAG, "haspassed>>>>>>>>CLOCKWISE");
      if (getDifference(r1, rm) > 0 &&
          getDifference(r2, rm) < 0) {
        Log.d(TAG, "haspassed>>>>>>>TRUE");
        return true;
          }
    }
    return false;
  }

  /** 
   * on my anonymous inner class grind - this sorts the given arraylist by
   * the rotation distance from refRad to each touchPoint in the list
   * @param pts The arrayList to sort
   * @param refRad The referance radian measurement
   */
  private void SortByDistance(
      ArrayList<TouchPoint> pts,
      final double refRad) {

    Collections.sort( pts, 
        new Comparator<TouchPoint>() {
          public int compare(TouchPoint a, TouchPoint b) {
            // difference is >0 if clockwise, <0 if not.
            double aDiff = getDifference(refRad, a.mRads);
            double bDiff = getDifference(refRad, b.mRads);
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

  /*
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

  //TODO
  //notes
  // maybe we could also enforce two invariants:
  // 1.  mPoints is always ordered from least to greatest rotation, going 
  //  clockwise, starting from due east(0c)
  // 2.  There is always, at minimum, an ANGLE_THRESHOLD rotation distance
  //  between any two given points
  // --how to enforce?
  //  when adding a new element, enforce the first invariant/second invariant by
  //    default
  //  when scrolling, USE the invariant in a useful way 
  //  when scrolling, enforce the invariant BY USING using the invariants:
  //    because points are sorted by rotation:
  //    if we're scrolling and moving clockwise and have a point inFront of us, 
  //    that point has the current touch point behind it.  if that point is
  //    ANGLE_THRESHOLD behind another point, the second point shold be 
  //    i*ANGLE_THRESHOLD away from the current touched point, where i is the 
  //    index of the point in question(the farther one).
  //    PROBLEMS: edge case(s) of -pi to +pi.
  //  other ideas: when we're scrolling, invariant is that the points are ordered
  //  by cw distance from the point being touched?

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

        float lastX = e2.getX() + distanceX;
        float lastY = e2.getY() + distanceY;

        // calculate the degree values of the last touch event and the 
        // current touch event
        double lastRad = coordsToRads(lastX, lastY);
        double curRad = coordsToRads(e2.getX(), e2.getY());

        // difference between the current position being touched
        // and the last position
        double radDifference = getDifference(lastRad, curRad);
        double firstRad = coordsToRads(e1.getX(), e1.getY());

        // have we moved clockwise?
        boolean clockwise = movingClockwise(lastRad, curRad);
        Log.d(TAG, "Clockwise: " +clockwise);

        // normal touch handling
        for (TouchPoint p : mPoints ){
          // if we're touching this point
          if (isTouchingThisPoint(e1.getX(), e1.getY(), p)) {
            inScroll = true;
            p.isBeingTouched = true;
            // if the point "isbeingtouched"...
          } if (p.isBeingTouched) {
            p.mRads = coordsToRads(e2.getX(), e2.getY());
            Log.d(TAG, "last: " + lastRad);
            Log.d(TAG, "diff: " + radDifference);
            Log.d(TAG, "curr: " + curRad);

            // keep 'em in line
            SortByDistance(mPoints, p.mRads);

            // handling keeping the buffer distance with jumps
            // This code uses the ordering of mPoints to 
            // enforce the buffer space between points.
            for ( TouchPoint pt : mPoints) {
              // normal movement, using the ordering of the elements, from
              // the current touchPoint
              //cw
              if (!pt.isBeingTouched && hasPassed(
                    lastRad, 
                    pt.mRads, 
                    moveRadCW(curRad, ANGLE_THRESHOLD))) {
                pt.mRads = moveRadCW( curRad, mPoints.indexOf(pt)*ANGLE_THRESHOLD);
                //ccw
              } else if (!pt.isBeingTouched && hasPassed(
                    moveRadCCW(curRad, ANGLE_THRESHOLD),
                    pt.mRads,
                    lastRad)) {
                int ccwIndex = mPoints.size() - mPoints.indexOf(pt);
                pt.mRads = moveRadCCW( curRad, ccwIndex*ANGLE_THRESHOLD);
                    }
              if ( !pt.isBeingTouched && hasPointBehind(pt) ) {
                double prevPointRads = mPoints.get(mPoints.indexOf(pt)-1).mRads;
                pt.mRads = moveRadCW(prevPointRads, ANGLE_THRESHOLD);
              }

              // cw and then ccw 'jumping'
              if (!pt.isBeingTouched && hasPassed(lastRad, pt.mRads, curRad)) {
                Log.d(TAG, ">>>>>>Skipped a point...CW");
                pt.mRads = moveRadCW(
                    curRad, 
                    mPoints.indexOf(pt)*ANGLE_THRESHOLD);
              } else if (!pt.isBeingTouched && hasPassed(curRad, pt.mRads, lastRad)) {
                Log.d(TAG, ">>>>>>>Skipped a point...CCW");
                pt.mRads = moveRadCCW(curRad, ANGLE_THRESHOLD);
              }
            }


            /*
             *for ( TouchPoint pt : mPoints ) {
             *  if ( !pt.isBeingTouched && hasPointBehind(pt)) {
             *    TouchPoint pb = pointBehind(pt);
             *    pt.mRads = moveRadCW(pb.mRads, ANGLE_THRESHOLD);
             *  } else if (!pt.isBeingTouched && hasPointInFront(pt)) {
             *    TouchPoint pf = pointInFront(pt);
             *    pt.mRads = moveRadCCW(pf.mRads, ANGLE_THRESHOLD);
             *  }
             *}
             */

            // handling points in front and behind in normal, slow motion.
            // if clockwise and points in front, move everything.
            /*
             *            if (clockwise && hasPointInFront(p)) {
             *              for (TouchPoint pt : mPoints) {
             *                if (!pt.isBeingTouched && hasPassed(lastRad, pt.mRads, curRad)) {
             *                  Log.d(TAG, ">>>>>>Skipped a point...");
             *                  pt.mRads = moveRadCW(curRad, ANGLE_THRESHOLD);
             *                  return true;
             *                }
             *                if (!pt.isBeingTouched && isBetween(lastRad, pt.mRads, curRad)) {
             *                  Log.d(TAG, ">>>>>Skipped a point...");
             *                  pt.mRads = moveRadCW(curRad, ANGLE_THRESHOLD);
             *                  onScrollFinished();
             *                  return true;
             *                }
             *
             *                 [>if there's a point behind pt and it's not being touched<]
             *                if (!pt.isBeingTouched && hasPointBehind(pt)) {
             *                  TouchPoint pb = pointBehind(pt);
             *                  pt.mRads = moveRadCW(pb.mRads, ANGLE_THRESHOLD);
             *                  pt.mRads = moveRadCW(curRad, ANGLE_THRESHOLD);
             *                }
             *              }
             *            } else if (!clockwise && hasPointBehind(p)) {
             *              for (TouchPoint pt : mPoints) {
             *                if(!pt.isBeingTouched && hasPointInFront(pt)) {
             *                  TouchPoint pf = pointInFront(pt);
             *                  pt.mRads = moveRadCCW(pf.mRads, ANGLE_THRESHOLD);
             *                  [>pt.mRads = moveRadCCW(p.mRads, ANGLE_THRESHOLD);<]
             *                }
             *              }
             *            }
             */


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
>>>>>>> parent of 2affab2... very close to getting CCW working in all cases.  need to debug
