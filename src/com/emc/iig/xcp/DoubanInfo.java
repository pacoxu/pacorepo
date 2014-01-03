package com.emc.iig.xcp;
import groovy.json.JsonSlurper;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfSingleDocbaseModule;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfTime;
public class DoubanInfo 
//extends DfSingleDocbaseModule
{

	
	
	public static final String DOUBAN_URL = "http://api.douban.com/v2/book/";

	public static void newBook(String id) {
		DoubanInfo hpj = new DoubanInfo();
		try {
			hpj.updateDoubanInfo(id);
		} catch (DfException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final String s_docbaseUser = "dmadmin";
	private static final String s_userPassword = "password";
	private static final String s_docbase = "xcp";
	private static IDfSession getSession() throws DfException {
		IDfSessionManager sessionManager = null;

		IDfClientX oCX = new DfClientX();

		sessionManager = oCX.getLocalClient().newSessionManager();
		IDfLoginInfo oLI = oCX.getLoginInfo();

		oLI.setUser(s_docbaseUser);
		oLI.setPassword(s_userPassword);
		sessionManager.setIdentity(s_docbase, oLI);
		return sessionManager.getSession(s_docbase);
	}
	
	
	public void updateDoubanInfo(String douban_id) throws DfException {
		IDfSession session = getSession();
		IDfPersistentObject repoObject = session.newObject("iigbook_book");
		
		getDoubanInfo(repoObject, douban_id );
	}

	private void getDoubanInfo(IDfPersistentObject repoObject, String douban_id) throws DfException {
		
		repoObject.setString("douban_id", douban_id);
		String postResult = getDoubanJson(douban_id);
		JsonSlurper json = new JsonSlurper();
		//groovy api
		HashMap<?, ?> jsonObject = null;
		try{
			//groovy api
			jsonObject = (HashMap<?, ?>) json.parseText(postResult);
		}catch(IllegalArgumentException e){
			throw new RuntimeException(e.getMessage() + " The douban id is  " + douban_id + "  and The json is null");
		}
		String imageUrl = (String) jsonObject.get("image");
		repoObject.setString("image_url", imageUrl);
		String title = (String) jsonObject.get("title");
		repoObject.setString("object_name", title);
		repoObject.setString("title", title);
		String price = (String) jsonObject.get("price");
		repoObject.setString("price", price);

		String summary1 = ((String) jsonObject.get("summary"));
		int  i = 0;
		while(summary1.length() >= 300){
			repoObject.setRepeatingString("book_summary", i , summary1.substring(0,299));
			summary1 = summary1.substring(299);
			i++;
		}
		repoObject.setRepeatingString("book_summary", i , summary1);

		
		String catalog = (String) jsonObject.get("catalog");
		i = 0;
		while(catalog.length() >= 300){
			repoObject.setRepeatingString("catalog", i , catalog.substring(0,299));
			catalog = catalog.substring(299);
			i++;
		}
		repoObject.setRepeatingString("catalog", i , catalog);
		
		List<String> authors =  (List<String>) jsonObject.get("author");

		String author_intro = (String)jsonObject.get("author_intro");
		i = 0;
		while(author_intro.length() >= 300){
			repoObject.setRepeatingString("author_info", i , author_intro.substring(0,299));
			author_intro = author_intro.substring(299);
			i++;
		}
		repoObject.setRepeatingString("author_info", i , author_intro);

		i = 0;
		for (String author : authors){
			repoObject.setRepeatingString("book_authors", i , author);
			i++;
		}

		
		List<HashMap> tags =  (List<HashMap>) jsonObject.get("tags");
		i = 0;
		for (HashMap tag : tags){
//			repoObject.setRepeatingString("tag", i ,  (String) tag.get("name"));
			i++;
		}
		
		String isbn10 = (String) jsonObject.get("isbn10");
		String isbn13 = (String) jsonObject.get("isbn13");
		String page = (String) jsonObject.get("page");
		repoObject.setString("isbn10", isbn10);
		repoObject.setString("isbn13", isbn13);
		int pagei = (page==null)?0:Integer.parseInt(page);
		repoObject.setInt("page", pagei);

		String publish_date = (String) jsonObject.get("publish_date");
		SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
		Date a = null;
		try {
			a = format.parse(publish_date);
			IDfTime date = new DfTime(a);
			repoObject.setTime("publish_date", date  );
		} catch (Exception e) {
			
		}
		List<String> translators =  (List<String>) jsonObject.get("translator");
		String translatorAll= "";
		for (String translator : translators){
			translatorAll = translatorAll + translator;
		}
		repoObject.setString("translator",  translatorAll);


		repoObject.setString("book_status",  "available");

		repoObject.save();
		
		
		System.out.println("Json:" + postResult);
	}
	
	
	
	static String getDoubanJson(String id) {
		return doHttpPost(DOUBAN_URL + id);
	}
	private static String doHttpPost(String URL) {
		String xmlInfo = "";
		byte[] xmlData = xmlInfo.getBytes();
		InputStream instr = null;
		try {
			URL url = new URL(URL);
			URLConnection urlCon = url.openConnection();
			urlCon.setDoOutput(true);
			urlCon.setDoInput(true);
			urlCon.setUseCaches(false);
			urlCon.setRequestProperty("Content-Type", "text/xml");
			urlCon.setRequestProperty("Content-length",
					String.valueOf(xmlData.length));
			DataOutputStream printout = new DataOutputStream(
					urlCon.getOutputStream());
			printout.write(xmlData);
			printout.flush();
			printout.close();
			instr = urlCon.getInputStream();
			byte[] bis = IOUtils.toByteArray(instr);
			String ResponseString = new String(bis, "UTF-8");
			if ((ResponseString == null) || ("".equals(ResponseString.trim()))) {
				System.out.println("nothing found");
			}
			return ResponseString;
		} catch (Exception e) {
			throw new RuntimeException(URL + " is not accessible or with some errors. "+ e.getMessage());
		}
		finally {
			try {
				instr.close();
			} catch (Exception ex) {

			}
		}
	}

}