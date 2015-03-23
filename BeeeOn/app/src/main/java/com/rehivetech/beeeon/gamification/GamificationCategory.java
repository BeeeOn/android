package com.rehivetech.beeeon.gamification;

import com.rehivetech.beeeon.IIdentifier;

/**
 * @author Jan Lamacz
 */
public class GamificationCategory implements IIdentifier {
  private String mName;
  private String id;
  private int mComplete;
  private int mTotal;

  public GamificationCategory(String id, String name) {
    setId(id);
    setName(name);
  }

  public String getName() {return mName;}
  public void setName(String mName) {this.mName = mName;}

  @Override
  public String getId() {return id;}
  public void setId(String id) {this.id = id;}

  /**
   * Number of total achievements
   */
  public int getTotal() {return mTotal;}
  public void setTotal(int mTotal) {this.mTotal = mTotal;}

  /**
   *  Number of completed achievements
   */
  public int getDone() {return mComplete;}
  public void setDone(int mComplete) {this.mComplete = mComplete;}

  /**
   * Number of earned points
   */
  public int getPoints() {return mComplete*5;}
}
