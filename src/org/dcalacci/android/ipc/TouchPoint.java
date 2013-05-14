package org.dcalacci.android.ipc;

/**
 * Container for touch points
 */
public class TouchPoint implements Comparable<TouchPoint> {
  private double mRads; // the radian value of this touchpoint
  public boolean isBeingTouched = false; // true if this point is being touched


  /**
   * Constructs a default TouchPoint with a radian value of 30
   */
  public TouchPoint() {
    super();
    this.mRads = 30;
  }

  /**
   * Constructs a TouchPoint
   * @param mRads
   *  The radian value of this touchpoint.
   */
  public TouchPoint(double mRads) {
    super();
    this.mRads = mRads;
  }

  /**
   * positive if getDifference(this, tp) is positive, 0 if it's 0,
   * negative if it's negative.
   * @param  tp The touchpoint to compare this to
   */

  /**
   * Compare this TouchPoint to the given TouchPoint. Two TouchPoints are
   * compared relative to this TouchPoint.
   *
   * If the given TouchPoint, tp, is within pi radians ahead (clockwise) of
   * this touchpoint, tp is greater than this TouchPoint.
   *
   * If the given TouchPoint is within pi radians behind
   * (counterclockwise) of this touchpoint, then tp is less than this
   * TouchPoint.
   *
   * If the given TouchPoint is exactly pi radians away from this
   * TouchPoint, then the given TouchPoint is greater than this TouchPoint
   *
   * They're equal if there is no difference between their radian values.
   *
   * @param tp
   *  The TouchPoint to compare this to
   * @return
   *  0 if this and tp are equal;
   *  a positive value if this is greater than tp;
   *  a negative value if this is less than tp.
   */
  public int compareTo(TouchPoint tp) throws ClassCastException {
    if (getDifference(this.mRads, tp.mRads) == 0) {
      return 0;
    } else {
      return getDifference(this.mRads, tp.mRads) > 0 ? 1 : -1;
    }
  }

  /**
   * Returns the number of clockwise radians between the first and second
   * point, moving clockwise from the first point.
   * @param
   *  tp1 The first point.
   * @param
   *  tp2 The second point.
   */
  public static double getDistCW(TouchPoint tp1, TouchPoint tp2) {
    double diff = getDifference(tp1.mRads, tp2.mRads);
    if (diff < 0) {
      diff = Math.PI*2 + diff;
    }
    return diff;
  }

  /**
   * @return
   *  This TouchPoint's radian value
   */
  public double getmRads() {
    return this.mRads;
  }

  public void setmRads(Double rads) {
    this.mRads = rads;
  }

  /**
   * Returns the difference between two radian values, negative if end is
   * under pi rads away from start, ccw, positive otherwise.
   * Computes the difference between two radian values.
   * @param start
   *  The first angle
   * @param end
   *  The second angle
   * @return
   *  0 if they are the same;
   *  A negative value if end is within pi radians (ccw) away from start;
   *  A positive value otherwise.
   */
  private static double getDifference(double start, double end) {
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

}
