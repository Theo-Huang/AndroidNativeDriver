package com.google.android.testing.nativedriver.server;

import android.view.View;
import android.widget.CheckBox;

/**
 * @author Theo Huang
 * 
 * @param <V>
 */
public class CheckBoxElement<V extends CheckBox> extends ViewElement<V> {

	@SuppressWarnings("hiding")
	public static final ViewElementType TYPE = new ViewElementType(CheckBox.class) {
		@Override
		public ViewElement<?> newInstance(AndroidNativeDriver driver, ElementContext context, View view) {
			return new CheckBoxElement<CheckBox>(driver, context, (CheckBox) view);
		}
	};

	public CheckBoxElement(AndroidNativeDriver driver, ElementContext context, V view) {
		super(driver, context, view);
	}

	@Override
	public String getAttribute(String name) {
		String result = super.getAttribute(name);
		if (result != null) {
			return result;
		}
		if (name.toLowerCase().equals("ischecked")) {
			return String.valueOf(((CheckBox) getView()).isChecked());
		}
		return null;
	}

}
