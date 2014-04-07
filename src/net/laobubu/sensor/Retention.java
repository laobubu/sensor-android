package net.laobubu.sensor;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Retention {

	private SharedPreferences sp;
	private Editor editor;
	
	public Retention(SharedPreferences sp1) {
		sp=sp1;
	}
	
	public String getData(String key,String def){
		return sp.getString(key, def);
	}
	
	public void setData(String key,String val){
		editor = sp.edit();
		editor.putString(key, val);
		editor.commit();
	}

}
