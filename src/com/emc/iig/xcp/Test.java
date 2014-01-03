package com.emc.iig.xcp;

import groovy.json.JsonSlurper;

import java.util.HashMap;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DoubanInfo d = new DoubanInfo();
		String douban_id = "6021440";
		String postResult = d.getDoubanJson(douban_id);
		JsonSlurper json = new JsonSlurper();
		//groovy api
		HashMap<?, ?> jsonObject = null;
		try{
			//groovy api
			jsonObject = (HashMap<?, ?>) json.parseText(postResult);
		}catch(IllegalArgumentException e){
			throw new RuntimeException(e.getMessage() + " The douban id is  " + douban_id + "  and The json is null");
		}
	}

}
