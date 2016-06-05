package com.lihoy21gmail.audioprogect;


import android.util.Log;

public class BDLevels {
    private final String TAG = "myLogs";
    private String levels;
    private int height;
    private int width;
    private int TotalCountLvls=11;

    BDLevels(){}
    BDLevels(int lvl) {
        switch (lvl) {
            case 0:
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
            case 1:
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
            case 2:
                height = 10;
                width = 12;
                levels = "############" +
                        "#u         #" +
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
                height = 9;
                width = 8;
                levels = "########" +
                        "###   ##" +
                        "#pub  ##" +
                        "### bp##" +
                        "#p##b ##" +
                        "# # p ##" +
                        "#b dbbp#" +
                        "#   p  #" +
                        "########";
                break;
            case 4:
                height = 8;
                width = 8;
                levels = "########" +
                        "##  ####" +
                        "##b    #" +
                        "## p#  #" +
                        "#  #p ##" +
                        "#u   b##" +
                        "####  ##" +
                        "########";
                break;
            case 5:
                height =7 ;
                width =7 ;
                levels = "#######" +
                        "# u#  #" +
                        "#bbbb #" +
                        "# dp  #" +
                        "#pdd  #" +
                        "#pp  ##" +
                        "#######";
                break;
            case 6:
                height = 9;
                width = 12;
                levels = "############" +
                        "#u  #  #  ##" +
                        "#  b#b   b##" +
                        "##  #pp#  ##" +
                        "##  #pp#  ##" +
                        "##  #pp#  ##" +
                        "##b   b#b  #" +
                        "##  #  #   #" +
                        "############";
                break;
            case 7:
                height = 8;
                width = 8;
                levels = "########" +
                        "#  u####" +
                        "#  bp  #" +
                        "###bp# #" +
                        "#  bp# #" +
                        "# #bp  #" +
                        "#    ###" +
                        "########";
                break;
            case 8:
                height =9;
                width =15;
                levels = "###############" +
                        "##  ###########" +
                        "##b pppppppp  #" +
                        "##  #######b  #" +
                        "##b #  # b    #" +
                        "## ##b     #b #" +
                        "# b     b  #  #" +
                        "#   # u########" +
                        "###############";
                break;
            case 9:
                height = 10;
                width = 7;
                levels = "#######" +
                        "#  ####" +
                        "#    ##" +
                        "# b  ##" +
                        "### ###" +
                        "# b b #" +
                        "#ppupp#" +
                        "#  b  #" +
                        "###  ##" +
                        "#######";
                break;
            case 10:
                height = 8;
                width = 11;
                levels = "###########" +
                        "####### u #" +
                        "# b b bbb #" +
                        "#ppppppppp#" +
                        "#b b b b  #" +
                        "# # #######" +
                        "#   #######" +
                        "###########";
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
