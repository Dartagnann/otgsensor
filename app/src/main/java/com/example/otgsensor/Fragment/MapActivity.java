package com.example.otgsensor.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import com.example.otgsensor.R;


import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private MapView fmapView;
    private BaiduMap fbaiduMap;
    public LocationClient mLocationClient;
    private boolean  isFirstLocate = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        fmapView = findViewById(R.id.fmapView);
        fbaiduMap = fmapView.getMap();
        fbaiduMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MapActivity.this,permissions,1);
        }
        else {
            requestLocation();
        }
    }
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location){
            if (location.getLocType() == BDLocation.TypeGpsLocation
                    || location.getLocType() == BDLocation.TypeNetWorkLocation){
                navigateTo(location);
            }

        }

        public void onConnectHotSpotMessage(String s, int i){

        }
    }

    private void navigateTo(final BDLocation location){
        if (isFirstLocate){
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatus mapStatus = new MapStatus.Builder()
                    .target(ll)
                    .zoom(17.5f)
                    .build();
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
            fbaiduMap.animateMapStatus(mapStatusUpdate);
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
        fbaiduMap.setMyLocationData(locationData);
    }
    @Override
    protected  void onResume(){
        super.onResume();
        fmapView.onResume();
    }
    @Override
    protected  void onPause(){
        super.onPause();
        fmapView.onPause();
    }
    @Override
    protected  void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();
        fmapView.onDestroy();
        fbaiduMap.setMyLocationEnabled(false);
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.ACTION_DOWN){
            MapActivity.this.finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
