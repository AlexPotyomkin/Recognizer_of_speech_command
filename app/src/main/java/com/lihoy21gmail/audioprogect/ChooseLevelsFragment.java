package com.lihoy21gmail.audioprogect;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class ChooseLevelsFragment extends Fragment {
    private BDLevels lvl;
    private final int COUNT_ROW = 4;
    private final int COUNT_COLUMN = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.choose_levels_fragment, null);

        final TableLayout tableLayout = (TableLayout) v.findViewById(R.id.ChooseLevelTable);
        tableLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tableLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                init(v);

            }
        });
        return v;
    }

    private void init(View v) {
        TableLayout tableLayout = (TableLayout) v.findViewById(R.id.ChooseLevelTable);
        lvl = new BDLevels();
        int k = 0;
        for (int i = 0; i < COUNT_ROW; i++) {
            TableRow tableRow = new TableRow(getActivity());

            TableRow.LayoutParams lp1 = new TableRow.LayoutParams();
            int height = tableLayout.getHeight() / COUNT_ROW;
            int width = tableLayout.getWidth() / COUNT_COLUMN;
            TableLayout.LayoutParams lp = new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (height > width) height = width;
            if (width > height) width = height;
            lp1.height = height;
            lp1.weight = width;
            int margin = 15;
            lp1.setMargins(margin, margin, margin, margin);
            lp.setMargins(10, ((tableLayout.getHeight() -
                            (width * COUNT_ROW + (COUNT_ROW - 1) * margin * 2)) / 2) - 15, 10,
                    ((tableLayout.getHeight() -
                            (width * COUNT_ROW + (COUNT_ROW - 1) * margin * 2)) / 2) - 15);
            tableLayout.setLayoutParams(lp);
            for (int j = 0; j < COUNT_COLUMN; j++) {
                Button button = new Button(getActivity());
                if (k < lvl.getTotalCountLvls()) {
                    button.setBackgroundResource(R.drawable.box_big);
                    button.setTextColor(Color.parseColor("#ffffff"));
                    final int finalK = k;
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            GameWorldFragment gameWorldFragment = GameWorldFragment.newInstance(finalK);
                            getFragmentManager().
                                    beginTransaction().
                                    replace(android.R.id.content, gameWorldFragment).
                                    commit();
                        }
                    });
                } else {
                    button.setBackgroundResource(R.drawable.box_shadow);
                    button.setTextColor(Color.parseColor("#78ffffff"));
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getActivity(), "Пока вы сидите и играете, разработчик" +
                                    " кропотливо создает новые уровни. Наберитесь терпения:)",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                button.setText(String.valueOf(k++));
                button.setTextSize(30);
                button.setLayoutParams(lp1);
                tableRow.addView(button, j);
            }
            tableLayout.addView(tableRow, i);
        }
    }
}
