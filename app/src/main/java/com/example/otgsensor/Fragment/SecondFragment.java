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
                    new String[]{"date","img"},new int[]{R.id.opttime,R.id.img} );
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
        Cursor c = db.rawQuery("SELECT date FROM Data ORDER BY id DESC LIMIT 10 ",null);
        while (c.moveToNext()) {
            //String temp = c.getString(c.getColumnIndex("temp"));
            //String humidity = c.getString(c.getColumnIndex("humidity"));
            String date = c.getString(c.getColumnIndex("date"));
            //String spic = c.getString(c.getColumnIndex("PIC"));

            Map<String, Object> map = new HashMap<>();
            map.put("img", R.drawable.b111);
           // map.put("temp", temp);
            //map.put("humidity", humidity);
            map.put("date", date);

            list.add(map);
        }
        c.close();
        db.close();

        return list;
    }
}
