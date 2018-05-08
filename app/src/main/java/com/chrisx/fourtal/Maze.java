package com.chrisx.fourtal;

class Maze {
    private final String ALPHABET = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private int[] arr;

    Maze(String s) {
        //ex. #KNHCADBEIGPOLOCMMJFQLGNHBQPEADKIFJ#
        arr = new int[s.length()];
        for (int i = 0; i < s.length(); i++) {
            arr[i] = ALPHABET.indexOf(s.charAt(i));
        }
    }

    void draw() {
        if (arr.length == 16) {
            for (int i = 0; i < arr.length; i++) {
                float x = (i%4) * MainActivity.w()/4,
                        y = MainActivity.h()/2 + (i/4-2) * MainActivity.w()/4;
                MainActivity.canvas.drawBitmap(MainActivity.portals4[arr[i]],x,y,null);
            }
        } else if (arr.length == 36) {
            for (int i = 0; i < arr.length; i++) {
                float x = (i%6) * MainActivity.w()/6,
                        y = MainActivity.h()/2 + (i/6-3) * MainActivity.w()/6;
                MainActivity.canvas.drawBitmap(MainActivity.portals6[arr[i]],x,y,null);
            }
        }
    }
}
