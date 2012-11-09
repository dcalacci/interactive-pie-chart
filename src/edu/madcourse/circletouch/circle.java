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
	 * Adds categories to the list of categories
	 * @param pCCW - Counter Clockwise point
	 * @param pCW - Clockwise point
	 * @param category - the Category of the slice
	 * @param color - Color of the category
	 */
	private void addCategory(TouchPoint pCCW, TouchPoint pCW, String category,  Color color){
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
	private class Category {
		private TouchPoint pCCW;
		private TouchPoint pCW;
		private String category;
		private Color color; // I think it should be hex / refer to color.xml

		public Category(TouchPoint ccw, TouchPoint cw, String category, Color c ){
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

		public String getCategory(){
			return this.category;
		}

		public Color getColor(){
			return this.color;
		}

		public void setColor(Color color){
			this.color = color;
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