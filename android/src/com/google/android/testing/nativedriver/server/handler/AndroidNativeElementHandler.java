package com.google.android.testing.nativedriver.server.handler;

import org.openqa.selenium.remote.server.handler.WebElementHandler;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.remote.server.Session;

public abstract class AndroidNativeElementHandler extends WebElementHandler{

  protected AndroidNativeElementHandler(Session session) {
    super(session);
  }

  @Override
  protected WebElement getElement() {
    try {
      WrapsElement proxyElement = (WrapsElement) super.getElement();
      return (((WrapsElement) proxyElement.getWrappedElement()).getWrappedElement());
    } catch (Exception e) {
      throw new WebDriverException("Cannot get AndroidNativeElement", e);
    }
  }
}
