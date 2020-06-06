package com.example.otgsensor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class DataActivity extends AppCompatActivity {
    private TextView textView1, textDate, textTemp,textHumi,textPres,textLight,textSoilt,textSoilh,textZwx,textLongi,textLati;
    private ImageView imageBig;
    private Button button_del;
    private MyDatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        textView1 = findViewById(R.id.extra);
        textDate = findViewById(R.id.text_date);
        textTemp = findViewById(R.id.text_temp);
        textHumi = findViewById(R.id.text_humi);
        textPres = findViewById(R.id.text_pres);
        textLight = findViewById(R.id.text_light);
        textSoilt = findViewById(R.id.text_soil_t);
        textSoilh = findViewById(R.id.text_soil_h);
        textZwx = findViewById(R.id.text_zwx);
        textLongi = findViewById(R.id.text_longi);
        textLati = findViewById(R.id.text_lati);

        imageBig = findViewById(R.id.image_big);
        button_del = findViewById(R.id.button_del);
        dbHelper = new MyDatabaseHelper(this,"CollectedData.db",null,1);



        Intent intent = getIntent();
        String data = intent.getStringExtra("extra_data");
        tvAppend(textView1,data);
        final int did = Integer.parseInt(data);
        SQLiteDatabase db;
        db = this.openOrCreateDatabase("CollectedData.db", Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("SELECT * FROM Data WHERE id = "+ did,null);
        while (c.moveToNext()){
            String date = c.getString(c.getColumnIndex("date"));
            String temp = c.getString(c.getColumnIndex("tem"));
            String humi = c.getString(c.getColumnIndex("humidity"));
            String pres = c.getString(c.getColumnIndex("pressure"));
            String illu = c.getString(c.getColumnIndex("illumination"));
            String soil_t = c.getString(c.getColumnIndex("soil_t"));
            String soil_h = c.getString(c.getColumnIndex("soil_h"));
            String uv = c.getString(c.getColumnIndex("uv"));
            String longitude = c.getString(c.getColumnIndex("longitude"));
            String latitude = c.getString(c.getColumnIndex("latitude"));
            byte[] img = c.getBlob(c.getColumnIndex("img"));
            tvAppend(textDate, date);
            tvAppend(textTemp,temp+"℃");
            tvAppend(textHumi,humi+"%");
            tvAppend(textPres,pres+"hpa");
            tvAppend(textLight,illu+"lux");
            tvAppend(textSoilt,soil_t+"℃");
            tvAppend(textSoilh,soil_h+"%");
            tvAppend(textZwx,uv+"mW/cm2");
            tvAppend(textLongi,longitude);
            tvAppend(textLati,latitude);
            if(img !=null){
                ByteArrayInputStream in = new ByteArrayInputStream(img);
                imageBig.setImageDrawable(Drawable.createFromStream(in,"src"));
            }
        }
        c.close();
        db.close();

        button_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("DELETE FROM Data WHERE id =" + did);
                Intent intent = new Intent(DataActivity.this, MainActivity.class);
                startActivity(intent);
                Toast.makeText(DataActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable()//在后台线程中关闭此活动
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Thread.sleep(1000);
                            DataActivity.this.finish();
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });






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
}
