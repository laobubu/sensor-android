package net.laobubu.sensor;

import java.util.*;

import net.laobubu.sensor.SensorX.SensorXCallback;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;

public class SensorMain extends Activity 
		implements 
		OnMenuItemClickListener, 
		OnItemSelectedListener, 
		OnCheckedChangeListener, 
		SensorXCallback, 
		OnEditorActionListener
	{


	Spinner lst;
	Vector<SensorX> ss=new Vector<SensorX>();
	ArrayAdapter<String> adapter;
	private EditText txtHost;
	private EditText txtPattern;
	private TextView txtData;
	private TextView txtType;
	private TextView txtModel;
	private ToggleButton itemSwitch;
	private boolean running;
	private boolean locked;
	
	public String host;
	public int port;
	
	Retention conf;
	UDPWorker udp;
	
	PowerManager powerManager;
	WakeLock wakeLock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_main);
		

		powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SENSOR");
		
		udp = new UDPWorker();
		conf = new Retention(getSharedPreferences("q", MODE_PRIVATE));
		
		SensorX.init(this);
		List<Sensor> lS = SensorX.getList();
		String sensorName[]=new String[lS.size()];
		int is=0;
		for(Sensor i:lS) {
			SensorX ii=new SensorX(i);
			ii.index = is;
			ss.add(ii);
			ii.pattern = conf.getData("s"+is, "%f,%f,%f");
			ii.setCallback(this);
			sensorName[is++] = ii.getFrinedlyName() + ":" + ii.sensor.getName();
		}
		
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,sensorName);
		lst = (Spinner) findViewById(R.id.lstSensor);
		lst.setAdapter(adapter);
		lst.setOnItemSelectedListener(this);
		((ToggleButton)findViewById(R.id.mSwitch)).setOnCheckedChangeListener(this);
		
		txtHost = (EditText) findViewById(R.id.txtHost);
		txtPattern = (EditText) findViewById(R.id.txtPattern);
		txtData = (TextView) findViewById(R.id.txtData);
		txtType = (TextView) findViewById(R.id.txtType);
		txtModel = (TextView) findViewById(R.id.txtModel);
		itemSwitch = (ToggleButton) findViewById(R.id.itemSwitch);
		
		txtHost.setOnEditorActionListener(this);
		txtHost.addTextChangedListener(new TextWatcher() {
	          public void afterTextChanged(Editable s) {
	        	  onEditorAction(txtHost,EditorInfo.IME_ACTION_DONE,null);
	          }
	          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	          public void onTextChanged(CharSequence s, int start, int before, int count) {}
	       });
		txtPattern.setOnEditorActionListener(this);
		txtPattern.addTextChangedListener(new TextWatcher() {
	          public void afterTextChanged(Editable s) {
	        	  onEditorAction(txtPattern,EditorInfo.IME_ACTION_DONE,null);
	          }
	          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	          public void onTextChanged(CharSequence s, int start, int before, int count) {}
	       });
		txtHost.setText(conf.getData("host", ""));
		
		itemSwitch.setOnCheckedChangeListener(this);
		onEditorAction(txtHost,EditorInfo.IME_ACTION_DONE,null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sensor_main, menu);
		menu.findItem(R.id.action_exit).setOnMenuItemClickListener(this);
		menu.findItem(R.id.action_help).setOnMenuItemClickListener(this);
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem m) {
		switch (m.getItemId()) {
		case R.id.action_exit:
			this.finish();
			System.exit(0);
			break;
		case R.id.action_help:
			Intent in = new Intent();
			in.setClass(this, HelpActivity.class);
			startActivity(in);
			break;
		}
		return false;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.mSwitch){
			running = isChecked;
			if (running)
				wakeLock.acquire();
			else
				wakeLock.release();
		} else {
			if (!locked) {
				SensorX s = ss.get(lst.getSelectedItemPosition());
				s.setEnable(isChecked);
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		locked = true;
		SensorX s = ss.get(lst.getSelectedItemPosition());
		txtType.setText(s.getFrinedlyName());
		txtModel.setText(s.sensor.getName());
		txtData.setText(s.data);
		txtPattern.setText(s.pattern);
		itemSwitch.setChecked(s.working);
		locked = false;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensor(SensorX x, float v1, float v2, float v3) {
		if (x.index == lst.getSelectedItemPosition()) {
			txtData.setText(x.data);
		}
		if (running)
			udp.send(x.data);
	}

	@Override
	public boolean onEditorAction(TextView t, int a, KeyEvent k) {
		if (a==EditorInfo.IME_ACTION_DONE) {
			if (t.equals(txtHost)) {
				host = txtHost.getText().toString();
				conf.setData("host", host);
				try {
					port = Integer.parseInt(host.substring(host.indexOf(':')+1));
					host = host.substring(0, host.indexOf(':'));
					udp.setHost(host);
					udp.setPort(port);
				} catch (Exception e1) {
					//HOST ERROR
				}
			} else
			if (!locked) {
				SensorX s = ss.get(lst.getSelectedItemPosition());
				s.pattern = txtPattern.getText().toString();
				conf.setData("s" + lst.getSelectedItemPosition(), s.pattern);
			}
		}
		return false;
	}
}
