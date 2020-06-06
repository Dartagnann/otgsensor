package com.example.otgsensor.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.otgsensor.DataActivity;
import com.example.otgsensor.R;

/**
 * 这是查看数据记录的fragment
 */



public class SecondFragment extends ListFragment {
    private ImageView imageView;
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
                    new String[]{"id","date","tem","humidity","pressure","illumination","soil_t","soil_h","uv","longitude","latitude"},
                    new int[]{R.id.did,R.id.opttime,R.id.temp,R.id.humi,R.id.pres,R.id.illu,R.id.soil_t,R.id.soil_h,R.id.uv,R.id.longi,R.id.lati} );
            setListAdapter(adapter);
        }
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        try {
            HashMap<String, Object> view = (HashMap<String, Object>) l.getItemAtPosition(position);
            //Toast.makeText(getActivity(), view.get("id").toString(), Toast.LENGTH_LONG).show();
            //传递当前点击item中的id字段
            Intent intent = new Intent(getActivity(), DataActivity.class);
            String data = view.get("id").toString();
            intent.putExtra("extra_data",data);
            startActivity(intent);
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
        Cursor c = db.rawQuery("SELECT * FROM Data ORDER BY id DESC LIMIT 1000 ",null);
        while (c.moveToNext()) {
            //String temp = c.getString(c.getColumnIndex("temp"));
            //String humidity = c.getString(c.getColumnIndex("humidity"));
            String id = c.getString(c.getColumnIndex("id"));
            String date = c.getString(c.getColumnIndex("date"));
            String temp = c.getString(c.getColumnIndex("tem"));
            String humidity = c.getString(c.getColumnIndex("humidity"));
            String pressure = c.getString(c.getColumnIndex("pressure"));
            String illumination = c.getString(c.getColumnIndex("illumination"));
            String soil_t = c.getString(c.getColumnIndex("soil_t"));
            String soil_h = c.getString(c.getColumnIndex("soil_h"));
            String uv = c.getString(c.getColumnIndex("uv"));
            String longitude = c.getString(c.getColumnIndex("longitude"));
            String latitude = c.getString(c.getColumnIndex("latitude"));
            //byte[] img = c.getBlob(c.getColumnIndex("img"));
            //ByteArrayInputStream in = new ByteArrayInputStream(img);
            //

            //imageView.setImageDrawable(Drawable.createFromStream(in,"src"));
            //String date = c.getString(c.getColumnIndex("date"));



            //String spic = c.getString(c.getColumnIndex("PIC"));

            Map<String, Object> map = new HashMap<>();
            // map.put("img", R.drawable.logo2);
            // map.put("temp", temp);
            //map.put("humidity", humidity);
            map.put("id",id);
            map.put("date", date);
            map.put("tem", "温度: "+temp+"℃");
            map.put("humidity", "湿度: "+humidity+"%");
            map.put("pressure", "气压: "+pressure+"hPa");
            map.put("illumination", "光照: "+illumination+"lux");
            map.put("soil_t", "土壤温度: "+soil_t+"℃");
            map.put("soil_h", "土壤湿度: "+soil_h+"%");
            map.put("uv", "紫外线等级: "+uv+"mW/cm2");
            map.put("longitude", "经度: "+longitude);
            map.put("latitude", "纬度: "+latitude);
            //map.put("img",img);
            //map.put("temp", temp);
            //map.put("temp", temp);



            list.add(map);
        }
        c.close();
        db.close();

        return list;
    }
}
