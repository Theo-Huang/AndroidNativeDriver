package com.google.android.testing.nativedriver.server.handler;

import java.util.Map;
import org.openqa.selenium.remote.server.JsonParametersAware;
import org.openqa.selenium.remote.server.Session;
import org.openqa.selenium.remote.server.handler.WebDriverHandler;
import org.openqa.selenium.remote.server.rest.ResultType;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import com.google.android.testing.nativedriver.common.AndroidNativeHasTouchScreen;
import com.google.android.testing.nativedriver.common.Touch;
import com.google.android.testing.nativedriver.server.util.ConvenienceCoordinates;


/**
 * @author Theo Huang
 */
public class Scroll extends WebDriverHandler implements JsonParametersAware{
	private volatile int xStart;
	private volatile int yStart;
	private volatile int xEnd;
	private volatile int yEnd;
	public Scroll(Session sessions){
		super(sessions);
	}

	@Override
	public ResultType call() throws Exception {
		EventFiringWebDriver driver = (EventFiringWebDriver)getDriver();
		Touch touch = ((AndroidNativeHasTouchScreen) driver.getWrappedDriver()).getTouch();
		touch.scroll(new ConvenienceCoordinates(xStart,yStart),xEnd,yEnd);
		return ResultType.SUCCESS;
	}
	
	@Override
	public void setJsonParameters(Map<String, Object> Parameters) throws Exception {
		xStart = Integer.parseInt(Parameters.get("xStart").toString());
		yStart = Integer.parseInt(Parameters.get("yStart").toString());
		xEnd = Integer.parseInt(Parameters.get("xEnd").toString());
		yEnd = Integer.parseInt(Parameters.get("yEnd").toString());
	}
	
	@Override
	public String toString(){
		return String.format("[Scroll (%s,%s) to (%s,%s)]",xStart,yStart,xEnd,yEnd);
	}
}
