package net.laobubu.sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.hardware.*;

@SuppressWarnings("deprecation")
public class SensorX implements SensorEventListener {
	public interface SensorXCallback {
		void onSensor(SensorX x, float v1,float v2,float v3);
	}

	public static SensorManager manager;
	public static Map<Integer, String> sensorMap=new HashMap<Integer, String>(); 
	public Sensor sensor;
	public String name;
	private SensorXCallback cb;
	public String pattern = "%f,%f,%f";
	public String data = "";
	public boolean working = false;
	public int index = 0;
	
	public static void init(Context c){
		manager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        sensorMap.put(Sensor.TYPE_ACCELEROMETER, "加速度计");//1  
        //磁场传感器所测得值要经过磁场矩阵的处理  磁场矩阵的值使用的美国航天局 每5年更新一次  
        sensorMap.put(Sensor.TYPE_MAGNETIC_FIELD, "磁场传感器");//2  
        //方向传感器是一个逻辑部件 不是物理部件 实际是由加速度传感器和磁场传感器测得的值 计算出方向  
        sensorMap.put(Sensor.TYPE_ORIENTATION,"方向传感器");//3  
        sensorMap.put(Sensor.TYPE_GYROSCOPE, "陀螺仪");//4  
        sensorMap.put(Sensor.TYPE_LIGHT, "光线传感器");//5  
        sensorMap.put(Sensor.TYPE_PRESSURE, "压力传感器");  
        sensorMap.put(Sensor.TYPE_TEMPERATURE, "温度传感器");  
        sensorMap.put(Sensor.TYPE_PROXIMITY, "接近传感器");//8  
        sensorMap.put(Sensor.TYPE_GRAVITY, "重力传感器");//9  
        sensorMap.put(Sensor.TYPE_AMBIENT_TEMPERATURE, "环境温度传感器");  
	}
	
	public static List<Sensor> getList(){
        List<Sensor> list=manager.getSensorList(Sensor.TYPE_ALL);  
        return list;
	}
	
	public SensorX(Sensor s){
		sensor = s;
		name="["+sensor.getName()+"]";
	}
	
	public void setEnable(boolean enable){
		if (working==enable)
			return;
		working=enable;
		if (enable)
			manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME); //SENSOR_DELAY_NORMAL
		else
			manager.unregisterListener(this);
		data="";
		cb.onSensor(this, 0,0,0);
	}
	
	public void setCallback(SensorXCallback scb){
		cb=scb;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	
	@Override
	public void onSensorChanged(SensorEvent e) {
		float r[]=new float[10];
		for(int i=0;i<e.values.length&&i<3;i++)
			r[i]=e.values[i];
		try {
			data = String.format(pattern, r[0],r[1],r[2],r[3],r[4],r[5],r[6],r[7],r[8],r[9]);
		} catch(Exception err){
			data = "::FAULT::"+err.getMessage();
		}
		cb.onSensor(this, r[0], r[1], r[2]);
	}

	public String getFrinedlyName() {
		try {
			String rr=sensorMap.get(sensor.getType());
			if (rr==null) throw(new Exception());
			return rr;
		} catch(Exception e){
			return "";
		}
	}
}
