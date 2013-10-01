/*
Copyright 2007-2011 NativeDriver committers
Copyright 2007-2011 Google Inc.

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

import com.google.common.collect.Lists;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.remote.server.JsonParametersAware;
import org.openqa.selenium.remote.server.handler.WebDriverHandler;
import org.openqa.selenium.remote.server.rest.ResultType;
import org.openqa.selenium.remote.server.Session;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A handler to send key events via Android Instrumentation to the
 * currently-running process.
 *
 * @author Steve Salevan
 */
public class AndroidNativeSendKeys extends WebDriverHandler
    implements JsonParametersAware {
  private final List<CharSequence> keys
      = new CopyOnWriteArrayList<CharSequence>();

  public AndroidNativeSendKeys(Session sessions) {
    super(sessions);
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public void setJsonParameters(Map<String, Object> allParameters)
      throws Exception {
    List<String> rawKeys = (List<String>) allParameters.get("value");
    List<String> temp = Lists.newArrayList();

    for (String key : rawKeys) {
      temp.add(key);
    }
    keys.addAll(temp);
  }

  @Override
  public ResultType call() throws Exception {
    String[] keysToSend = keys.toArray(new String[0]);
    ((HasInputDevices) getDriver()).getKeyboard().sendKeys(keysToSend);
    return ResultType.SUCCESS;
  }

  @Override
  public String toString() {
    return String.format("[send keys: %s]", keys);
  }
}
