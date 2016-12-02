package com.onurciner.ohibernate;

import java.util.ArrayList;

/**
 * Created by Onur.Ciner on 22.11.2016.
 */

public class OHash<K, T> {

    private ArrayList<K> k = new ArrayList<>();
    private ArrayList<T> t = new ArrayList<>();

    public void add(K k, T t) {
        if (k != null && t != null) {
            this.k.add(k);
            this.t.add(t);
        }
    }

    public K getKey(int i){
        if(k.size()>i){
            return k.get(i);
        }else
            return null;
    }


    public T getValue(int i){
        if(t.size()>i){
            return t.get(i);
        }else
            return null;
    }

    public ArrayList<K> getKeysArrayList(){
        return k;
    }

    public ArrayList<T> getValuesArrayList(){
        return t;
    }

    public K[] getKeysArray(){
        K[] array = (K[]) new Object[k.size()];
        for(int i = 0;i<k.size();i++){
            array[i] = k.get(i);
        }
        return array;
    }

    public T[] getValuesArray(){
        T[] array = (T[]) new Object[t.size()];
        for(int i = 0;i<t.size();i++){
            array[i] = t.get(i);
        }
        return array;
    }

    public int size(){
        if (k != null && t != null) {
           return k.size();
        }
        return 0;
    }
}
