/*
Copyright 2011 NativeDriver committers
Copyright 2011 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.google.android.testing.nativedriver.server;

import java.util.ArrayList;
import com.google.android.testing.nativedriver.common.Touch;
import com.google.android.testing.nativedriver.server.util.CoordinatesUtil;
import com.google.common.base.Preconditions;
import android.app.Instrumentation;
import android.view.ViewConfiguration;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.support.ui.Clock;
import javax.annotation.Nullable;

/**
 * {@code Touch} interface implementation.
 * 
 * @author Dezheng Xu
 * @author Theo Huang
 */
public class AndroidNativeTouch implements Touch {
  // Use as the last argument for creating MotionEvent instance
  protected static final int DEFAULT_META_STATE = 0;
  protected static final long DURATION = (long) (ViewConfiguration.getLongPressTimeout());

  // The duration in milliseconds before a press turns into a long press,
  // "x1.5" to ensure.
  protected static final long DURATION_OF_LONG_PRESS = (long) (DURATION * 1.5f);
  // The duration in milliseconds between the first tap's up event and second
  // tap's down event for an interaction to be considered a double-tap.
  // "/1.5" to ensure.
  protected static final long DURATION_BETWEEN_DOUBLE_TAP = (long) (ViewConfiguration.getDoubleTapTimeout() / 1.5f);
  protected static final long DRAG_TIMEOUT = 10000;
  // private static final long UNDEFINED_TIME = Long.MIN_VALUE;
  private final Instrumentation instrumentation;
  private final Clock clock;
  private Coordinates currentActiveCoordinates;
  protected int SCROLL_PIXEL = 25;

  // We are only accessing downTime in synchronized blocks
  // private long downTime = UNDEFINED_TIME;

  private boolean isTouchStateReleased = true;
  private TouchActionBuilder touchActionBuilder;

  public AndroidNativeTouch(Clock clock, Instrumentation instrumentation) {
    this.clock = clock;
    this.instrumentation = instrumentation;
  }

  public static AndroidNativeTouch withDefaults(
      Instrumentation instrumentation) {
    Clock clock = new AndroidSystemClock();
    return new AndroidNativeTouch(clock, instrumentation);
  }

  @Override
  public synchronized void tap(@Nullable Coordinates where) {
    if (!isTouchStateReleased()) {
      throw new IllegalStateException(
          "Attempt to tap when touch state is already down");
    }
    updateActiveCoordinates(where);
    Point point = currentActiveCoordinates.onScreen();
    tap(point.getX(), point.getY());
  }

  @Override
  public synchronized void doubleTap(@Nullable Coordinates where) {
    if (!isTouchStateReleased()) {
      throw new IllegalStateException(
          "Attempt to double tap when touch state is already down");
    }
    updateActiveCoordinates(where);
    Point point = currentActiveCoordinates.onScreen();
    doubleTap(point.getX(), point.getY());
  }

  @Override
  public synchronized void touchDown(@Nullable Coordinates where) {
    if (!isTouchStateReleased()) {
      throw new IllegalStateException(
          "Attempt to touch down when touch state is already down");
    }
    updateActiveCoordinates(where);
    Point point = currentActiveCoordinates.onScreen();
    touchDown(point.getX(), point.getY());
  }

  @Override
  public synchronized void touchUp(@Nullable Coordinates where) {
    if (isTouchStateReleased()) {
      throw new IllegalStateException(
          "Attempt to release touch when touch is already released");
    }
    updateActiveCoordinates(where);
    Point point = currentActiveCoordinates.onScreen();
    touchUp(point.getX(), point.getY());
  }

  @Override
  public synchronized void touchMove(Coordinates where) {
    Preconditions.checkNotNull(where);
    updateActiveCoordinates(where);
    if (!isTouchStateReleased()) {
      Point point = where.onScreen();
      touchMove(point.getX(), point.getY());
    }
  }

  @Override
  public synchronized void touchMove(
      Coordinates where, long xOffset, long yOffset) {
    // Even Mouse.mouseMove(Coordinates where, long xOffset, long yOffset) is
    // not supported yet in current WebDriver implementation.
    throw new UnsupportedOperationException(
        "Moving to arbitrary (x, y) coordinates not supported.");
  }

  @Override
  public synchronized void longClick(Coordinates where) {
    longClick(where, DURATION_OF_LONG_PRESS);
  }

  @Override
  public synchronized void longClick(Coordinates where, long sleepInterval) {
    if (!isTouchStateReleased()) {
      throw new IllegalStateException(
          "Attempt to longclick when touch state is already down");
    }
    updateActiveCoordinates(where);
    Point point = currentActiveCoordinates.onScreen();
    longClick(point.getX(), point.getY(), sleepInterval);
  }

  protected void touchDown(int x, int y) {
    // downTime = clock.now();
    // MotionEvent motionEvent = MotionEvent.obtain(downTime, clock.now(),
    // MotionEvent.ACTION_DOWN, x, y, DEFAULT_META_STATE);
    // sendMotionEvent(motionEvent, false);
    TouchActionBuilder actionBuilder = getActionBuilder();
    actionBuilder.touchDown(x, y).perform();
    isTouchStateReleased = actionBuilder.isTouchStateReleased();
  }

  protected void touchUp(int x, int y) {
    // MotionEvent motionEvent = MotionEvent.obtain(downTime, clock.now(),
    // MotionEvent.ACTION_UP, x, y, DEFAULT_META_STATE);
    // setTouchStateReleased();
    // sendMotionEvent(motionEvent, true);
    TouchActionBuilder actionBuilder = getActionBuilder();
    actionBuilder.touchUp(x, y).perform();
    isTouchStateReleased = actionBuilder.isTouchStateReleased();
  }

  protected void touchMove(int x, int y) {
    // MotionEvent motionEvent = MotionEvent.obtain(downTime, clock.now(),
    // MotionEvent.ACTION_MOVE, x, y, DEFAULT_META_STATE);
    // sendMotionEvent(motionEvent, false);
    TouchActionBuilder actionBuilder = getActionBuilder();
    actionBuilder.touchMove(x, y).perform();
    isTouchStateReleased = actionBuilder.isTouchStateReleased();
  }

  protected void tap(int x, int y) {
    int scaledTouchSlopAdjustment = getScaledTouchSlopAdjustment();
    int adjX = x + scaledTouchSlopAdjustment;
    int adjY = y + scaledTouchSlopAdjustment;
    // touchDown(x, y);
    // touchMove(adjX, adjY);
    // touchUp(x, y);
    TouchActionBuilder actionBuilder = getActionBuilder();
    actionBuilder.touchDown(x, y).touchMove(adjX, adjY).touchUp(x, y).perform();
    isTouchStateReleased = actionBuilder.isTouchStateReleased();
  }

  protected void longClick(int x, int y, long sleepInterval) {
    // touchDown(x, y);
    int scaledTouchSlopAdjustment = getScaledTouchSlopAdjustment();
    int adjX = x + scaledTouchSlopAdjustment;
    int adjY = y + scaledTouchSlopAdjustment;
    // touchMove(x + scaledTouchSlopAdjustment, y + scaledTouchSlopAdjustment);
    // sleep(sleepInterval);
    // touchUp(x, y);
    TouchActionBuilder actionBuilder = getActionBuilder();
    actionBuilder
        .touchDown(x, y)
        .touchMove(adjX, adjY)
        .sleep(sleepInterval)
        .touchUp(x, y).perform(sleepInterval * 2);
    isTouchStateReleased = actionBuilder.isTouchStateReleased();
  }

  protected void doubleTap(int x, int y) {
    // tap(x, y);
    // sleep(DURATION_BETWEEN_DOUBLE_TAP);
    // tap(x, y);

    TouchActionBuilder actionBuilder = getActionBuilder();
    int scaledTouchSlopAdjustment = getScaledTouchSlopAdjustment();
    int adjX = x + scaledTouchSlopAdjustment;
    int adjY = y + scaledTouchSlopAdjustment;
    actionBuilder.touchDown(x, y).touchMove(adjX, adjY).touchUp(x, y);
    actionBuilder.sleep(DURATION_BETWEEN_DOUBLE_TAP);
    actionBuilder.touchDown(x, y).touchMove(adjX, adjY).touchUp(x, y);
    actionBuilder.perform(DURATION_BETWEEN_DOUBLE_TAP * 2);
    isTouchStateReleased = actionBuilder.isTouchStateReleased();
  }

  @Override
  public void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new WebDriverException(exception);
    }
  }

  @Override
  public long getDuration() {
    return DURATION;
  }

  protected boolean isTouchStateReleased() {
    // return downTime == UNDEFINED_TIME;
    return isTouchStateReleased;
  }

  protected int getScaledTouchSlopAdjustment() {
    int touchSlop = ViewConfiguration.get(instrumentation.getContext())
        .getScaledTouchSlop();
    return touchSlop / 4;
  }

  private TouchActionBuilder getActionBuilder() {
    if (touchActionBuilder == null) {
      touchActionBuilder = TouchActionBuilder.getInstance(clock, instrumentation);
    }
    touchActionBuilder.resetActionList();
    return touchActionBuilder;
  }

  private void updateActiveCoordinates(Coordinates coordinates) {
    if (coordinates != null) {
      currentActiveCoordinates = coordinates;
    } else if (currentActiveCoordinates == null) {
      throw new IllegalStateException(
          "No current active coordinates and given coordinates is null.");
    }
  }

  // private void setTouchStateReleased() {
  // downTime = UNDEFINED_TIME;
  // }

  @Override
  public synchronized void singleTap(Coordinates where) {
    tap(where);
  }

  @Override
  public synchronized void up(int x, int y) {
    touchUp(x, y);
  }

  @Override
  public synchronized void down(int x, int y) {
    touchDown(x, y);
  }

  @Override
  public synchronized void flick(int arg0, int arg1) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("in flick(int arg0, int arg1)");
  }

  @Override
  public synchronized void flick(Coordinates where, int x, int y, int speed) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("in flick(Coordinates arg0, int arg1, int arg2, int arg3)");
  }

  // different with longClick, it take double times with hold
  @Override
  public synchronized void longPress(Coordinates where) {
    longClick(where, DURATION_OF_LONG_PRESS * 2);
  }

  @Override
  public synchronized void move(int x, int y) {
    touchMove(x, y);
  }

  @Override
  public synchronized void scroll(int arg0, int arg1) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("in  scroll(int arg0, int arg1) ");
  }

  @Override
  public synchronized void scroll(Coordinates where, int xEnd, int yEnd) {
    Preconditions.checkNotNull(where);
    updateActiveCoordinates(where);
    Point startPoint = where.onScreen();
    try {
      fluentMove(startPoint.getX(), startPoint.getY(), xEnd, yEnd);
    } catch (Exception e) {
      throw new WebDriverException(
          String.format("May pass error points (%s,%s)-(%s,%s)",
              startPoint.getX(), startPoint.getY(), xEnd, yEnd));
    } finally {
      if (!isTouchStateReleased()) {
        touchUp(xEnd, yEnd);
      }
    }
  }

  // backup solution
  // protected void drag(float xStart, float yStart, float xEnd, float yEnd, int stepCount) {
  // float x = xStart;
  // float y = yStart;
  // float intervalX = (xEnd - xStart);
  // float intervalY = (yEnd - yStart);
  // float xAdjust = intervalX / stepCount;
  // float yAdjust = intervalY / stepCount;
  // intervalX = Math.abs(intervalX);
  // intervalY = Math.abs(intervalY);
  // touchDown((int) xStart, (int) yStart);
  // ArrayList<Point> pointList = new ArrayList<Point>();
  // do {
  // x += xAdjust;
  // y += yAdjust;
  // pointList.add(new Point((int) x, (int) y));
  // } while (!(Math.abs(xStart - x) >= intervalX && Math.abs(yStart - y) >= intervalY));
  //
  // for (Point p : pointList) {
  // touchMove(p.getX(), p.getY());
  // // sleep(DURATION_OF_SCROLL);
  // }
  // touchUp((int) xEnd, (int) yEnd);
  // }

  public void fluentMove(int xStart, int yStart, int xEnd, int yEnd) {
    final CoordinatesUtil cu =
        new CoordinatesUtil(new Point(xStart, yStart), new Point(xEnd, yEnd));
    int intervalX = Math.abs(xStart - xEnd);
    int intervalY = Math.abs(yStart - yEnd);
    abstract class VorH {
      protected int adjust;
      protected int countX;
      protected int countY;

      VorH(int adjust, int countX, int countY) {
        this.adjust = adjust;
        this.countX = countX;
        this.countY = countY;
      }

      public abstract void add();

      public int getCountX() {
        return this.countX;
      }

      public int getCountY() {
        return this.countY;
      }
    }
    // prevent scroll to much
    if (intervalX < SCROLL_PIXEL && intervalY < SCROLL_PIXEL) {
      if (intervalX < intervalY) {
        SCROLL_PIXEL = intervalX;
      } else {
        SCROLL_PIXEL = intervalY;
      }
    }
    VorH VH;
    if (!cu.isVertical() && intervalX > intervalY) {
      VH = new VorH((xStart < xEnd) ? SCROLL_PIXEL : SCROLL_PIXEL * -1,
          xStart, yStart) {
        @Override
        public void add() {
          countX += adjust;
          countY = (int) cu.getYbyX((double) countX);
        }
      };
    } else {
      VH = new VorH((yStart < yEnd) ? SCROLL_PIXEL : SCROLL_PIXEL * -1,
          xStart, yStart) {
        @Override
        public void add() {
          countY += adjust;
          countX = (int) cu.getXbyY((double) countY);
        }
      };
    }
    // for performance pre store.
    Point addPoint;
    Point lastPoint;
    int size;
    ArrayList<Point> pointList = new ArrayList<Point>();
    do {
      VH.add();
      // don't know why Vertical or Horizontal perform slowly.
      // So work around with this.
      addPoint = new Point(VH.getCountX(), VH.getCountY());
      size = pointList.size();
      if (size > 0) {
        lastPoint = pointList.get(size - 1);
        if (addPoint.getX() == lastPoint.getX()) {
          addPoint.x++;
        }
        if (addPoint.getY() == lastPoint.getY()) {
          addPoint.y++;
        }
      }
      pointList.add(addPoint);
    } while (!(Math.abs(xStart - VH.getCountX()) >= intervalX && Math.abs(yStart - VH.getCountY()) >= intervalY));
    TouchActionBuilder actionBuilder = getActionBuilder();
    if (isTouchStateReleased()) {
      actionBuilder.touchDown(xStart, yStart);
    }
    for (Point p : pointList) {
      actionBuilder.touchMove(p.getX(), p.getY());
    }
    actionBuilder.perform(DRAG_TIMEOUT);
    isTouchStateReleased = actionBuilder.isTouchStateReleased();
  }
  // TODO(dxu): think about how to deal with ACTION_CANCEL

}
