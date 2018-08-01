package com.example.otgsensor.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.otgsensor.LogActivity;
import com.example.otgsensor.MainActivity;
import com.example.otgsensor.R;
import com.example.otgsensor.SensorActivity;

import static tool.SharedPreferenceHelper.setLoggingStatus;



public class ThirdFragment extends Fragment {
    private TextView textView;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_third, container, false);
        textView = view.findViewById(R.id.returnLog);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //saveRealBillNameToSharedPreferences(AverageBillActivity.this, "");
                setLoggingStatus(getActivity(), false);
                Intent intent = new Intent(getActivity(), LogActivity.class);
                startActivity(intent);
               getActivity().finish();

            }
        });
        return view;//这里返回的是上面加载的view
    }
}
