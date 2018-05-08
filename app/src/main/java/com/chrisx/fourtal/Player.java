package com.chrisx.fourtal;

import android.graphics.Canvas;
import android.graphics.RectF;

class Player {
    private final int ANIMATION_MAX = 60;

    private int pos, prevPos, animation;
    private Maze maze;

    Player(Maze maze) {
        this.maze = maze;
        pos = 0;
    }

    void setMaze(Maze maze) {
        this.maze = maze;
    }

    void reset() {
        pos = prevPos = 0;
    }

    int getPos() {
        return pos;
    }

    boolean atEnd() {
        return animation < ANIMATION_MAX / 2 && pos == maze.size()*maze.size()-1;
    }

    void goTo(int p) {
        if (animation > 0) return;

        prevPos = pos;
        pos = p;
        animation = ANIMATION_MAX;
    }

    void draw() {
        Canvas c = MainActivity.canvas;
        float w = MainActivity.w() / maze.size();
        float x = w/2 + (pos%4) * w,
                y = w/2 + MainActivity.h()/2 + (pos/maze.size()-maze.size()/2) * w;

        if (animation == 0) {
            c.drawBitmap(MainActivity.dot, null, new RectF(x-w/6,y-w/6,x+w/6,y+w/6), null);
        } else {
            float scale;
            if (animation > ANIMATION_MAX/2) {
                //jump through starting portal
                float   x1 = w/2 + (prevPos%4) * w,
                        y1 = w/2 + MainActivity.h()/2 + (prevPos/maze.size()-maze.size()/2) * w,
                        x2 = w/2 + (pos%4) * w,
                        y2 = w/2 + MainActivity.h()/2 + (pos/maze.size()-maze.size()/2) * w;
                x = x2 + (x1 - x2) * ((float)(animation-ANIMATION_MAX/2)/(ANIMATION_MAX/2));
                y = y2 + (y1 - y2) * ((float)(animation-ANIMATION_MAX/2)/(ANIMATION_MAX/2));
                scale = -9f + 0.433333f*animation - 0.00444444f*animation*animation;
            } else {
                //jump from ending portal
                x = w/2 + (maze.match(pos)%4) * w;
                y = w/2 + MainActivity.h()/2 + (maze.match(pos)/maze.size()-maze.size()/2) * w;
                scale = 1f + 0.1f*animation - 0.00444444f*animation*animation;
            }
            c.save();
            c.translate(x, y);
            c.scale(scale, scale);
            c.drawBitmap(MainActivity.dot, null, new RectF(-w/6,-w/6,w/6,w/6), null);
            c.restore();
        }

        if (animation > 0) {
            animation--;
            if (animation == 0) pos = maze.match(pos);
        }
    }
}
