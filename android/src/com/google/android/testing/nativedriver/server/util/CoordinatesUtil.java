package com.google.android.testing.nativedriver.server.util;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;

public class CoordinatesUtil {
	private Point p1;
	private Point p2;
	private double a;
	private double b;
	private boolean isVertical = false;
	private boolean isHorizontal = false;
	public CoordinatesUtil(Point p1,Point p2){
		this.p1 = p1;
		this.p2 = p2;
		setAB();
	}
	private void setAB(){
		if(p1.x == p2.x){
			isVertical = true;
		}
		if(p1.y == p2.y){
			isHorizontal = true;
		}
		if(isVertical && isHorizontal){
			throw new WebDriverException("Same point");
		}
		if(isHorizontal || isVertical){
			return;
		}
		if(p1.x == 0){
			b = p1.y;
			Point tempPoint = p2;
			p2 = p1;
			p1 = tempPoint;
		}else if(p2.x == 0){
			b = p2.y;
		}
		else{
			double adjust = (double)p1.x/(double)p2.x;
			double temp = adjust*p2.y;
			b = (temp-p1.y)/(adjust-1);
		}
		a = (p1.y-b)/p1.x;
	}
	public double getYbyX(double X){
		if(isVertical){
			throw new WebDriverException("isVertical");
		}
		if(isHorizontal){
			return p1.y;
		}
		return (a*X)+b;
	}
	public double getXbyY(double Y){
		if(isVertical){
			return p1.x;
		}
		if(isHorizontal){
			throw new WebDriverException("isHorizontal");
		}
		return (Y-b)/a;
	}
	public boolean isHorizontal(){
		return isHorizontal;
	}
	public boolean isVertical(){
		return isVertical;
	}

}
