package com.example.routeex.models.RestaurantModels;


import com.google.gson.annotations.SerializedName;


public class OpeningHours{

	@SerializedName("open_now")
	private boolean openNow;

	public void setOpenNow(boolean openNow){
		this.openNow = openNow;
	}

	public boolean isOpenNow(){
		return openNow;
	}

	@Override
 	public String toString(){
		return 
			"OpeningHours{" + 
			"open_now = '" + openNow + '\'' + 
			"}";
		}
}