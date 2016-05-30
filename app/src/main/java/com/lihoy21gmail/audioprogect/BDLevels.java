package com.lihoy21gmail.audioprogect;


import android.util.Log;

public class BDLevels {
    private final String TAG = "myLogs";
    private String levels;
    private int height;
    private int width;
    private int TotalCountLvls;

    BDLevels(int lvl) {
        TotalCountLvls = 3;
        switch (lvl) {
            case 0:
                height = 10;
                width = 10;
                levels = "##########" +
                        "#u      p#" +
                        "# b#     #" +
                        "#  #     #" +
                        "#  ###   #" +
                        "#    ##  #" +
                        "#     ## #" +
                        "# b    ###" +
                        "#       p#" +
                        "##########";
                break;
            case 1:
                height = 7;
                width = 7;
                levels = "#######" +
                         "#  u#p#" +
                         "# b## #" +
                         "#  ## #" +
                         "#     #" +
                         "#     #" +
                         "#######";
                break;
            case 2:
                height = 10;
                width = 12;
                levels = "############" +
                         "#         u#" +
                         "# b    b b #" +
                         "# ##  ##   #" +
                         "# #p  pp## #" +
                         "# #p  pp## #" +
                         "# ##  ##   #" +
                         "# b    b b #" +
                         "#          #" +
                         "############";
                break;
            case 3:
                height = 5;
                width = 6;
                levels = "##########" +
                        "#u      h#" +
                        "# b#     #" +
                        "#  #     #" +
                        "#  ###   #" +
                        "#    ##  #" +
                        "#     ## #" +
                        "# b    ###" +
                        "#       h#" +
                        "##########";
                break;
            case 4:
                height = 5;
                width = 6;
                levels = "##########" +
                        "#u      h#" +
                        "# b#     #" +
                        "#  #     #" +
                        "#  ###   #" +
                        "#    ##  #" +
                        "#     ## #" +
                        "# b    ###" +
                        "#       h#" +
                        "##########";
                break;
        }
    }

    public String getLevels() {
        return levels;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getTotalCountLvls() {
        return TotalCountLvls;
    }
}
