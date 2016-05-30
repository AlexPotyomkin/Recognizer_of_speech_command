package com.lihoy21gmail.audioprogect;

import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;


public class GameWorldFragment extends Fragment implements View.OnTouchListener {
    private final String TAG = "myLogs";
    private int ColumnCount;
    private int RowCount;
    private Point BeginMotionPoint = new Point(0, 0);
    private Point UserPoint = new Point(1, 1);

    enum Cell {User, Wall, None, Box, Point, UserOnPoint, BoxDone}

    private Cell[][] CurrMap;
    private Cell[][] TopMap;
    private int TotalBoxCount=0;
    private int PlacedBoxCount=0;
    private BDLevels lvl;
    public static GameWorldFragment newInstance(int lvl) {
        GameWorldFragment f = new GameWorldFragment();
        Bundle args = new Bundle();
        args.putInt("lvl", lvl);
        f.setArguments(args);
        return f;
    }

    public int getShownIndex() {
        return getArguments().getInt("lvl", 0);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        final View v = inflater.inflate(R.layout.game_world, null);
        final TableLayout tableLayout = (TableLayout) v.findViewById(R.id.tabLayout);
        tableLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tableLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                init(v);

            }
        });
        v.setOnTouchListener(this);
        return v;
    }

    public void init(View v) {
        lvl = new BDLevels(getShownIndex());
        char temp[] = lvl.getLevels().toCharArray();
        ColumnCount = lvl.getWidth();
        RowCount = lvl.getHeight();
        CurrMap = new Cell[RowCount][ColumnCount];
        TopMap = new Cell[RowCount][ColumnCount];
        for (int i = 0; i < RowCount; i++)
            for (int j = 0; j < ColumnCount; j++)
                CurrMap[i][j] = CharToCell(temp[i * ColumnCount + j]);

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
                imageView.setImageResource(get_texture(CurrMap[i][j]));

                imageView.setLayoutParams(lp1);
                tableRow.addView(imageView, j);
            }
            tableLayout.addView(tableRow, i);
        }
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

    public int get_texture(Cell cell) {
        //if(cell == null) return;
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

    public void step(int dx, int dy) {
        Point place;
        Point after;
        Log.d(TAG, "here");
        place = new Point(UserPoint.x + dx, UserPoint.y + dy);
        if (!InRange(place))
            return;
        if (TopMap[place.y][place.x] == Cell.None) {
            TopMap[UserPoint.y][UserPoint.x] = Cell.None;
            ShowMapTop(UserPoint);
            TopMap[place.y][place.x] = Cell.User;
            ShowMapTop(place);
            UserPoint = place;
        }
        if (TopMap[place.y][place.x] == Cell.Box) {
            after = new Point(place.x + dx, place.y + dy);
            if (!InRange(after))
                return;
            if (TopMap[after.y][after.x] != Cell.None)
                return;
            if(CurrMap[place.y][place.x]==Cell.Point) PlacedBoxCount--;
            if(CurrMap[after.y][after.x]==Cell.Point) PlacedBoxCount++;

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
            if(lvl.getTotalCountLvls()>getShownIndex()+1)
                ((MainActivity) getActivity()).LoadLevel(getShownIndex() + 1);
            else
                ((MainActivity) getActivity()).LoadLevel(0);
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
                        //Toast.makeText(getActivity(), "Вправо", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onTouch: Вправо");
                        step(1, 0);
                    } else {
                        //Toast.makeText(getActivity(), "Влево", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onTouch: Влево");
                        step(-1, 0);
                    }
                if (Math.abs(difX) < Math.abs(difY))
                    if (difY < 0) {
                        //Toast.makeText(getActivity(), "Вверх", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onTouch: Вверх");
                        step(0, -1);
                    } else {
                        //Toast.makeText(getActivity(), "Вниз", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onTouch: Вниз");
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
        else if (TopMap[point.y][point.x] == Cell.Box && CurrMap[point.y][point.x] == Cell.Point)
            imageView.setImageResource(get_texture(Cell.BoxDone));
        else if (TopMap[point.y][point.x] == Cell.User && CurrMap[point.y][point.x] == Cell.Point)
            imageView.setImageResource(get_texture(Cell.UserOnPoint));
        else {
            imageView.setImageResource(get_texture(TopMap[point.y][point.x]));
        }
    }

    public boolean IsDone() {
        Log.d(TAG, "Total = " +TotalBoxCount+ " Placed = " +PlacedBoxCount);
        if (TotalBoxCount == PlacedBoxCount)
            return true;
        else
            return false;
    }
}

