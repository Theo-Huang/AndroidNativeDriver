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

package com.google.android.testing.nativedriver.client;

import com.google.android.testing.nativedriver.common.FindsByText;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.Response;
import java.util.List;

/**
 * Represents an element on the client side. Objects of this type are identical to
 * {@code RemoteWebElement} objects, but in addition support finding by text. The code in this class
 * intercepts calls to the methods on {@code FindsByText} and causes the correct JSON commands to be
 * sent to the remote session.
 * 
 * @author Matt DeVore
 */
public class AndroidNativeElement
		extends RemoteWebElement implements FindsByText {
	/**
	 * Constructs a new instance and sets the parent WebDriver object.
	 * 
	 * @param parent
	 *          the parent WebDriver of the new instance
	 */
	// private long LONG_CLICK_INTERVAL = 1500;
	public AndroidNativeElement(AndroidNativeDriver parent) {
		setParent(Preconditions.checkNotNull(parent));
	}

	@Override
	public WebElement findElementByPartialText(String using) {
		return findElement(USING_PARTIALTEXT, using);
	}

	@Override
	public WebElement findElementByText(String using) {
		return findElement(USING_TEXT, using);
	}

	@Override
	public List<WebElement> findElementsByPartialText(String using) {
		return findElements(USING_PARTIALTEXT, using);
	}

	@Override
	public List<WebElement> findElementsByText(String using) {
		return findElements(USING_TEXT, using);
	}

	public boolean isDisplayed() {
		Response response =
					execute(DriverCommand.IS_ELEMENT_DISPLAYED, ImmutableMap.of("id", id));
		return (Boolean) response.getValue();
	}

	public void drag(int x, int y) {
		execute(DriverCommand.DRAG_ELEMENT, ImmutableMap.of("id", id, "x", x, "y", y));
	}

	public void longClick() {
		execute(DriverCommand.TOUCH_LONG_PRESS, ImmutableMap.of("element", id));
	}

	public void doubleClick() {
		execute(DriverCommand.TOUCH_DOUBLE_TAP, ImmutableMap.of("element", id));
	}

	@Override
	public Coordinates getCoordinates() {
		return new Coordinates() {
			public Point onScreen() {
				return getLocation();
			}

			public Point inViewPort() {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			public Point onPage() {
				return getLocation();
			}

			public Object getAuxiliary() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
	}

}
