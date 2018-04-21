package com.status.brickgame;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Shape {
    public List<Integer> list = new ArrayList<Integer>(4);
    private int orientation;
    private int color;
    private int preview;
    private int type;
    private int countMove;
    private List<Integer> copy;
    private Semaphore sem;

    public Shape(int type, int orientation, Semaphore sem){
        this.type = type;
        this.countMove = 0;
        this.orientation = 1 ;
        this.copy = new ArrayList<Integer>(4);
        this.sem = sem;

        init();
        for(int i = 1; i < orientation; i++){
            rotate(true);
        }
        up();

    }

    private void init(){
        switch (type) {
            case 1:
                color = R.drawable.yellow;
                preview = R.drawable.yellow_p;
                list.add(-5);
                list.add(-4);
                list.add(-3);
                list.add(-2);
                break;
            case 2:
                color = R.drawable.lightblue;
                preview = R.drawable.lightblue_p;
                list.add(-15);
                list.add(-14);
                list.add(-13);
                list.add(-4);
                break;
            case 3:
                color = R.drawable.green;
                preview = R.drawable.green_p;
                list.add(-15);
                list.add(-14);
                list.add(-5);
                list.add(-4);
                break;
            case 4:
                color = R.drawable.blue;
                preview = R.drawable.blue_p;
                list.add(-15);
                list.add(-14);
                list.add(-4);
                list.add(-3);
                break;
            case 5:
                color = R.drawable.violet;
                preview = R.drawable.violet_p;
                list.add(-14);
                list.add(-13);
                list.add(-5);
                list.add(-4);
                break;
            case 6:
                color = R.drawable.red;
                preview = R.drawable.red_p;
                list.add(-15);
                list.add(-14);
                list.add(-13);
                list.add(-3);
                break;
            case 7:
                color = R.drawable.orange;
                preview = R.drawable.orange_p;
                list.add(-15);
                list.add(-14);
                list.add(-13);
                list.add(-5);
                break;
        }
    }

    public int getPreview(){

        return preview;
    }

    public void clear(List<Integer> copyList){

//        if(copy.size() < 4) {
//            String message = "ERROR clear; size of copy < 4; size of copy = " + copy.size();
//            MainActivity.writeLog(message);
//        }
//        checkShape("begin clear ");

       for (Integer item : copyList) {
            if(item.intValue() >= 0) {
                MainActivity.listCells.set(item.intValue(), 0);
            }
        }
    }

    public void rebuildListCells(){
//        checkShape("begin rebuild ");
//        if(MainActivity.debug) {
//            MainActivity.writeLog("rebuild " + " " + type + " " + list);
//        }
        for(Integer item: list){
            if(item.intValue() >= 0) {
                MainActivity.listCells.set(item.intValue(), color);
            }
        }
    }

    public void rotate(boolean constructor){
        int min_x = MainActivity.WIDTH, min_y = MainActivity.HEIGHT, max_y = -MainActivity.HEIGHT, shift = 0, new_x, new_y, x,y,i;
        boolean error = true;

        try {
            sem.acquire();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

        //debug
//        if(copy.size() > 0){
//            String message = "ERROR rotate; size of copy > 0; size of copy = " + copy.size();
//            MainActivity.writeLog(message);
//        }
//        checkShape("begin rotate ");
//        if(MainActivity.debug) {
//            MainActivity.writeLog("begin rotate " + type + " " + list);
//        }
        //debug

        copy.addAll(list);

//рассчитываем положение фигуры
        for(Integer item: list){
            x = item.intValue()%MainActivity.WIDTH;
            y = (int)(item.intValue()/MainActivity.WIDTH);

            if(x < 0){
                y--;
                x = MainActivity.WIDTH + x;
            }

            if(min_x > x){
                min_x = x;
            }
            if(min_y > y){
                min_y = y;
            }
            if(max_y < y){
                max_y = y;
            }
        }


//вращаем фигуру
        shift = 0;
        do{
            error = false;
            for(i=0; i<4; i++) {
                Integer item = list.get(i);
                x = item.intValue() % MainActivity.WIDTH;
                y = (int) (item.intValue() / MainActivity.WIDTH);

                if(x < 0){
                    y--;
                    x = MainActivity.WIDTH + x;
                }

                new_x = min_x + (max_y - y) - shift;
                new_y = min_y + (x - min_x);

                if (!constructor) {
                    if (new_y >= MainActivity.HEIGHT || new_x >= MainActivity.WIDTH || new_x < 0) {
                        error = true;
                        break;
                    }
                    if ((new_y * MainActivity.WIDTH + new_x) >= 0 && MainActivity.listCells.get(new_y * MainActivity.WIDTH + new_x).intValue() != 0 && !copy.contains(new_y * MainActivity.WIDTH + new_x)) {
                        error = true;
                        break;
                    }
                }
                list.set(i, new_y * MainActivity.WIDTH + new_x);
            }
            shift++;

            if(error){
                list.clear();
                list.addAll(copy);
            }
            else {
                orientation++;
                if (!constructor) {
                    clear(copy);
                    rebuildListCells();
                }
            }
        }
        while(shift < 4 && error == true && (min_x- shift) >= 0 && orientation%2 == 0);

        copy.clear();
//        if(MainActivity.debug) {
//            MainActivity.writeLog("end rotate " + type + " " + list);
//        }
        sem.release();

    }

    public boolean move(String direction){
        int new_x = 0, new_y = 0, x, y, i;
        boolean error = false;

        try {
            sem.acquire();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

        //debug
//        if(copy.size() > 0){
//            String message = "ERROR move; size of copy > 0; size of copy = " + copy.size();
//            MainActivity.writeLog(message);
//        }
//        checkShape("begin move " + direction);
//        if(MainActivity.debug) {
//            MainActivity.writeLog("begin move " + direction + " " + type + " " + list);
//        }
        //debug
        copy.addAll(list);

        for(i=0; i<4; i++) {
            Integer item = list.get(i);
            x = item.intValue() % MainActivity.WIDTH;
            y = (int) (item.intValue() / MainActivity.WIDTH);

            if(x < 0){
                y--;
                x = MainActivity.WIDTH + x;
            }

            if(direction.equals("down")){
                new_x = x;
                new_y = y + 1;
            }
            if(direction.equals("left")){
                new_x = x - 1;
                new_y = y;
            }
            if(direction.equals("right")){
                new_x = x + 1;
                new_y = y;
            }

            if (new_y >= MainActivity.HEIGHT || new_x >= MainActivity.WIDTH || new_x < 0) {
                error = true;
                break;
            }
            if((new_y * MainActivity.WIDTH + new_x) >= 0 && MainActivity.listCells.get(new_y * MainActivity.WIDTH + new_x).intValue() != 0 && !copy.contains(new_y * MainActivity.WIDTH + new_x)){
                error = true;
                break;
            }


            list.set(i, new_y * MainActivity.WIDTH + new_x);
        }

        if(error){
            if(direction.equals("down")) {
                if(countMove == 0){
                    MainActivity.gameOver = true;
                }
            }
//            if(MainActivity.debug) {
//                MainActivity.writeLog("end move false " + direction + " " + type + " " + list);
//            }
            list.clear();
            list.addAll(copy);
            copy.clear();
            sem.release();
            return  false;
        }
        else{
            if(direction.equals("down")) {
                countMove++;
            }
            clear(copy);
            rebuildListCells();
        }

        copy.clear();
//        if(MainActivity.debug) {
//            MainActivity.writeLog("end move true " + direction + " " + type + " " + list);
//        }
        sem.release();
        return true;
    }

    public void up(){
        int new_x = 0, new_y = 0, max_y = -1, x, y, i;

        try {
            sem.acquire();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

        for(i=0; i<4; i++) {
            Integer item = list.get(i);
            if(item.intValue() >= 0) {
                y = (int) (item.intValue() / MainActivity.WIDTH);
                if (y > max_y) {
                    max_y = y;
                }
            }
        }

        if(max_y >= 0){
            for(i=0; i<4; i++) {
                Integer item = list.get(i);
                x = item.intValue() % MainActivity.WIDTH;
                y = (int) (item.intValue() / MainActivity.WIDTH);

                new_x = x;
                new_y = y - (max_y + 1);

                list.set(i, new_y * MainActivity.WIDTH + new_x);
            }
        }

        sem.release();
    }

    private void checkShape(String e){
//        if(list.size() < 4) {
//            String message = e + "; size of list < 4; size of list = " + list.size();
//            MainActivity.writeLog(message);
//        }
//        else{
//
//            for(int i = 0; i < 4; i++){
//                for(int j = i + 1; j < 4; j++){
//                    if (list.get(i).intValue() == list.get(j).intValue()){
//                        String message = e + "; incorrect list " +list;
//                        MainActivity.writeLog(message);
//
//                    }
//                }
//            }
//        }

    }

}
