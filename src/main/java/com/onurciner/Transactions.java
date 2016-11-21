package com.onurciner;

import java.util.ArrayList;

import jsqlite.Exception;

/**
 * Created by Onur.Ciner on 18.11.2016.
 */

public interface Transactions {

    public void define(ArrayList<String> fieldsValues,ArrayList<String> fields, ArrayList<String> fieldsTypes, String tableName, String id_fieldName);

    public String insert() throws Exception;

    public void update() throws Exception;

    public void update(String key, String value) throws Exception;

    public void delete() throws Exception;

    public void delete(String key, String value) throws Exception;
}
