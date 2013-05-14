package org.dcalacci.android.ipc;

import java.util.ArrayList;
import java.util.Collection;

public class Category implements Comparable<Category>{
  private TouchPoint pCCW;
  private TouchPoint pCW;
  private String name;
  private int color;

  /**
   * Constructs a new name. A name is defined by its' two boundary
   * points - one 'clockwise', one 'counter-clockwise'. A name also
   * has a name, and a color.
   * @param ccw
   *    The counter-clockwise boundary point of this name, in radians.
   * @param cw
   *    The clockwise boundary point of this name, in radians.
   * @param name
   *    The name of this name.
   * @param color
   *    This name's color.
   */
  public Category(TouchPoint ccw, TouchPoint cw, String name, int color ){
    this.pCCW   = ccw;
    this.pCW    = cw;
    this.name   = name;
    this.color  = color;
  }

  public Category() {
  }

  /**
   * Compares this category to another category. Ordering is defined by
   * the clockwise ordering of categories' CCW points, starting from 0.
   */
  public int compareTo(Category c) throws ClassCastException {
    return c.getpCCW().compareTo(this.getpCCW());
  }

  /**
   * Extracts the TouchPoints from a list of Categories.
   * @param cats
   *    The list of categories to extract the TouchPoints from.
   * @return
   *    A list of TouchPoints.
   */
  public static Collection<TouchPoint> getTouchPointsFromCategoryList(
                                                                      Collection<Category> cats) {
    ArrayList<TouchPoint> pts = new ArrayList<TouchPoint>();
    for (Category c : cats) {
      pts.add(c.getpCW());
    }
    return pts;
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
    return this.name;
  }

  public int getColor(){
    return this.color;
  }

  public void setColor(int color){
    this.color = color;
  }

  public String toString(){
    return this.name;
  }
}
