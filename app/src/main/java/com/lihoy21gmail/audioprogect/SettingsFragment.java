package com.lihoy21gmail.audioprogect;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsFragment extends Fragment {
    private final String TAG = "myLogs";
    private SharedPreferences sPref;
    private boolean SoundState;
    private boolean MusicState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.settings, null);
        LoadPref();
        Switch swtchSound = (Switch) v.findViewById(R.id.swtchSound);
        swtchSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SoundState = isChecked;
            }
        });
        Switch swtchMusic = (Switch) v.findViewById(R.id.swtchMusic);
        swtchMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MusicState = true;
                    ((MainActivity) getActivity()).StartMusic();
                } else {
                    MusicState = false;
                    ((MainActivity) getActivity()).StopMusic();
                }
            }
        });
        swtchMusic.setChecked(MusicState);
        swtchSound.setChecked(SoundState);
        return v;
    }

    public void LoadPref() {
        Log.d(TAG, "LoadPref: setting");
        sPref = getActivity().getSharedPreferences("settings", 0);
        SharedPreferences.Editor ed = sPref.edit();
        SoundState = sPref.getBoolean("SoundState", false);
        MusicState = sPref.getBoolean("MusicState", false);

        ed.apply();
    }

    public void SavePref() {
        Log.d(TAG, "SavePref: setting");
        sPref = getActivity().getSharedPreferences("settings", 0);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean("SoundState", SoundState);
        ed.putBoolean("MusicState", MusicState);
        ed.apply();
    }

    @Override
    public void onStop() {
        SavePref();
        super.onStop();
    }
}
