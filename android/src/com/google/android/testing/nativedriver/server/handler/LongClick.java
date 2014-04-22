package com.google.android.testing.nativedriver.server.handler;

import java.util.Map;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.server.JsonParametersAware;
import org.openqa.selenium.remote.server.Session;
import org.openqa.selenium.remote.server.handler.WebDriverHandler;
import org.openqa.selenium.remote.server.rest.ResultType;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import com.google.android.testing.nativedriver.common.AndroidNativeHasTouchScreen;
import com.google.android.testing.nativedriver.common.Touch;

/**
 * @author Theo Huang
 */
public class LongClick extends WebDriverHandler implements JsonParametersAware {
	private static final String ELEMENT = "element";
	private volatile String elementId;

	public LongClick(Session session) {
		super(session);
	}

	@Override
	public ResultType call() throws Exception {
		EventFiringWebDriver driver = (EventFiringWebDriver)getDriver();
		Touch touch = ((AndroidNativeHasTouchScreen) driver.getWrappedDriver()).getTouch();
		WebElement element = getKnownElements().get(elementId);
		Coordinates elementLocation = ((Locatable) element).getCoordinates();
		touch.longPress(elementLocation);
		return ResultType.SUCCESS;
	}

	@Override
	public String toString() {
		return String.format("[long click element]");
	}

	@Override
	public void setJsonParameters(Map<String, Object> allParameters) throws Exception {
		elementId = (String) allParameters.get(ELEMENT);
	}

}
