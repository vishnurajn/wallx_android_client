package com.yingyang.wallx.models;

public class ItemWallpaperByCategory {


	private String ItemCategoryName;
	private String ItemImageUrl;
	private String ItemCatId;
 

	public ItemWallpaperByCategory(String itemcategoryname, String itemimageurl, String itemcatid) {
		// TODO Auto-generated constructor stub
		this.ItemCategoryName=itemcategoryname;
		this.ItemImageUrl=itemimageurl;
		this.ItemCatId=itemcatid;
	}

	public ItemWallpaperByCategory() {
		// TODO Auto-generated constructor stub
	}

	public String getItemCategoryName() {
		return ItemCategoryName;
	}

	public void setItemCategoryName(String itemcategoryname) {
		this.ItemCategoryName = itemcategoryname;
	}


	public String getItemImageurl()
	{
		return ItemImageUrl;

	}

	public void setItemImageurl(String itemimageurl)
	{
		this.ItemImageUrl=itemimageurl;
	}
	public String getItemCatId()
	{
		return ItemCatId;

	}

	public void setItemCatId(String itemcatid)
	{
		this.ItemCatId=itemcatid;
	}


}
