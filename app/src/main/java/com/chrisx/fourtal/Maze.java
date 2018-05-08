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
}
