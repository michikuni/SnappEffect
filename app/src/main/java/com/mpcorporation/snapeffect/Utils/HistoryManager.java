package com.mpcorporation.snapeffect.Utils;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager <T>{
    private final List<T> history;
    private int idx;
    private int len;

    public HistoryManager() {
        this.history = new ArrayList<>();
        this.idx = -1;
        this.len = 0;
    }
    public void add (T item) {
        this.history.add(++idx, item);
        len = idx+1;
    }
    public void delete (T item){
        if (idx > 0){
            this.history.remove(idx--);
            len--;
        }
    }
    public void undo (){
        if (idx > 0) {
            idx--;
        }
    }
    public void redo (){
        if (idx < len-1){
            idx++;
        }
    }
    public T get(){
        if (idx >= 0 && idx < len){
            return history.get(idx);
        } else {
            return null;
        }
    }
    public void clear(){
        history.clear();
        idx = -1;
        len = 0;
    }
    public int length(){
        return history.size();
    }
}
