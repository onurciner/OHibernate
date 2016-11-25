package com.onurciner;

import android.util.Log;

import com.onurciner.ohibernatetools.OHash;

import java.util.ArrayList;

import jsqlite.Exception;
import jsqlite.Stmt;

/**
 * Created by Onur.Ciner on 18.11.2016.
 */

public class Process implements Transactions {

    private String id_fieldName;
    private String tableName;
    private ArrayList<String> fields;
    private ArrayList<String> fieldsValues;
    private ArrayList<String> fieldsTypes;

    private OHash<String, Object> whereData;
    private Integer andConnector;
    private Integer orConnector;

    @Override
    public void define(ArrayList<String> fieldsValues, ArrayList<String> fields, ArrayList<String> fieldsTypes, String tableName, String id_fieldName, OHash<String, Object> whereData, Integer andConnector, Integer orConnector) {
        this.fieldsValues = fieldsValues;
        this.fields = fields;
        this.tableName = tableName;
        this.id_fieldName = id_fieldName;
        this.fieldsTypes = fieldsTypes;
        this.whereData = whereData;
        this.andConnector = andConnector;
        this.orConnector = orConnector;
    }

    @Override
    public String insert() throws Exception {

        String key = "";
        String keyValue = "";

        for (int i = 0; i < fields.size(); i++) {

            key += ", " + fields.get(i) + "";
            if (fieldsValues.get(i).contains("Transform(GeometryFromText"))
                keyValue += ", " + fieldsValues.get(i) + " ";
            else if (fieldsTypes.get(i).equals("BLOB"))
                keyValue += ", X'" + fieldsValues.get(i) + "' ";
            else
                keyValue += ", '" + fieldsValues.get(i) + "'";

        }

        String keys = key.substring(2, key.length());
        String keyValues = keyValue.substring(2, keyValue.length());

        String sql = "INSERT INTO " + tableName + " (" + keys + ") VALUES(" + keyValues + ")";


        OHibernateConfig.db.exec(sql, null);

        try {
            return getLastID();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", "getLastID() -> " + e.getMessage());
        }
        return "";
    }

    //UPDATE
    @Override
    public void update() throws Exception {

        String id = "";
        String key = "";

        for (int i = 0; i < fields.size(); i++) {
            if (fieldsValues.get(i).contains("Transform(GeometryFromText"))
                key += ", " + fields.get(i) + "=" + fieldsValues.get(i) + " ";
            else if (fieldsTypes.get(i).equals("BLOB"))
                key += ", " + fields.get(i) + "= X'" + fieldsValues.get(i) + "'";
            else
                key += ", " + fields.get(i) + "='" + fieldsValues.get(i) + "'";

            if (fields.get(i).equals(id_fieldName))
                id = fieldsValues.get(i);

        }
        String keys = key.substring(2, key.length());

        String sql = null;

        //-------->>Where
        if (whereData != null && whereData.size() > 0) {
            if(andConnector != null && andConnector>0){
                String wherer = "";
                for(int i = 0; i<whereData.getKeysArrayList().size();i++){
                    wherer += " "+ whereData.getKeysArrayList().get(i) +"='"+whereData.getValuesArrayList().get(i)+"' and";
                }
                wherer = wherer.substring(0,wherer.length()-3);
                sql = "UPDATE " + tableName + " SET " + keys + " WHERE"+wherer+" ";
            }else if(orConnector != null && orConnector>0){
                String wherer = "";
                for(int i = 0; i<whereData.getKeysArrayList().size();i++){
                    wherer += " "+ whereData.getKeysArrayList().get(i) +"='"+whereData.getValuesArrayList().get(i)+"' or";
                }
                wherer = wherer.substring(0,wherer.length()-2);
                sql = "UPDATE " + tableName + " SET " + keys + " WHERE"+wherer+" ";
            }else{
                sql = "UPDATE " + tableName + " SET " + keys + " WHERE " + whereData.getKey(0) + " = '" + whereData.getValue(0) + "'";
            }
        } else {
            sql = "UPDATE " + tableName + " SET " + keys + " WHERE " + id_fieldName + " = '" + id + "'";
        }
        //-------->>Where #############

        OHibernateConfig.db.exec(sql, null);

    }

    @Override
    public void update(String key, Object value) throws Exception {

        String keye = "";

        for (int i = 0; i < fields.size(); i++) {
            if (fieldsValues.get(i).contains("Transform(GeometryFromText"))
                keye += ", " + fields.get(i) + "=" + fieldsValues.get(i) + " ";
            else if (fieldsTypes.get(i).equals("BLOB"))
                keye += ", " + fields.get(i) + "= X'" + fieldsValues.get(i) + "'";
            else
                keye += ", " + fields.get(i) + "='" + fieldsValues.get(i) + "'";
        }
        String keys = keye.substring(2, keye.length());

        String sql = "UPDATE " + tableName + " SET " + keys + " where " + key + " = '" + value + "'";

        OHibernateConfig.db.exec(sql, null);

    }

    //DELETE
    @Override
    public void delete() throws Exception {

        String id = "";

        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).equals(id_fieldName))
                id = fieldsValues.get(i);
        }

        String sql = "DELETE FROM " + tableName + " WHERE " + id_fieldName + "='" + id + "'";

        //-------->>Where
        if (whereData != null && whereData.size() > 0) {
            if(andConnector != null && andConnector>0){
                String wherer = "";
                for(int i = 0; i<whereData.getKeysArrayList().size();i++){
                    wherer += " "+ whereData.getKeysArrayList().get(i) +"='"+whereData.getValuesArrayList().get(i)+"' and";
                }
                wherer = wherer.substring(0,wherer.length()-3);
                sql = "DELETE FROM "+ tableName +" WHERE" +wherer+" ";
            }else if(orConnector != null && orConnector>0){
                String wherer = "";
                for(int i = 0; i<whereData.getKeysArrayList().size();i++){
                    wherer += " "+ whereData.getKeysArrayList().get(i) +"='"+whereData.getValuesArrayList().get(i)+"' or";
                }
                wherer = wherer.substring(0,wherer.length()-2);
                sql = "DELETE FROM "+ tableName +" WHERE" +wherer+" ";
            }else{
                sql = "DELETE FROM "+ tableName +" WHERE " + whereData.getKey(0) + " = '" + whereData.getValue(0) + "'";
            }
        } else {
            sql = "DELETE FROM " + tableName + " WHERE " + id_fieldName + "='" + id + "'";
        }
        //-------->>Where #############

        OHibernateConfig.db.exec(sql, null);

    }

    @Override
    public void delete(String key, Object value) throws Exception {

        String sql = "DELETE FROM " + tableName + " WHERE " + key + "='" + value + "'";

        OHibernateConfig.db.exec(sql, null);

    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    private String getLastID() throws Exception {
        String one = "";
        String two = "";
        String sql = "SELECT MAX(" + id_fieldName + ") FROM " + tableName + " ";
        try {
            Stmt stmt = OHibernateConfig.db.prepare(sql);
            while (stmt.step()) {
                if (stmt.column(0) != null && stmt.column(0).toString() != null)
                    one = stmt.column(0).toString();
                else
                    one = "0";
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", "getLastID() -> " + e.getMessage());
        }
        //-----------------------
        String sql2 = "SELECT MIN(" + id_fieldName + ") FROM " + tableName + " ";
        try {
            Stmt stmt = OHibernateConfig.db.prepare(sql2);
            while (stmt.step()) {
                if (stmt.column(0) != null && stmt.column(0).toString() != null)
                    two = stmt.column(0).toString();
                else
                    two = "0";
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", "getLastID() -> " + e.getMessage());
        }
        //-----------------------
        if (!one.equals("") && !two.equals("") && !one.equals("null") && !two.equals("null")) {
            boolean oneB = false;
            boolean twoB = false;
            if (one.contains("-")) {
                one = one.replace("-", "");
                oneB = true;
            }
            if (two.contains("-")) {
                two = two.replace("-", "");
                twoB = true;
            }
            int Ione = (int) Double.parseDouble(one);
            int Itwo = (int) Double.parseDouble(two);

            if (Ione > Itwo)
                if (oneB)
                    return "-" + Ione;
                else
                    return Ione + "";
            else if (Itwo > Ione)
                if (twoB)
                    return "-" + Itwo;
                else
                    return Itwo + "";
            else {
                if (oneB)
                    return "-" + Ione;
                else
                    return Ione + "";
            }
        }
        return "0";
    }

}
