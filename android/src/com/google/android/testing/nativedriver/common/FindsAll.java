package com.google.android.testing.nativedriver.common;

import java.util.List;

import org.openqa.selenium.WebElement;

public interface FindsAll {
	String CURRENT_ACTIVITY = "CurrentActivity";
	List<WebElement> findAllElements();
	List<WebElement> findAllElementsByCurrentActivity();
}
