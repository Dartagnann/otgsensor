package com.example.otgsensor.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.otgsensor.R;



public class SecondFragment extends ListFragment {
    public SecondFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v;
        v = inflater.inflate(R.layout.fragment_second, container, false);
        return v;
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            SimpleAdapter adapter = new SimpleAdapter(getActivity(), getData(),
                    R.layout.listviewitem,
                    //new String[]{"img","temp","humidity","date"},
                    //new int[]{R.id.img,R.id.title,R.id.info,R.id.opttime}
                    new String[]{"date","tem","humi","co2","illumination","soil_t","soil_h","ph","longitude","latitude"},
                    new int[]{R.id.opttime,R.id.temp,R.id.humi,R.id.co2,R.id.illu,R.id.soil_t,R.id.soil_h,R.id.ph,R.id.longi,R.id.lati} );
            setListAdapter(adapter);
        }
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        try {
            HashMap<String, Object> view = (HashMap<String, Object>) l.getItemAtPosition(position);
            Toast.makeText(getActivity(), view.get("id").toString(), Toast.LENGTH_LONG).show();
        }
        catch( Exception e) {
            Log.e("HashMap",e.toString() );
        }
    }
    private List<Map<String, Object>> getData() {
        List<Map<String ,Object>> list = new ArrayList<>();
        // 从数据库中取
        SQLiteDatabase db;
        db = getActivity().openOrCreateDatabase("CollectedData.db", Context.MODE_PRIVATE, null);
         Cursor c = db.rawQuery("SELECT * FROM Data ORDER BY id DESC LIMIT 10 ",null);
        while (c.moveToNext()) {
            //String temp = c.getString(c.getColumnIndex("temp"));
            //String humidity = c.getString(c.getColumnIndex("humidity"));
            String date = c.getString(c.getColumnIndex("date"));
            String temp = c.getString(c.getColumnIndex("tem"));
            String humidity = c.getString(c.getColumnIndex("humi"));
            String pressure = c.getString(c.getColumnIndex("co2"));
            String illumination = c.getString(c.getColumnIndex("illumination"));
            String soil_t = c.getString(c.getColumnIndex("soil_t"));
            String soil_h = c.getString(c.getColumnIndex("soil_h"));
            String uv = c.getString(c.getColumnIndex("ph"));
            String longitude = c.getString(c.getColumnIndex("longitude"));
            String latitude = c.getString(c.getColumnIndex("latitude"));
            //String date = c.getString(c.getColumnIndex("date"));



            //String spic = c.getString(c.getColumnIndex("PIC"));

            Map<String, Object> map = new HashMap<>();
           // map.put("img", R.drawable.logo2);
           // map.put("temp", temp);
            //map.put("humidity", humidity);
            map.put("date", date);
            map.put("tem", "温度: "+temp+"℃");
            map.put("humi", "湿度: "+humidity+"%");
            map.put("co2", "二氧化碳: "+pressure+"ppm");
            map.put("illumination", "光照强度: "+illumination+"lux");
            map.put("soil_t", "土壤温度: "+soil_t+"℃");
            map.put("soil_h", "土壤湿度: "+soil_h+"%");
            map.put("ph", "土壤pH值: "+uv+" ");
            map.put("longitude", "经度: "+longitude);
            map.put("latitude", "纬度: "+latitude);
            //map.put("temp", temp);
            //map.put("temp", temp);



            list.add(map);
        }
        c.close();
        db.close();

        return list;
    }
}
