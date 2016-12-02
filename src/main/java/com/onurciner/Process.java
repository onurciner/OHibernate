package com.onurciner;

import android.util.Log;

import com.onurciner.ohibernatetools.Conditions;

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
    private Conditions conditions;

    @Override
    public void define(ArrayList<String> fieldsValues, ArrayList<String> fields, ArrayList<String> fieldsTypes, String tableName, String id_fieldName, Conditions conditions) {
        this.fieldsValues = fieldsValues;
        this.fields = fields;
        this.tableName = tableName;
        this.id_fieldName = id_fieldName;
        this.fieldsTypes = fieldsTypes;
        this.conditions = conditions;
    }

    @Override
    public String insert() throws Exception {

        String key = "";
        String keyValue = "";

        for (int i = 0; i < fields.size(); i++) {

            key += ", " + fields.get(i) + "";
            if (fieldsValues.get(i) != null) {
                if (fieldsValues.get(i).contains("Transform(GeometryFromText"))
                    keyValue += ", " + fieldsValues.get(i) + " ";
                else if (fieldsTypes.get(i).equals("BLOB"))
                    keyValue += ", X'" + fieldsValues.get(i) + "' ";
                else
                    keyValue += ", '" + fieldsValues.get(i) + "'";
            } else {
                keyValue += ", NULL ";
            }
        }

        String keys = key.substring(2, key.length());
        String keyValues = keyValue.substring(2, keyValue.length());

        String sql = "INSERT INTO " + tableName + " (" + keys + ") VALUES(" + keyValues + ")";

        OHibernateConfig.db.exec(sql, null);

        Log.i("OHibernate -> Info", "Table Name:"+tableName+". Object INSERTED");
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
            if (fieldsValues.get(i) != null) {
                if (fieldsValues.get(i).contains("Transform(GeometryFromText"))
                    key += ", " + fields.get(i) + "=" + fieldsValues.get(i) + " ";
                else if (fieldsTypes.get(i).equals("BLOB"))
                    key += ", " + fields.get(i) + "= X'" + fieldsValues.get(i) + "'";
                else
                    key += ", " + fields.get(i) + "='" + fieldsValues.get(i) + "'";
            } else {
                key += ", " + fields.get(i) + "= NULL ";
            }

            if (fields.get(i).equals(id_fieldName))
                id = fieldsValues.get(i);

        }
        String keys = key.substring(2, key.length());

        String sql = null;

        //-------->>Where
        if (this.conditions.getWhereData() != null && this.conditions.getWhereData().size() > 0) {
            if (this.conditions.getAndConnector() != null && this.conditions.getAndConnector() > 0) {
                String wherer = "";
                for (int i = 0; i < this.conditions.getWhereData().getKeysArrayList().size(); i++) {
                    wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + "='" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' and";
                }
                wherer = wherer.substring(0, wherer.length() - 3);
                sql = "UPDATE " + tableName + " SET " + keys + " WHERE" + wherer + " ";
            } else if (this.conditions.getOrConnector() != null && this.conditions.getOrConnector() > 0) {
                String wherer = "";
                for (int i = 0; i < this.conditions.getWhereData().getKeysArrayList().size(); i++) {
                    wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + "='" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' or";
                }
                wherer = wherer.substring(0, wherer.length() - 2);
                sql = "UPDATE " + tableName + " SET " + keys + " WHERE" + wherer + " ";
            } else {
                sql = "UPDATE " + tableName + " SET " + keys + " WHERE " + this.conditions.getWhereData().getKey(0) + " = '" + this.conditions.getWhereData().getValue(0) + "'";
            }
        } else {
            sql = "UPDATE " + tableName + " SET " + keys + " WHERE " + id_fieldName + " = '" + id + "'";
        }
        //-------->>Where #############

        OHibernateConfig.db.exec(sql, null);

        Log.i("OHibernate -> Info", "Table Name:"+tableName+". Key:"+id_fieldName+". Value:"+id+". UPDATED");
    }

    @Override
    public void update(String key, Object value) throws Exception {

        String keye = "";

        for (int i = 0; i < fields.size(); i++) {
            if (fieldsValues.get(i) != null) {
                if (fieldsValues.get(i).contains("Transform(GeometryFromText"))
                    keye += ", " + fields.get(i) + "=" + fieldsValues.get(i) + " ";
                else if (fieldsTypes.get(i).equals("BLOB"))
                    keye += ", " + fields.get(i) + "= X'" + fieldsValues.get(i) + "'";
                else
                    keye += ", " + fields.get(i) + "='" + fieldsValues.get(i) + "'";
            } else {
                keye += ", " + fields.get(i) + "= NULL ";
            }
        }
        String keys = keye.substring(2, keye.length());

        String sql = "UPDATE " + tableName + " SET " + keys + " where " + key + " = '" + value + "'";

        OHibernateConfig.db.exec(sql, null);

        Log.i("OHibernate -> Info", "Table Name:"+tableName+". Key:"+key+". Value:"+value+". UPDATED");
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
        if (this.conditions.getWhereData() != null && this.conditions.getWhereData().size() > 0) {
            if (this.conditions.getAndConnector() != null && this.conditions.getAndConnector() > 0) {
                String wherer = "";
                for (int i = 0; i < this.conditions.getWhereData().getKeysArrayList().size(); i++) {
                    wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + "='" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' and";
                }
                wherer = wherer.substring(0, wherer.length() - 3);
                sql = "DELETE FROM " + tableName + " WHERE" + wherer + " ";
            } else if (this.conditions.getOrConnector() != null && this.conditions.getOrConnector() > 0) {
                String wherer = "";
                for (int i = 0; i < this.conditions.getWhereData().getKeysArrayList().size(); i++) {
                    wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + "='" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' or";
                }
                wherer = wherer.substring(0, wherer.length() - 2);
                sql = "DELETE FROM " + tableName + " WHERE" + wherer + " ";
            } else {
                sql = "DELETE FROM " + tableName + " WHERE " + this.conditions.getWhereData().getKey(0) + " = '" + this.conditions.getWhereData().getValue(0) + "'";
            }
        } else {
            sql = "DELETE FROM " + tableName + " WHERE " + id_fieldName + "='" + id + "'";
        }
        //-------->>Where #############

        OHibernateConfig.db.exec(sql, null);

        Log.i("OHibernate -> Info", "Table Name:"+tableName+". Key:"+id_fieldName+". Value:"+id+". DELETED");
    }

    @Override
    public void delete(String key, Object value) throws Exception {

        String sql = "DELETE FROM " + tableName + " WHERE " + key + "='" + value + "'";

        OHibernateConfig.db.exec(sql, null);

        Log.i("OHibernate -> Info", "Table Name:"+tableName+". Key:"+key+". Value:"+value+". DELETED");
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
