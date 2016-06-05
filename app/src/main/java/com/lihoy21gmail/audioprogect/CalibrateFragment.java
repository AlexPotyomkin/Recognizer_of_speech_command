package com.lihoy21gmail.audioprogect;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

public class CalibrateFragment extends Fragment {
    private final String TAG = "myLogs";
    private int checking_radio = 0;
    private RadioGroup radioGroup1;
    private RadioGroup radioGroup2;

    RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                radioGroup2.setOnCheckedChangeListener(null); // remove the listener before clearing so we don't throw that stackoverflow exception(like Vladimir Volodin pointed out)
                radioGroup2.clearCheck(); // clear the second RadioGroup!
                radioGroup2.setOnCheckedChangeListener(listener2); //reset the listener
            }
            if(checkedId==R.id.radioRight){
                checking_radio = 0;
            }
            if(checkedId==R.id.radioLeft){
                checking_radio = 1;
            }
        }
    };
    RadioGroup.OnCheckedChangeListener  listener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                radioGroup1.setOnCheckedChangeListener(null); // remove the listener before clearing so we don't throw that stackoverflow exception(like Vladimir Volodin pointed out)
                radioGroup1.clearCheck(); // clear the second RadioGroup!
                radioGroup1.setOnCheckedChangeListener(listener1); //reset the listener
            }
            if(checkedId==R.id.radioUp){
                checking_radio = 2;
            }
            if(checkedId==R.id.radioDown){
                checking_radio = 3;
            }
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.calibrate_fragment, null);
        radioGroup1 = (RadioGroup) v.findViewById(R.id.radioGroup1);
        radioGroup2 = (RadioGroup) v.findViewById(R.id.radioGroup2);

        //radioGroup1.clearCheck(); // this is so we can start fresh, with no selection on both RadioGroups
        //radioGroup2.clearCheck();
        radioGroup1.setOnCheckedChangeListener(listener1); // this is so we can start fresh, with no selection on both RadioGroups
        radioGroup2.setOnCheckedChangeListener(listener2);

        Button button = (Button) v.findViewById(R.id.bCalibrate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity)getActivity()).on_Calibrate(checking_radio);
                Log.d(TAG, "Click: " + checking_radio);
            }
        });
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        ((MainActivity) getActivity()).startSpeechRecognition();
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        ((MainActivity)getActivity()).stopSpeechRecognition();
        super.onDetach();
    }
}
