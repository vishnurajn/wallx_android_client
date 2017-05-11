package com.yingyang.wallx.models;

public class ItemRecent {
	
 	private String CategoryName;
	private String ImageUrl; 
	
	public ItemRecent(String lcatename, String limage) {
		// TODO Auto-generated constructor stub
		this.CategoryName=lcatename;
		this.ImageUrl=limage;
	}

	public ItemRecent() {
		// TODO Auto-generated constructor stub
	}

	public String getCategoryName() {
		return CategoryName;
	}

	public void setCategoryName(String categoryname) {
		this.CategoryName = categoryname;
	}
	 
	public String getImageurl()
	{
		return ImageUrl;
		
	}
	
	public void setImageurl(String imageurl)
	{
		this.ImageUrl=imageurl;
	}

}
