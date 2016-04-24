package com.lihoy21gmail.audioprogect;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

public class ControlPanel extends Fragment {
    private int cheking_radio=0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.control_panel, null);
        RadioGroup radioGroup = (RadioGroup) v.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioRight: {cheking_radio = 0; break;}
                    case R.id.radioLeft:{cheking_radio = 1; break;}
                    case R.id.radioUp:{cheking_radio = 2; break;}
                    case R.id.radioDown:{cheking_radio = 3; break;}
                }
            }
        });
        Button button = (Button) v.findViewById(R.id.bCalibrate);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ((MainActivity)getActivity()).on_Calibrate(cheking_radio);
            }
        });
        return v;
    }
}
