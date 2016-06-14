package com.lihoy21gmail.audioprogect;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainMenuFragment extends Fragment {
    private SharedPreferences sPref;
    private final String TAG = "myLogs";
    private int LastLevel;
    private boolean GameState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.main_menu, null);
        LoadPref();
        Button btnProceed = (Button) v.findViewById(R.id.proceed);
        Button btnAbout = (Button) v.findViewById(R.id.about);
        Button btnLevelChoice = (Button) v.findViewById(R.id.lvl_choice);
        Button btnSettings = (Button) v.findViewById(R.id.settings);
        Button btnExit = (Button) v.findViewById(R.id.exit);
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameWorldFragment gameWorldFragment = GameWorldFragment.newInstance(LastLevel);
                getFragmentManager().
                        beginTransaction().
                        replace(android.R.id.content, gameWorldFragment).
                        commit();
            }
        });
        btnLevelChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameState = true;
                ChooseLevelsFragment chooseLevelsFragment = new ChooseLevelsFragment();
                getFragmentManager().
                        beginTransaction().
                        replace(android.R.id.content, chooseLevelsFragment).
                        //addToBackStack(null).
                                commit();
            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsFragment settingsFragment = new SettingsFragment();
                getFragmentManager().
                        beginTransaction().
                        replace(android.R.id.content, settingsFragment).
                        addToBackStack(null).
                        commit();
            }
        });
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutFragment aboutFragment = new AboutFragment();
                getFragmentManager().
                        beginTransaction().
                        replace(android.R.id.content, aboutFragment).
                        addToBackStack(null).
                        commit();
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        return v;
    }

    @Override
    public void onStop() {
        SavePref();
        super.onStop();
    }

    public void LoadPref() {
        Log.d(TAG, "LoadPref: MainMenu");
        sPref = getActivity().getSharedPreferences("settings", 0);
        SharedPreferences.Editor ed = sPref.edit();
        LastLevel = sPref.getInt("LastLevel", 0);
        GameState = sPref.getBoolean("IsDone", true);
        Log.d(TAG, "LoadPref: gameState" + GameState);
        ed.apply();
    }
    public void SavePref() {
        Log.d(TAG, "SavePref: MainMenu");
        sPref = getActivity().getSharedPreferences("settings", 0);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean("IsDone", GameState);
        ed.apply();
    }
}
