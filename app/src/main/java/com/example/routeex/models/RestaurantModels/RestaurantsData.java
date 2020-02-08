package com.example.routeex.models.RestaurantModels;

import java.util.List;

import com.google.gson.annotations.SerializedName;


public class RestaurantsData {

	@SerializedName("next_page_token")
	private String nextPageToken;

	@SerializedName("html_attributions")
	private List<Object> htmlAttributions;

	@SerializedName("results")
	private List<ResultsItem> results;

	@SerializedName("status")
	private String status;

	public void setNextPageToken(String nextPageToken){
		this.nextPageToken = nextPageToken;
	}

	public String getNextPageToken(){
		return nextPageToken;
	}

	public void setHtmlAttributions(List<Object> htmlAttributions){
		this.htmlAttributions = htmlAttributions;
	}

	public List<Object> getHtmlAttributions(){
		return htmlAttributions;
	}

	public void setResults(List<ResultsItem> results){
		this.results = results;
	}

	public List<ResultsItem> getResults(){
		return results;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}

	@Override
 	public String toString(){
		return 
			"RestaurantsData{" +
			"next_page_token = '" + nextPageToken + '\'' + 
			",html_attributions = '" + htmlAttributions + '\'' + 
			",results = '" + results + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}