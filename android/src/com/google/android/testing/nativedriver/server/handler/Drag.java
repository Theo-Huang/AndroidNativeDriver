package com.google.android.testing.nativedriver.server.handler;

import org.openqa.selenium.remote.server.JsonParametersAware;
import org.openqa.selenium.remote.server.Session;
import org.openqa.selenium.remote.server.rest.ResultType;
import com.google.android.testing.nativedriver.server.AndroidNativeElement;
import java.util.Map;

public class Drag extends AndroidNativeElementHandler implements JsonParametersAware {

  private volatile int x;
  private volatile int y;

  public Drag(Session session) {
    super(session);
  }

  public void setJsonParameters(Map<String, Object> allParameters) throws Exception {
    x = ((Long) allParameters.get("x")).intValue();
    y = ((Long) allParameters.get("y")).intValue();
  }

  public ResultType call() throws Exception {
    AndroidNativeElement element = (AndroidNativeElement) getElement();
    element.dragAndDropTo(x, y);
    return ResultType.SUCCESS;
  }

  @Override
  public String toString() {
    return String.format("[drag element : %s to (x, y): (%d, %d)]", getElementAsString(), x, y);
  }
}
