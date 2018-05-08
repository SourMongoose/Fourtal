package com.chrisx.fourtal;

import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;

class Maze {
    private final String ALPHABET = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private int[] arr;
    private int moves;
    private Paint opacity;

    Maze(String s) {
        //ex. #KNHCADBEIGPOLOCMMJFQLGNHBQPEADKIFJ#
        arr = new int[s.length()];
        for (int i = 0; i < s.length(); i++) {
            arr[i] = ALPHABET.indexOf(s.charAt(i));
        }

        moves = calcMoves();

        opacity = new Paint();
        opacity.setAlpha(200);
    }

    private int calcMoves() {
        int[] dist = new int[arr.length];
        for (int i = 0; i < dist.length; i++) dist[i] = -1;
        dist[0] = 0;

        ArrayList<Integer> q = new ArrayList<>();
        q.add(0);
        while (!q.isEmpty()) {
            int curr = q.get(0);
            q.remove(0);

            //left
            if (curr % size() > 0 && dist[match(curr - 1)] == -1) {
                dist[match(curr - 1)] = dist[curr] + 1;
                q.add(match(curr - 1));
            }
            //right
            if ((curr + 1) % size() > 0 && dist[match(curr + 1)] == -1) {
                dist[match(curr + 1)] = dist[curr] + 1;
                q.add(match(curr + 1));
            }
            //up
            if (curr >= size() && dist[match(curr - size())] == -1) {
                dist[match(curr - size())] = dist[curr] + 1;
                q.add(match(curr - size()));
            }
            //down
            if (curr + size() < arr.length && dist[match(curr + size())] == -1) {
                dist[match(curr + size())] = dist[curr] + 1;
                q.add(match(curr + size()));
            }
        }

        return dist[dist.length-1];
    }

    int size() {
        return (int)Math.round(Math.sqrt(arr.length));
    }
    int getMoves() {
        return moves;
    }

    int match(int i) {
        if (i == 0 || i == arr.length-1) return i;
        for (int x = 0; x < arr.length; x++) {
            if (arr[x] == arr[i] && i != x) return x;
        }
        return -1;
    }

    void draw() {
        float w = MainActivity.w() / size();
        float brX = MainActivity.w() - w/2;
        float brY = MainActivity.h()/2 + MainActivity.w()/2 - w/2;
        MainActivity.canvas.drawBitmap(MainActivity.finish, null, new RectF(brX-w/2,brY-w/2,brX+w/2,brY+w/2), opacity);

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
