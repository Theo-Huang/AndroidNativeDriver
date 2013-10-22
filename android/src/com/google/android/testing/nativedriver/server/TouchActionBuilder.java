package com.google.android.testing.nativedriver.server;

import java.util.LinkedList;
import java.util.List;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.Clock;
import com.google.common.base.Function;
import android.app.Instrumentation;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class TouchActionBuilder {

  private static TouchActionBuilder instance;
  private static final long UNDEFINED_TIME = Long.MIN_VALUE;
  private static final long DEFAULT_PERFORM_SLEEP_INTERVAL = 100;
  private static final long DEFAULT_PERFORM_TIMEOUT = 3000;

  protected static final int DEFAULT_META_STATE = 0;
  protected static final long DURATION = (long) (ViewConfiguration.getLongPressTimeout());
  protected static final long DURATION_OF_LONG_PRESS = (long) (DURATION * 1.5f);
  protected static final long DURATION_BETWEEN_DOUBLE_TAP = (long) (ViewConfiguration.getDoubleTapTimeout() / 1.5f);

  private final Clock clock;
  private final Instrumentation instrumentation;

  private long downTime;
  private List<Action> actionList;

  public static TouchActionBuilder getInstance(Clock clock, Instrumentation instrumentation) {
    if (instance == null) {
      instance = new TouchActionBuilder(clock, instrumentation);
    }
    return instance;
  }

  private TouchActionBuilder(Clock clock, Instrumentation instrumentation) {
    this.clock = clock;
    this.instrumentation = instrumentation;
  }

  public void resetActionList() {
    actionList = new LinkedList<Action>();
  }

  public TouchActionBuilder touchDown(final int x, final int y) {
    resetActionList();
    Action action = new Action() {
      @Override
      public void act() {
        downTime = clock.now();
        MotionEvent motionEvent = MotionEvent.obtain(downTime, clock.now(),
            MotionEvent.ACTION_DOWN, x, y, DEFAULT_META_STATE);
        sendMotionEvent(motionEvent);
      }
    };
    actionList.add(action);
    return this;
  }

  public TouchActionBuilder touchUp(final int x, final int y) {
    Action action = new Action() {

      @Override
      public void act() {
        MotionEvent motionEvent = MotionEvent.obtain(downTime, clock.now(),
            MotionEvent.ACTION_UP, x, y, DEFAULT_META_STATE);
        setTouchStateReleased();
        sendMotionEvent(motionEvent);
      }
    };
    actionList.add(action);
    return this;
  }

  private void setTouchStateReleased() {
    downTime = UNDEFINED_TIME;
  }

  public boolean isTouchStateReleased() {
    return downTime == UNDEFINED_TIME;
  }

  public TouchActionBuilder touchMove(final int x, final int y) {
    Action action = new Action() {

      @Override
      public void act() {
        MotionEvent motionEvent = MotionEvent.obtain(downTime, clock.now(),
            MotionEvent.ACTION_MOVE, x, y, DEFAULT_META_STATE);
        sendMotionEvent(motionEvent);
      }
    };
    actionList.add(action);
    return this;
  }

  public TouchActionBuilder sleep(final long interval) {
    Action action = new Action() {

      @Override
      public void act() {
        try {
          Thread.sleep(interval);
        } catch (InterruptedException exception) {
          Thread.currentThread().interrupt();
          throw new WebDriverException(exception);
        }
      }
    };
    actionList.add(action);
    return this;
  }

  protected void sendMotionEvent(MotionEvent motionEvent) {
    instrumentation.waitForIdleSync();
    try {
      instrumentation.sendPointerSync(motionEvent);
    } catch (Exception e) {
      // ignore
    }
  }

  public void perform() {
    perform(DEFAULT_PERFORM_TIMEOUT);
  }

  public void perform(long timeout) {
    final Thread performThread = new Thread() {
      @Override
      public void run() {
        threadFinishedListener.isFinish = false;
        try {
          for (Action action : actionList) {
            action.act();
          }
        } finally {
          threadFinishedListener.isFinish = true;
        }
      }
    };
    AndroidWait androidWait =
        new AndroidWait(
            new AndroidSystemClock(),
            DEFAULT_PERFORM_SLEEP_INTERVAL,
            timeout);
    try {
      performThread.start();
      androidWait.until(new Function<Void, Boolean>() {
        @Override
        public Boolean apply(Void input) {
          return threadFinishedListener.isFinish && !performThread.isAlive();
        }
      });
    } catch (TimeoutException exception) {
      System.err.println("Drop current event.");
      setTouchStateReleased();
    } finally {
      resetActionList();
      threadFinishedListener.isFinish = true;
    }

  }

  interface Action {
    void act();
  }

}

class threadFinishedListener {
  static boolean isFinish = true;
}
