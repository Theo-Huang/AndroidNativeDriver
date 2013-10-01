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

package com.google.android.testing.nativedriver.server.handler;

import com.google.android.testing.nativedriver.common.AndroidNativeHasTouchScreen;
import com.google.android.testing.nativedriver.common.Touch;
import org.openqa.selenium.remote.server.Session;
import org.openqa.selenium.remote.server.handler.WebDriverHandler;
import org.openqa.selenium.remote.server.rest.ResultType;
import org.openqa.selenium.support.events.EventFiringWebDriver;

/**
 * Handler of {@code /session/:sessionId/buttonup} for interaction of
 * touch screen
 *
 * @author Dezheng Xu
 * @author Theo Huang
 */
public class TouchUp extends WebDriverHandler {
  public TouchUp(Session sessions) {
    super(sessions);
  }

  @Override
  public ResultType call() throws Exception {
  	EventFiringWebDriver driver = (EventFiringWebDriver)getDriver();
		Touch touch = ((AndroidNativeHasTouchScreen) driver.getWrappedDriver()).getTouch();
    touch.touchUp(null);
    return ResultType.SUCCESS;
  }

  @Override
  public String toString() {
    return String.format("touchup on last active coordinates");
  }
}
