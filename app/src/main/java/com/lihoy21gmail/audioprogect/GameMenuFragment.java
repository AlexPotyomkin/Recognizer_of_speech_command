package com.lihoy21gmail.audioprogect;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class GameMenuFragment extends Fragment {
    private final String TAG = "myLogs";
    private boolean GameState;
    SharedPreferences sPref;

    public static GameMenuFragment newInstance(int lvl, int CountLevel) {
        GameMenuFragment f = new GameMenuFragment();
        Bundle args = new Bundle();
        args.putInt("lvl", lvl);
        args.putInt("CountLevel", CountLevel);
        f.setArguments(args);
        return f;
    }

    public int getLevelNumber() {
        return getArguments().getInt("lvl", 0);
    }

    public int getCountLevel() {
        return getArguments().getInt("CountLevel", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.game_menu, null);
        LoadPref();
        Button btnProceed = (Button) v.findViewById(R.id.proceed);
        Button btnNext = (Button) v.findViewById(R.id.next);
        Button btnLevelChoice = (Button) v.findViewById(R.id.lvl_choice);
        Button btnSettings = (Button) v.findViewById(R.id.settings);
        Button btnToMainMenu = (Button) v.findViewById(R.id.main_menu);
        Button btnExit = (Button) v.findViewById(R.id.exit);
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameWorldFragment gameWorldFragment = GameWorldFragment.
                            newInstance(getLevelNumber());
                getFragmentManager().
                        beginTransaction().
                        replace(android.R.id.content, gameWorldFragment).
                        commit();

            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCountLevel() > getLevelNumber() + 1) {
                    GameState = true;
                    GameWorldFragment gameWorldFragment = GameWorldFragment.
                            newInstance(getLevelNumber() + 1);
                    getFragmentManager().
                            beginTransaction().
                            replace(android.R.id.content, gameWorldFragment).
                            commit();
                } else
                    Toast.makeText(getActivity(), "Это последний уровень, новые будут скоро",
                            Toast.LENGTH_SHORT).show();
            }
        });
        btnLevelChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameState = true;
                ChooseLevelsFragment chooseLevelsFragment = new ChooseLevelsFragment();
                getFragmentManager().
                        beginTransaction().
                        //remove(getFragmentManager().findFragmentByTag("menu")).
                                replace(android.R.id.content, chooseLevelsFragment).
                        commit();
            }
        });
        btnToMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenuFragment mainMenuFragment = new MainMenuFragment();
                getFragmentManager().
                        beginTransaction().
                        replace(android.R.id.content, mainMenuFragment).
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
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        Log.d(TAG, "onCreateView: GameState " + GameState);

        return v;

    }

    @Override
    public void onStop() {
        SavePref();
        super.onStop();
    }

    public void SavePref() {
        Log.d(TAG, "SavePref: game menu");
        sPref = getActivity().getSharedPreferences("settings", 0);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean("IsDone", GameState);
        Log.d(TAG, "SavePref: game state " + GameState);
        ed.apply();
    }

    public void LoadPref() {
        Log.d(TAG, "LoadPref: GameWorld");
        sPref = getActivity().getSharedPreferences("settings", 0);
        SharedPreferences.Editor ed = sPref.edit();
        GameState = sPref.getBoolean("IsDone", true);
        ed.apply();
    }
}
