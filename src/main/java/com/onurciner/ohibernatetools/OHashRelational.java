package com.onurciner.ohibernatetools;

import java.util.ArrayList;

/**
 * Created by Onur.Ciner on 22.11.2016.
 */

public class OHashRelational<GJoinColumn, GJoinColumnField, GoneBean, GBeanField, GKey, GCascade, GFetch> {

    private ArrayList<GJoinColumn> JoinColumn = new ArrayList<>();
    private ArrayList<GJoinColumnField> JoinColumnField = new ArrayList<>();
    private ArrayList<GoneBean> oneBean = new ArrayList<>();
    private ArrayList<GBeanField> BeanField = new ArrayList<>();
    private ArrayList<GKey> Key = new ArrayList<>();
    private ArrayList<GCascade> Cascade = new ArrayList<>();
    private ArrayList<GFetch> Fetch = new ArrayList<>();

    public void add(GJoinColumn k, GJoinColumnField t, GoneBean u, GBeanField r, GKey c, GCascade cascade, GFetch fetch) {
        if (k != null && t != null && u != null && r != null && c != null) {
            this.JoinColumn.add(k);
            this.JoinColumnField.add(t);
            this.oneBean.add(u);
            this.BeanField.add(r);
            this.Key.add(c);
            this.Cascade.add(cascade);
            this.Fetch.add(fetch);
        }
    }

    public GJoinColumn getJoinColumn(int i){
        if(JoinColumn.size()>i){
            return JoinColumn.get(i);
        }else
            return null;
    }


    public GJoinColumnField getJoinColumnField(int i){
        if(JoinColumnField.size()>i){
            return JoinColumnField.get(i);
        }else
            return null;
    }

    public GoneBean getOneBean(int i){
        if(oneBean.size()>i){
            return oneBean.get(i);
        }else
            return null;
    }

    public GBeanField getBeanField(int i){
        if(BeanField.size()>i){
            return BeanField.get(i);
        }else
            return null;
    }

    public GKey getKey(int i){
        if(Key.size()>i){
            return Key.get(i);
        }else
            return null;
    }

    public GCascade getCascade(int i){
        if(Cascade.size()>i){
            return Cascade.get(i);
        }else
            return null;
    }

    public ArrayList<GJoinColumn> getJoinColumnArrayList(){
        return JoinColumn;
    }

    public ArrayList<GJoinColumnField> getJoinColumnFieldArrayList(){
        return JoinColumnField;
    }

    public ArrayList<GoneBean> getOneBeanArrayList(){
        return oneBean;
    }

    public ArrayList<GBeanField> getBeanFieldArrayList(){
        return BeanField;
    }

    public ArrayList<GKey> getKeyArrayList(){
        return Key;
    }

    public ArrayList<GCascade> getCascadeArrayList(){
        return Cascade;
    }


    public GJoinColumn[] getJoinColumnArray(){
        GJoinColumn[] array = (GJoinColumn[]) new Object[JoinColumn.size()];
        for(int i = 0; i< JoinColumn.size(); i++){
            array[i] = JoinColumn.get(i);
        }
        return array;
    }

    public GJoinColumnField[] getJoinColumnFieldArray(){
        GJoinColumnField[] array = (GJoinColumnField[]) new Object[JoinColumnField.size()];
        for(int i = 0; i< JoinColumnField.size(); i++){
            array[i] = JoinColumnField.get(i);
        }
        return array;
    }

    public GoneBean[] getOneBeanArray(){
        GoneBean[] array = (GoneBean[]) new Object[oneBean.size()];
        for(int i = 0; i< oneBean.size(); i++){
            array[i] = oneBean.get(i);
        }
        return array;
    }

    public GBeanField[] getBeanFieldArray(){
        GBeanField[] array = (GBeanField[]) new Object[BeanField.size()];
        for(int i = 0; i< BeanField.size(); i++){
            array[i] = BeanField.get(i);
        }
        return array;
    }

    public GKey[] getKeyArray(){
        GKey[] array = (GKey[]) new Object[Key.size()];
        for(int i = 0; i< Key.size(); i++){
            array[i] = Key.get(i);
        }
        return array;
    }

    public GCascade[] getCascadeArray(){
        GCascade[] array = (GCascade[]) new Object[Cascade.size()];
        for(int i = 0; i< Cascade.size(); i++){
            array[i] = Cascade.get(i);
        }
        return array;
    }

    public int size(){
        if (JoinColumn != null && JoinColumnField != null) {
           return JoinColumn.size();
        }
        return 0;
    }
}
