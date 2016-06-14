package com.lihoy21gmail.audioprogect;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;


public class GameWorldFragment extends Fragment implements View.OnTouchListener,
        SoundPool.OnLoadCompleteListener, Observer {
    private final String TAG = "myLogs";
    enum Cell {User, Wall, None, Box, Point, UserOnPoint, BoxDone}
    private int ColumnCount;
    private int RowCount;
    private Point BeginMotionPoint = new Point(0, 0);
    private Point UserPoint = new Point(1, 1);
    private Point LastUserPoint = new Point(1, 1);
    private Point LastBoxPoint = new Point(1, 1);
    private Point LastPreUserPoint = new Point(1, 1);
    private Cell[][] CurrMap;
    private Cell[][] TopMap;
    private int TotalBoxCount = 0;
    private int PlacedBoxCount = 0;
    private BDLevels lvl;
    private Button btnCancel;
    private Button btnMenu;
    private Model mModel;
    private SharedPreferences sPref;
    private SoundPool sp;
    private boolean SoundState;
    private boolean MusicStateLoad;
    private boolean SoundStateLoad;
    private boolean GameState;
    private boolean SpeechCommandState = false;
    private int soundIdStep;
    private int soundIdPark;
    private int soundIdNoWay;
    private int soundIdFlourish;


    public static GameWorldFragment newInstance(int lvl) {
        GameWorldFragment f = new GameWorldFragment();
        Bundle args = new Bundle();
        args.putInt("lvl", lvl);
        f.setArguments(args);
        return f;

    }

    public int getCurrLevel() {
        return getArguments().getInt("lvl", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        sp.setOnLoadCompleteListener(this);
        mModel = Model.getInstance();
        soundIdPark = sp.load(getActivity(), R.raw.park, 1);
        soundIdStep = sp.load(getActivity(), R.raw.step, 1);
        soundIdNoWay = sp.load(getActivity(), R.raw.noway, 1);
        soundIdFlourish = sp.load(getActivity(), R.raw.funfar, 1);

        final View v = inflater.inflate(R.layout.game_world, null);
        final TableLayout tableLayout = (TableLayout) v.findViewById(R.id.tabLayout);
        TextView tv = (TextView) v.findViewById(R.id.lvl);
        tv.setText(getResources().getString(R.string.lvl, getCurrLevel()));
        tableLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tableLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                init(v);

            }
        });
        Button btnAgain = (Button) v.findViewById(R.id.again);
        btnAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameState = true;
                GameWorldFragment gameWorldFragment = GameWorldFragment.newInstance(getCurrLevel());
                getFragmentManager().
                        beginTransaction().
                        replace(android.R.id.content, gameWorldFragment).
                        commit();
            }
        });
        btnMenu = (Button) v.findViewById(R.id.toMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameMenuFragment gameMenuFragment = GameMenuFragment.newInstance(getCurrLevel(),
                        lvl.getTotalCountLvls());
                getFragmentManager().
                        beginTransaction().
                        replace(android.R.id.content, gameMenuFragment, "menu").
                        //addToBackStack(null).
                                commit();
            }
        });
        btnCancel = (Button) v.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
             btnCancel.setEnabled(false);
             if (LastBoxPoint.x == -1) {
                 TopMap[UserPoint.y][UserPoint.x] = Cell.None;
                 ShowMapTop(UserPoint);
                 TopMap[LastPreUserPoint.y][LastPreUserPoint.x] = Cell.User;
                 ShowMapTop(LastPreUserPoint);
                 UserPoint = LastPreUserPoint;

             } else {
                 TopMap[LastBoxPoint.y][LastBoxPoint.x] = Cell.None;
                 ShowMapTop(LastBoxPoint);
                 TopMap[LastPreUserPoint.y][LastPreUserPoint.x] = Cell.User;
                 ShowMapTop(LastPreUserPoint);
                 TopMap[LastUserPoint.y][LastUserPoint.x] = Cell.Box;
                 ShowMapTop(LastUserPoint);
                 UserPoint = LastPreUserPoint;
                 if (CurrMap[LastBoxPoint.y][LastBoxPoint.x] == Cell.Point) PlacedBoxCount--;
                 if (CurrMap[LastUserPoint.y][LastUserPoint.x] == Cell.Point) PlacedBoxCount++;
             }
         }
     }
        );
        btnCancel.setEnabled(false);
        Switch swtchSpeechCommand = (Switch) v.findViewById(R.id.speech_command);
        swtchSpeechCommand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (MusicStateLoad)
                        ((MainActivity) getActivity()).StopMusic();
                    SoundState = false;
                    SpeechCommandState = true;
                    ((MainActivity) getActivity()).startSpeechRecognition();
                } else {
                    Log.d(TAG, "onCheckedChanged: switch speech command rec disable");
                    if (MusicStateLoad)
                        ((MainActivity) getActivity()).StartMusic();
                    if (SoundStateLoad)
                        SoundState = true;
                    SpeechCommandState = false;
                    ((MainActivity) getActivity()).stopSpeechRecognition();
                }
            }
        });
        mModel.addObserver(this);
        v.setOnTouchListener(this);
        return v;
    }

    public void init(View v) {
        Log.d(TAG, "init: ");
        lvl = new BDLevels(getCurrLevel());
        char temp[] = lvl.getLevels().toCharArray();
        ColumnCount = lvl.getWidth();
        RowCount = lvl.getHeight();
        CurrMap = new Cell[RowCount][ColumnCount];
        TopMap = new Cell[RowCount][ColumnCount];
        LoadPref();
        Switch swtchSpeechCommand = (Switch) v.findViewById(R.id.speech_command);
        swtchSpeechCommand.setChecked(SpeechCommandState);
        if (GameState) {
            for (int i = 0; i < RowCount; i++)
                for (int j = 0; j < ColumnCount; j++)
                    CurrMap[i][j] = CharToCell(temp[i * ColumnCount + j]);

            for (int i = 0; i < RowCount; i++)
                for (int j = 0; j < ColumnCount; j++) {
                    switch (CurrMap[i][j]) {
                        case User:
                            UserPoint.set(j, i);
                            CurrMap[i][j] = Cell.None;
                            TopMap[i][j] = Cell.User;
                            break;
                        case None:
                        case Wall:
                        case Point:
                            TopMap[i][j] = Cell.None;
                            break;
                        case Box:
                            TotalBoxCount++;
                            TopMap[i][j] = Cell.Box;
                            CurrMap[i][j] = Cell.None;
                            break;
                        case BoxDone:
                            PlacedBoxCount++;
                            TotalBoxCount++;
                            TopMap[i][j] = Cell.Box;
                            CurrMap[i][j] = Cell.Point;
                            break;
                    }
                }
        }
        TableLayout tableLayout = (TableLayout) v.findViewById(R.id.tabLayout);
        for (int i = 0; i < RowCount; i++) {
            TableRow tableRow = new TableRow(getActivity());
            TableRow.LayoutParams lp1 = new TableRow.LayoutParams();
            int height = tableLayout.getHeight() / RowCount;
            int width = tableLayout.getWidth() / ColumnCount;
            TableLayout.LayoutParams lp = new TableLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            if (height > width) height = width;
            if (width > height) width = height;
            lp1.height = height;
            lp1.weight = width;
            lp1.setMargins(1, 1, 1, 1);
            lp.setMargins((tableLayout.getWidth() - (width * ColumnCount + ColumnCount)) / 2,
                    (tableLayout.getHeight() - (width * RowCount + RowCount)) / 2,
                    (tableLayout.getWidth() - (width * ColumnCount + ColumnCount)) / 2,
                    (tableLayout.getHeight() - (width * RowCount + RowCount)) / 2);
            tableLayout.setLayoutParams(lp);
            for (int j = 0; j < ColumnCount; j++) {
                ImageView imageView = new ImageView(getActivity());
                imageView.setLayoutParams(lp1);
                tableRow.addView(imageView, j);
            }
            tableLayout.addView(tableRow, i);
        }
        SoundState = false;
        for (int i = 0; i < RowCount; i++)
            for (int j = 0; j < ColumnCount; j++)
                ShowMapTop(new Point(j, i));
        SoundState = SoundStateLoad;
    }

    public int get_texture(Cell cell) {
        switch (cell) {
            case Wall:
                return R.drawable.wall;
            case User:
                return R.drawable.user;
            case None:
                return R.drawable.white;
            case Point:
                return R.drawable.point;
            case Box:
                return R.drawable.box;
            case BoxDone:
                return R.drawable.boxdone;
            case UserOnPoint:
                return R.drawable.useronpoint;
            default:
                return R.drawable.white;
        }
    }

    public Cell CharToCell(char c) {
        switch (c) {
            case '#':
                return Cell.Wall;
            case 'u':
                return Cell.User;
            case ' ':
                return Cell.None;
            case 'p':
                return Cell.Point;
            case 'b':
                return Cell.Box;
            case 'd':
                return Cell.BoxDone;
            default:
                return Cell.None;
        }
    }

    public char CellToChar(Cell cell) {
        switch (cell) {
            case Wall:
                return '#';
            case User:
                return 'u';
            case None:
                return ' ';
            case Point:
                return 'p';
            case Box:
                return 'b';
            case BoxDone:
                return 'd';
            default:
                return ' ';
        }
    }

    public void step(int dx, int dy) {
        if(GameState)
            GameState=false;
        Point place;
        Point after;
        place = new Point(UserPoint.x + dx, UserPoint.y + dy);

        if (!InRange(place)) {
            if (SoundState) sp.play(soundIdNoWay, 1, 1, 1, 1, 1);
            return;
        }
        LastBoxPoint = new Point(-1, -1);

        if (TopMap[place.y][place.x] == Cell.None) {
            btnCancel.setEnabled(true);
            LastPreUserPoint = UserPoint;
            LastUserPoint = place;

            TopMap[UserPoint.y][UserPoint.x] = Cell.None;
            ShowMapTop(UserPoint);
            TopMap[place.y][place.x] = Cell.User;
            ShowMapTop(place);
            UserPoint = place;

        }
        if (TopMap[place.y][place.x] == Cell.Box) {
            after = new Point(place.x + dx, place.y + dy);
            if (!InRange(after)) {
                if (SoundState) sp.play(soundIdNoWay, 1, 1, 1, 1, 1);
                return;
            }
            btnCancel.setEnabled(true);
            LastPreUserPoint = UserPoint;
            LastUserPoint = place;
            LastBoxPoint = after;
            if (TopMap[after.y][after.x] != Cell.None) {
                if (SoundState) sp.play(soundIdNoWay, 1, 1, 1, 1, 1);
                return;
            }

            if (CurrMap[place.y][place.x] == Cell.Point) PlacedBoxCount--;
            if (CurrMap[after.y][after.x] == Cell.Point) PlacedBoxCount++;

            TopMap[UserPoint.y][UserPoint.x] = Cell.None;
            ShowMapTop(UserPoint);
            TopMap[place.y][place.x] = Cell.User;
            ShowMapTop(place);
            TopMap[after.y][after.x] = Cell.Box;
            ShowMapTop(after);
            UserPoint = place;
        }

        if (IsDone()) {
            Log.d(TAG, "DONE");
            GameState = true;
            if (SoundState) sp.play(soundIdFlourish, 1, 1, 1, 1, 1);
            if (lvl.getTotalCountLvls() > getCurrLevel() + 1) {
                GameState = true;
                GameWorldFragment gameWorldFragment = GameWorldFragment.
                        newInstance(getCurrLevel() + 1);
                getFragmentManager().
                        beginTransaction().
                        replace(android.R.id.content, gameWorldFragment).
                        commit();
            } else {
                MainMenuFragment mainMenufragment = new MainMenuFragment();
                getFragmentManager().
                        beginTransaction().
                        replace(android.R.id.content, mainMenufragment).
                        commit();
                Toast.makeText(getActivity(), "Это последний уровень, новые будут скоро",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // нажатие
                BeginMotionPoint.set(Math.round(event.getX()), Math.round(event.getY()));
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP: // отпускание
                float difX, difY;
                difX = event.getX() - BeginMotionPoint.x;
                difY = event.getY() - BeginMotionPoint.y;
                if (Math.abs(difX) > Math.abs(difY))
                    if (difX > 0) {
                        step(1, 0);
                    } else {
                        step(-1, 0);
                    }
                if (Math.abs(difX) < Math.abs(difY))
                    if (difY < 0) {
                        step(0, -1);
                    } else {
                        step(0, 1);
                    }
                break;
        }
        return true;
    }

    public boolean InRange(Point point) {
        if (point.x < 0 || point.x >= ColumnCount)
            return false;
        if (point.y < 0 || point.y >= RowCount)
            return false;
        if (CurrMap[point.y][point.x] == Cell.None)
            return true;
        if (CurrMap[point.y][point.x] == Cell.Point)
            return true;
        return false;
    }

    public void ShowMapTop(Point point) {
        TableLayout tableLayout = (TableLayout) getActivity().findViewById(R.id.tabLayout);
        TableRow tableRow = (TableRow) tableLayout.getChildAt(point.y);
        ImageView imageView = (ImageView) tableRow.getChildAt(point.x);
        if (TopMap[point.y][point.x] == Cell.None)
            imageView.setImageResource(get_texture(CurrMap[point.y][point.x]));
        else if (TopMap[point.y][point.x] == Cell.Box && CurrMap[point.y][point.x] == Cell.Point) {
            imageView.setImageResource(get_texture(Cell.BoxDone));
            if (SoundState) sp.play(soundIdPark, 1, 1, 1, 1, 1);
        } else if (TopMap[point.y][point.x] == Cell.User && CurrMap[point.y][point.x] == Cell.Point) {
            imageView.setImageResource(get_texture(Cell.UserOnPoint));
            if (SoundState) sp.play(soundIdStep, 0.5f, 0.5f, 1, 1, 1);
        } else {
            imageView.setImageResource(get_texture(TopMap[point.y][point.x]));
            if (SoundState) sp.play(soundIdStep, 0.5f, 0.5f, 1, 1, 1);
        }
    }

    public boolean IsDone() {
        if (TotalBoxCount == PlacedBoxCount)
            return true;
        else
            return false;
    }

    public void LoadPref() {
        Log.d(TAG, "LoadPref: GameWorld");
        sPref = getActivity().getSharedPreferences("settings", 0);
        SharedPreferences.Editor ed = sPref.edit();
        SoundStateLoad = SoundState = sPref.getBoolean("SoundState", false);
        MusicStateLoad = sPref.getBoolean("MusicState", false);
        GameState = sPref.getBoolean("IsDone", true);
        Log.d(TAG, "LoadPref: game state " + GameState );
        SpeechCommandState = sPref.getBoolean("SpeechCommandState", false);
        if (!GameState) {
            TotalBoxCount = sPref.getInt("TotalBoxCount", 0);
            PlacedBoxCount = sPref.getInt("PlacedBoxCount", 0);
            char temp[] = sPref.getString("Map", "").toCharArray();
            for (int i = 0; i < RowCount; i++)
                for (int j = 0; j < ColumnCount; j++)
                    CurrMap[i][j] = CharToCell(temp[i * ColumnCount + j]);
            temp = sPref.getString("Top", "").toCharArray();
            for (int i = 0; i < RowCount; i++)
                for (int j = 0; j < ColumnCount; j++) {
                    if (temp[i * ColumnCount + j] == 'u')
                        UserPoint.set(j, i);
                    TopMap[i][j] = CharToCell(temp[i * ColumnCount + j]);
                }
        }
        ed.apply();
    }

    public void SavePref() {
        Log.d(TAG, "SavePref: GameWorld");
        sPref = getActivity().getSharedPreferences("settings", 0);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean("SpeechCommandState", SpeechCommandState);
        Log.d(TAG, "SavePref: game state " + GameState );
        ed.putBoolean("IsDone", GameState);
        Log.d(TAG, "SavePref: game state after" + GameState );
        ed.putInt("LastLevel", getCurrLevel());
        if (!GameState) {
            ed.putInt("PlacedBoxCount", PlacedBoxCount);
            ed.putInt("TotalBoxCount", TotalBoxCount);
            StringBuilder temp = new StringBuilder();
            for (int i = 0; i < RowCount; i++)
                for (int j = 0; j < ColumnCount; j++)
                    temp.append(CellToChar(CurrMap[i][j]));
            ed.putString("Map", temp.toString());
            temp.setLength(0);
            for (int i = 0; i < RowCount; i++)
                for (int j = 0; j < ColumnCount; j++)
                    temp.append(CellToChar(TopMap[i][j]));
            ed.putString("Top", temp.toString());
        }
        ed.apply();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        SavePref();
        Switch swtch = (Switch) getActivity().findViewById(R.id.speech_command);
        swtch.setChecked(false);
        mModel.deleteObserver(this);
        super.onStop();
    }


    @Override
    public void update(Observable observable, Object data) {
        switch (mModel.getSpeechRecognitionResult()) {
            case 0:
                step(1, 0);
                break;
            case 1:
                step(-1, 0);
                break;
            case 2:
                step(0, -1);
                break;
            case 3:
                step(0, 1);
                break;
            case 4:
                if (btnCancel.isEnabled()) btnCancel.callOnClick();
                break;
            case 5:
                btnMenu.callOnClick();
                break;
        }
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
    }

}