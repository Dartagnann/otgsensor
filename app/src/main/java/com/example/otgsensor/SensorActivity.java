<<<<<<< HEAD
package com.example.otgsensor;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static tool.ServerIP.POSTURL;
import static tool.SharedPreferenceHelper.getTableNameBySP;

public class SensorActivity extends AppCompatActivity {
    public LocationClient mLocationClient;
    public String USERNAME;
    private TextView positionText, latitudeText, longitudeText;//position
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean  isFirstLocate = true;
    private MyDatabaseHelper dbHelper;

    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
    Button button_begin,button_stop,button_upload,button_save,button_reset,bt_temp,bt_humi,bt_pres,bt_illu,bt_soil_t,bt_soil,bt_zwx;
    TextView textView1,textView2,textView3,textView4,textView5,textView6,textView7;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    String data="";
    String cdate="";

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                data= new String(arg0, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);

                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            setUiEnabled(true);
                            serialPort.setBaudRate(115200);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            //tvAppend(textView,"Serial Connection Opened!\n");
                            Toast.makeText(SensorActivity.this, "设备已上线",Toast.LENGTH_SHORT).show();

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart(button_begin);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStop(button_stop);

            }
        }

        ;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());//先初始化
        setContentView(R.layout.activity_sensor);

        mapView = findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        USERNAME = getTableNameBySP(this);//以用户名作为表名

        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        button_begin = (Button) findViewById(R.id.button_begin);
        bt_temp = (Button) findViewById(R.id.bt_temp);
        bt_humi= (Button) findViewById(R.id.bt_humi);
        bt_pres = (Button) findViewById(R.id.bt_pres);
        bt_illu = (Button) findViewById(R.id.bt_illu);
        bt_soil_t=findViewById(R.id.bt_soil_t);
        bt_soil = (Button) findViewById(R.id.bt_soil);
        bt_zwx = (Button) findViewById(R.id.bt_zwx);
        button_stop = (Button) findViewById(R.id.button_stop);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (TextView) findViewById(R.id.textView5);
        textView6 = (TextView) findViewById(R.id.textView6);
        textView7 = (TextView) findViewById(R.id.textView7);
        setUiEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

        //下面创建LocationClient的实例
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        positionText = findViewById(R.id.textView_location);
        latitudeText = findViewById(R.id.textView_lati);
        longitudeText = findViewById(R.id.textView_longi);
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(SensorActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(SensorActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(SensorActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(SensorActivity.this,permissions,1);
        }
        else {
            requestLocation();
        }
        //下面是创建数据库相关代码
        dbHelper = new MyDatabaseHelper(this, "CollectedData.db", null, 1);
        dbHelper.getWritableDatabase();
        /*dbHelper = new MyDatabaseHelper(this, "CollectedData.db", null, 1);
        Button createDatabase = findViewById(R.id.button_begin);
        createDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.getWritableDatabase();
            }
        });*/
        //下面是添加数据库记录代码
        Button addData = findViewById(R.id.button_save);
        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp1 = textView1.getText().toString();
                String humidity1 = textView2.getText().toString();
                String pressure1 = textView3.getText().toString();
                String illumination1 = textView4.getText().toString();
                String soilt1 = textView5.getText().toString();
                String soilh1 = textView6.getText().toString();
                String uv1 = textView7.getText().toString();
                String longitude1 = longitudeText.getText().toString();
                String latitude1 = latitudeText.getText().toString();
                float temp = Float.parseFloat(temp1);
                float humidity = Float.parseFloat(humidity1);
                float pressure = Float.parseFloat(pressure1);
                float illumination = Float.parseFloat(illumination1);
                float soil_t = Float.parseFloat(soilt1);
                float soil_h = Float.parseFloat(soilh1);
                float uv = Float.parseFloat(uv1);
                float longitude = Float.parseFloat(longitude1);
                float latitude = Float.parseFloat(latitude1);
                java.util.Date date = new java.util.Date();
                SimpleDateFormat formatter = new SimpleDateFormat ("yyyy年MM月dd日 HH:mm:ss");
                Date date1 = new Date(System.currentTimeMillis());
                //DateFormat dateFormat = DateFormat.getDateInstance();
                String sTime = formatter.format(date1);
                //String str_date = dateFormat.format(date1);
                //SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                //Date curDate = new Date(System.currentTimeMillis());
                //String date = formatter.format(curDate);

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("temp", temp);
                values.put("date", sTime);
                values.put("humidity", humidity);
                values.put("pressure", pressure);
                values.put("illumination", illumination);
                values.put("soil_t", soil_t);
                values.put("soil_h", soil_h);
                values.put("uv", uv);
                values.put("illumination", illumination);
                values.put("illumination", illumination);
                values.put("longitude", longitude);
                values.put("latitude", latitude);
                db.insert("Data", null, values);
                /*values.put("temp", "1");
                values.put("date", date.getTime());
                values.put("humidity", "2");
                values.put("pressure", "3");
                values.put("illumination", "4");
                values.put("longitude", "5");
                values.put("latitude", "6");*/
                db.insert("Data", null, values);

                Toast.makeText(SensorActivity.this, "保存成功！", Toast.LENGTH_LONG).show();
            }
        });
        //下面是上传数据代码
        Button upData = findViewById(R.id.button_upload);
        upData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp1 = textView1.getText().toString();
                String humidity1 = textView2.getText().toString();
                String pressure1 = textView3.getText().toString();
                String illumination1 = textView4.getText().toString();
                String soilt1 = textView5.getText().toString();
                String soilh1 = textView6.getText().toString();
                String uv1 = textView7.getText().toString();
                String longitude1 = longitudeText.getText().toString();
                String latitude1 = latitudeText.getText().toString();
                String posturl = POSTURL;
                /*float temp = Float.parseFloat(temp1);
                float humidity = Float.parseFloat(humidity1);
                float pressure = Float.parseFloat(pressure1);
                float illumination = Float.parseFloat(illumination1);
                float soil_t = Float.parseFloat(soilt1);
                float soil_h = Float.parseFloat(soilh1);
                float uv = Float.parseFloat(uv1);
                float longitude = Float.parseFloat(longitude1);
                float latitude = Float.parseFloat(latitude1);*/
                upLoad(posturl,temp1,humidity1,pressure1,illumination1,soilt1,soilh1,uv1,longitude1,latitude1);
            }
        });
    }
    private void navigateTo(final BDLocation location){
        if (isFirstLocate){
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatus mapStatus = new MapStatus.Builder()
                    .target(ll)
                    .zoom(17)
                    .build();
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
            baiduMap.setMapStatus(mapStatusUpdate);
            //MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            //baiduMap.animateMapStatus(update);
            //update = MapStatusUpdateFactory.zoomTo(16f);
            //baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }
    @Override
    protected  void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected  void onPause(){
        super.onPause();
        mapView.onPause();
    }
    private  void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        //option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);
    }


    private void upLoad(String posturl,final String temp1, String humidity1, String pressure1, String illumination1, String soilt1, String soilh1, String uv1, String longitude1, String latitude1){

                    OkHttpClient client = new OkHttpClient();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");// HH:mm:ss
                    //获取当前时间
                    Date date = new Date(System.currentTimeMillis());
                    String cdate = simpleDateFormat.format(date);
                    FormBody.Builder formBuilder = new FormBody.Builder();
                    formBuilder.add("username", USERNAME);//USERNAME就是username
                    formBuilder.add("date", cdate);
                    formBuilder.add("temp", temp1);
                    formBuilder.add("humidity", humidity1);
                    formBuilder.add("pressure", pressure1);
                    formBuilder.add("illumination", illumination1);
                    formBuilder.add("soil_t", soilt1);
                    formBuilder.add("soil_h", soilh1);
                    formBuilder.add("uv", uv1);
                    formBuilder.add("longitude", longitude1);
                    formBuilder.add("latitude", latitude1);
                    Request request = new Request.Builder().url(posturl).post(formBuilder.build()).build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable()
                        {
                    @Override
                    public void run()
                    {
                        Toast.makeText(SensorActivity.this, "服务器错误", Toast.LENGTH_SHORT).show();
                    }
                        });
                    }
                    @Override
                        public void onResponse(Call call, Response response) throws IOException
                        {
                            final String res = response.body().string();
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (res.equals("0"))
                                    {
                                        Toast.makeText(SensorActivity.this, "请勿重复上传", Toast.LENGTH_SHORT).show();
                                    }
                                    else if (res.equals("1"))
                                    {
                                        Toast.makeText(SensorActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(SensorActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location){

            final int errorCode = location.getLocType();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
                    StringBuilder currentLatitude = new StringBuilder();
                    StringBuilder currentLongitude = new StringBuilder();
                    currentLatitude.append(location.getLatitude()).append("\n");
                    currentLongitude.append(location.getLongitude()).append("\n");
                    //currentPosition.append("国家：").append(location.getCountry()).append("\n");
            currentPosition.append("省：").append(location.getProvince()).append("\n");
            currentPosition.append("市：").append(location.getCity()).append("\n");
           currentPosition.append("区：").append(location.getDistrict()).append("\n");
            currentPosition.append("街道：").append(location.getStreet()).append("\n");
            currentPosition.append("").append(location.getLocationDescribe()).append("\n");
                    navigateTo(location);
                    currentPosition.append("定位方式:");
                    if (location.getLocType()==BDLocation.TypeGpsLocation){
                        currentPosition.append("GPS");navigateTo(location);
                    }else if (location.getLocType()==BDLocation.TypeNetWorkLocation){
                        currentPosition.append("网络");navigateTo(location);
                    }else currentPosition.append(errorCode);
                    longitudeText.setText(currentLongitude);
                    latitudeText.setText(currentLatitude);
                    positionText.setText(currentPosition);
                    navigateTo(location);
                }
            });

        }

        public void onConnectHotSpotMessage(String s, int i){

        }
    }
    public void setUiEnabled(boolean bool) {
        button_begin.setEnabled(!bool);
        button_stop.setEnabled(bool);
        //button_upload.setEnabled(bool);
        bt_temp.setEnabled(bool);
        bt_humi.setEnabled(bool);
        bt_pres.setEnabled(bool);
        bt_illu.setEnabled(bool);
        bt_soil.setEnabled(bool);
        bt_zwx.setEnabled(bool);
        textView1.setEnabled(bool);
        textView2.setEnabled(bool);
        textView3.setEnabled(bool);
        textView4.setEnabled(bool);
        //textView.setEnabled(bool);

    }

    public void onClickStart(View view) {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();


                if (deviceVID == 0x0403)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }


    }
    public void onClickSend1(View view) {
        textView1.setText("");
        String string = "1";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data1 = data;
                int a = data1.length();
                Log.i("data1", data1);
                tvAppend(textView1, data1);
            }
        }.start();


    }

    public void onClickSend2(View view) {
        textView2.setText("");
        String string = "2";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data2=data;
                int a = data2.length();
                Log.i("data2",data2);
                tvAppend(textView2, data2);
            }
        }.start();




        //tvAppend(textView, "\nData Sent : " + string + "\n");
        //Toast.makeText(MainActivity.this, "设备已连接",Toast.LENGTH_SHORT).show();
    }
    public void onClickSend3(View view) {
        textView3.setText("");
        String string = "3";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data3 = data;
                int a = data3.length();
                Log.i("data3", data3);
                tvAppend(textView3, data3);
            }
        }.start();


    }
    public void onClickSend4(View view) {
        textView4.setText("");
        String string = "4";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data4 = data;
                int a = data4.length();
                Log.i("data1", data4);
                tvAppend(textView4, data4);
            }
        }.start();

    }
    public void onClickSend5(View view) {
        textView5.setText("");
        String string = "5";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data5 = data;
                int a = data5.length();
                Log.i("data1", data5);
                tvAppend(textView5, data5);
            }
        }.start();

    }
    public void onClickSend6(View view) {
        textView6.setText("");
        String string = "6";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data6 = data;
                int a = data6.length();
                Log.i("data1", data6);
                tvAppend(textView6, data6 );
            }
        }.start();

    }
    public void onClickSend7(View view) {
        textView7.setText("");
        String string = "7";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data7 = data;
                int a = data7.length();
                Log.i("data1", data7);
                tvAppend(textView7, data7 );
            }
        }.start();

    }

    public void onClickStop(View view) {
        setUiEnabled(false);
        serialPort.close();
        //tvAppend(textView,"\nSerial Connection Closed! \n");
        Toast.makeText(SensorActivity.this, "设备已离线",Toast.LENGTH_SHORT).show();

    }
    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }
    public void onClickClear(View view) {
        textView1.setText("0.00");
        textView2.setText("0.00");
        textView3.setText("0.00");
        textView4.setText("0.00");
        textView5.setText("0.00");
        textView6.setText("0.00");
        textView7.setText("0.00");
    }
    /*public void onClickSave(View view){


    }*/

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();
        unregisterReceiver(broadcastReceiver);
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
}
=======
package com.example.otgsensor;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.otgsensor.Fragment.MapActivity;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static tool.ServerIP.POSTURL;
import static tool.SharedPreferenceHelper.getTableNameBySP;

public class SensorActivity extends AppCompatActivity {
    public LocationClient mLocationClient;
    public String USERNAME;
    private TextView positionText, latitudeText, longitudeText;//position
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean  isFirstLocate = true;
    private MyDatabaseHelper dbHelper;

    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
    Button button_begin,button_stop,button_upload,button_save,button_reset,bt_temp,bt_humi,bt_pres,bt_illu,bt_soil_t,bt_soil,bt_zwx;
    TextView textView1,textView2,textView3,textView4,textView5,textView6,textView7;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    String data="";
    String cdate="";

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                data= new String(arg0, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);

                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            setUiEnabled(true);
                            serialPort.setBaudRate(115200);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            //tvAppend(textView,"Serial Connection Opened!\n");
                            Toast.makeText(SensorActivity.this, "设备已上线",Toast.LENGTH_SHORT).show();

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart(button_begin);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStop(button_stop);

            }
        }

        ;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());//先初始化
        setContentView(R.layout.activity_sensor);

        mapView = findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        USERNAME = getTableNameBySP(this);//以用户名作为表名

        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        button_begin = (Button) findViewById(R.id.button_begin);
        bt_temp = (Button) findViewById(R.id.bt_temp);
        bt_humi= (Button) findViewById(R.id.bt_humi);
        bt_pres = (Button) findViewById(R.id.bt_pres);
        bt_illu = (Button) findViewById(R.id.bt_illu);
        bt_soil_t=findViewById(R.id.bt_soil_t);
        bt_soil = (Button) findViewById(R.id.bt_soil);
        bt_zwx = (Button) findViewById(R.id.bt_zwx);
        button_stop = (Button) findViewById(R.id.button_stop);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (TextView) findViewById(R.id.textView5);
        textView6 = (TextView) findViewById(R.id.textView6);
        textView7 = (TextView) findViewById(R.id.textView7);
        setUiEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

        //下面创建LocationClient的实例
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        positionText = findViewById(R.id.textView_location);
        latitudeText = findViewById(R.id.textView_lati);
        longitudeText = findViewById(R.id.textView_longi);
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(SensorActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(SensorActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(SensorActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(SensorActivity.this,permissions,1);
        }
        else {
            requestLocation();
        }
        //下面是创建数据库相关代码
        dbHelper = new MyDatabaseHelper(this, "CollectedData.db", null, 1);
        dbHelper.getWritableDatabase();
        /*dbHelper = new MyDatabaseHelper(this, "CollectedData.db", null, 1);
        Button createDatabase = findViewById(R.id.button_begin);
        createDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.getWritableDatabase();
            }
        });*/
        //下面是mapview点击响应代码
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent intent = new Intent(SensorActivity.this, MapActivity.class);
                startActivity(intent);
            }
            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        //下面是添加数据库记录代码
        Button addData = findViewById(R.id.button_save);
        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp1 = textView1.getText().toString();
                String humidity1 = textView2.getText().toString();
                String pressure1 = textView3.getText().toString();
                String illumination1 = textView4.getText().toString();
                String soilt1 = textView5.getText().toString();
                String soilh1 = textView6.getText().toString();
                String uv1 = textView7.getText().toString();
                String longitude1 = longitudeText.getText().toString();
                String latitude1 = latitudeText.getText().toString();
                float temp = Float.parseFloat(temp1);
                float humidity = Float.parseFloat(humidity1);
                float pressure = Float.parseFloat(pressure1);
                float illumination = Float.parseFloat(illumination1);
                float soil_t = Float.parseFloat(soilt1);
                float soil_h = Float.parseFloat(soilh1);
                float uv = Float.parseFloat(uv1);
                float longitude = Float.parseFloat(longitude1);
                float latitude = Float.parseFloat(latitude1);
                java.util.Date date = new java.util.Date();
                SimpleDateFormat formatter = new SimpleDateFormat ("yyyy年MM月dd日 HH:mm:ss");
                Date date1 = new Date(System.currentTimeMillis());
                //DateFormat dateFormat = DateFormat.getDateInstance();
                String sTime = formatter.format(date1);
                //String str_date = dateFormat.format(date1);
                //SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                //Date curDate = new Date(System.currentTimeMillis());
                //String date = formatter.format(curDate);

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("temp", temp);
                values.put("date", sTime);
                values.put("humidity", humidity);
                values.put("pressure", pressure);
                values.put("illumination", illumination);
                values.put("soil_t", soil_t);
                values.put("soil_h", soil_h);
                values.put("uv", uv);
                values.put("illumination", illumination);
                values.put("illumination", illumination);
                values.put("longitude", longitude);
                values.put("latitude", latitude);
                db.insert("Data", null, values);
                /*values.put("temp", "1");
                values.put("date", date.getTime());
                values.put("humidity", "2");
                values.put("pressure", "3");
                values.put("illumination", "4");
                values.put("longitude", "5");
                values.put("latitude", "6");*/
                db.insert("Data", null, values);

                Toast.makeText(SensorActivity.this, "保存成功！", Toast.LENGTH_LONG).show();
            }
        });
        //下面是上传数据代码
        Button upData = findViewById(R.id.button_upload);
        upData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp1 = textView1.getText().toString();
                String humidity1 = textView2.getText().toString();
                String pressure1 = textView3.getText().toString();
                String illumination1 = textView4.getText().toString();
                String soilt1 = textView5.getText().toString();
                String soilh1 = textView6.getText().toString();
                String uv1 = textView7.getText().toString();
                String longitude1 = longitudeText.getText().toString();
                String latitude1 = latitudeText.getText().toString();
                String posturl = POSTURL;
                /*float temp = Float.parseFloat(temp1);
                float humidity = Float.parseFloat(humidity1);
                float pressure = Float.parseFloat(pressure1);
                float illumination = Float.parseFloat(illumination1);
                float soil_t = Float.parseFloat(soilt1);
                float soil_h = Float.parseFloat(soilh1);
                float uv = Float.parseFloat(uv1);
                float longitude = Float.parseFloat(longitude1);
                float latitude = Float.parseFloat(latitude1);*/
                upLoad(posturl,temp1,humidity1,pressure1,illumination1,soilt1,soilh1,uv1,longitude1,latitude1);
            }
        });
    }
                                private void navigateTo(final BDLocation location){
                                    if (isFirstLocate){
                                        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                                        MapStatus mapStatus = new MapStatus.Builder()
                                                .target(ll)
                                                .zoom(17)
                                                .build();
                                        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                                        baiduMap.setMapStatus(mapStatusUpdate);
                                        //MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                                        //baiduMap.animateMapStatus(update);
                                        //update = MapStatusUpdateFactory.zoomTo(16f);
                                        //baiduMap.animateMapStatus(update);
                                        isFirstLocate = false;
                                    }
                                    MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
                                    locationBuilder.latitude(location.getLatitude());
                                    locationBuilder.longitude(location.getLongitude());
                                    MyLocationData locationData = locationBuilder.build();
                                    baiduMap.setMyLocationData(locationData);
    }
    @Override
    protected  void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected  void onPause(){
        super.onPause();
        mapView.onPause();
    }
    private  void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        //option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);
    }


    private void upLoad(String posturl,final String temp1, String humidity1, String pressure1, String illumination1, String soilt1, String soilh1, String uv1, String longitude1, String latitude1){

                    OkHttpClient client = new OkHttpClient();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");// HH:mm:ss
                    //获取当前时间
                    Date date = new Date(System.currentTimeMillis());
                    String cdate = simpleDateFormat.format(date);
                    FormBody.Builder formBuilder = new FormBody.Builder();
                    formBuilder.add("username", USERNAME);//USERNAME就是username
                    formBuilder.add("date", cdate);
                    formBuilder.add("temp", temp1);
                    formBuilder.add("humidity", humidity1);
                    formBuilder.add("pressure", pressure1);
                    formBuilder.add("illumination", illumination1);
                    formBuilder.add("soil_t", soilt1);
                    formBuilder.add("soil_h", soilh1);
                    formBuilder.add("uv", uv1);
                    formBuilder.add("longitude", longitude1);
                    formBuilder.add("latitude", latitude1);
                    Request request = new Request.Builder().url(posturl).post(formBuilder.build()).build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable()
                        {
                    @Override
                    public void run()
                    {
                        Toast.makeText(SensorActivity.this, "服务器错误", Toast.LENGTH_SHORT).show();
                    }
                        });
                    }
                    @Override
                        public void onResponse(Call call, Response response) throws IOException
                        {
                            final String res = response.body().string();
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (res.equals("0"))
                                    {
                                        Toast.makeText(SensorActivity.this, "请勿重复上传", Toast.LENGTH_SHORT).show();
                                    }
                                    else if (res.equals("1"))
                                    {
                                        Toast.makeText(SensorActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(SensorActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location){

            final int errorCode = location.getLocType();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
                    StringBuilder currentLatitude = new StringBuilder();
                    StringBuilder currentLongitude = new StringBuilder();
                    currentLatitude.append(location.getLatitude()).append("\n");
                    currentLongitude.append(location.getLongitude()).append("\n");
                    //currentPosition.append("国家：").append(location.getCountry()).append("\n");
            currentPosition.append("省：").append(location.getProvince()).append("\n");
            currentPosition.append("市：").append(location.getCity()).append("\n");
           currentPosition.append("区：").append(location.getDistrict()).append("\n");
            currentPosition.append("街道：").append(location.getStreet()).append("\n");
            currentPosition.append("").append(location.getLocationDescribe()).append("\n");
                    navigateTo(location);
                    currentPosition.append("定位方式:");
                    if (location.getLocType()==BDLocation.TypeGpsLocation){
                        currentPosition.append("GPS");navigateTo(location);
                    }else if (location.getLocType()==BDLocation.TypeNetWorkLocation){
                        currentPosition.append("网络");navigateTo(location);
                    }else currentPosition.append(errorCode);
                    longitudeText.setText(currentLongitude);
                    latitudeText.setText(currentLatitude);
                    positionText.setText(currentPosition);
                    navigateTo(location);
                }
            });

        }

        public void onConnectHotSpotMessage(String s, int i){

        }
    }
    public void setUiEnabled(boolean bool) {
        button_begin.setEnabled(!bool);
        button_stop.setEnabled(bool);
        //button_upload.setEnabled(bool);
        bt_temp.setEnabled(bool);
        bt_humi.setEnabled(bool);
        bt_pres.setEnabled(bool);
        bt_illu.setEnabled(bool);
        bt_soil.setEnabled(bool);
        bt_zwx.setEnabled(bool);
        textView1.setEnabled(bool);
        textView2.setEnabled(bool);
        textView3.setEnabled(bool);
        textView4.setEnabled(bool);
        //textView.setEnabled(bool);

    }

    public void onClickStart(View view) {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();


                if (deviceVID == 0x0403)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }


    }
    public void onClickSend1(View view) {
        textView1.setText("");
        String string = "1";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data1 = data;
                int a = data1.length();
                Log.i("data1", data1);
                tvAppend(textView1, data1);
            }
        }.start();


    }

    public void onClickSend2(View view) {
        textView2.setText("");
        String string = "2";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data2=data;
                int a = data2.length();
                Log.i("data2",data2);
                tvAppend(textView2, data2);
            }
        }.start();




        //tvAppend(textView, "\nData Sent : " + string + "\n");
        //Toast.makeText(MainActivity.this, "设备已连接",Toast.LENGTH_SHORT).show();
    }
    public void onClickSend3(View view) {
        textView3.setText("");
        String string = "3";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data3 = data;
                int a = data3.length();
                Log.i("data3", data3);
                tvAppend(textView3, data3);
            }
        }.start();


    }
    public void onClickSend4(View view) {
        textView4.setText("");
        String string = "4";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data4 = data;
                int a = data4.length();
                Log.i("data1", data4);
                tvAppend(textView4, data4);
            }
        }.start();

    }
    public void onClickSend5(View view) {
        textView5.setText("");
        String string = "5";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data5 = data;
                int a = data5.length();
                Log.i("data1", data5);
                tvAppend(textView5, data5);
            }
        }.start();

    }
    public void onClickSend6(View view) {
        textView6.setText("");
        String string = "6";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data6 = data;
                int a = data6.length();
                Log.i("data1", data6);
                tvAppend(textView6, data6 );
            }
        }.start();

    }
    public void onClickSend7(View view) {
        textView7.setText("");
        String string = "7";
        serialPort.write(string.getBytes());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String data7 = data;
                int a = data7.length();
                Log.i("data1", data7);
                tvAppend(textView7, data7 );
            }
        }.start();

    }

    public void onClickStop(View view) {
        setUiEnabled(false);
        serialPort.close();
        //tvAppend(textView,"\nSerial Connection Closed! \n");
        Toast.makeText(SensorActivity.this, "设备已离线",Toast.LENGTH_SHORT).show();

    }
    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }
    public void onClickClear(View view) {
        textView1.setText("0.00");
        textView2.setText("0.00");
        textView3.setText("0.00");
        textView4.setText("0.00");
        textView5.setText("0.00");
        textView6.setText("0.00");
        textView7.setText("0.00");
    }
    /*public void onClickSave(View view){


    }*/

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();
        unregisterReceiver(broadcastReceiver);
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
}
>>>>>>> 00e44cd70197d03f85f3ec0d0dc8dfa927649a5f
