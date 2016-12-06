package com.onurciner;

import android.util.Log;


import com.onurciner.enums.ConditionType;
import com.onurciner.enums.LikeType;
import com.onurciner.enums.OrderByType;
import com.onurciner.ohibernatetools.Conditions;
import com.onurciner.ohibernatetools.OHash;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jsqlite.Exception;
import jsqlite.Stmt;

/**
 * Created by Onur.Ciner on 5.12.2016.
 */

public class OQuery<K> {

    private K classType;

    public OQuery() {
        new OHibernateConfig().getConfig();
    }

    private ArrayList<String> selectDatas = new ArrayList<>();
    private ArrayList<String> fromDatas = new ArrayList<>();
    private Conditions conditions = new Conditions();
    private OHash<String, Object> setParameters = new OHash<>();
    private String id_fieldName = "id"; // Default "id"
    private String tableName = "";

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public OQuery Select(String... columnNames) {

        for (String data : columnNames) {
            selectDatas.add(data);
        }
        return this;
    }

    public OQuery From(String... tableNames) {
        for (String data : tableNames) {
            fromDatas.add(data);
        }

        return this;
    }

    public OQuery addEntity(Class<K> obj) {
        try {
            this.classType = obj.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void createSQLQuery(String sql) {
        try {
            OHibernateConfig.db.exec(sql, null);

            Log.i("OHibernate -> Info", "SUCCESSFUL");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }
    }

    public ArrayList<K> list() {
        if (classType == null) {
            try {
                return (ArrayList<K>) selectQueryStrings();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                return selectQueryObjects();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public K getSingleResult() {
        if (classType == null) {
            try {
                return (K) selectQueryString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                return selectQueryObject();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public OQuery Insert(String tableName) throws Exception {

        if (setParameters != null && setParameters.size() > 0) {
            this.tableName = tableName;
            String keys = "";
            String keyValues = "";
            for (int i = 0; i < setParameters.size(); i++) {
                keys += setParameters.getKey(i) + ", ";
                if (setParameters.getValue(i) != null) {
                    if (setParameters.getValue(i).toString().contains("Transform(GeometryFromText"))
                        keyValues += ", " + setParameters.getValue(i) + " ";
                    else if (setParameters.getValue(i) instanceof Byte)
                        keyValues += ", X'" + setParameters.getValue(i) + "' ";
                    else
                        keyValues += ", '" + setParameters.getValue(i) + "'";
                } else {
                    keyValues += ", NULL ";
                }
            }
            keys = keys.substring(0, keys.length() - 2);
            keyValues = keyValues.substring(1, keyValues.length());

            String sql = "INSERT INTO " + tableName + " (" + keys + ") VALUES(" + keyValues + ")";

            OHibernateConfig.db.exec(sql, null);

            Log.i("OHibernate -> Info", "Table Name:" + tableName + ". Object INSERTED");

        } else {
            Log.e("OHibernate -> Error", "SetParameters Not Found !");
        }
        return this;
    }

    public OQuery InsertEntity(String tableName,Object object) throws Exception {
        try {
            this.classType = ((Class<K>) object.getClass()).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Field[] allField = classType.getClass().getDeclaredFields();
        this.tableName = tableName;
        ArrayList<String> ColumnNames = getTableColumnName(tableName);
        String keys = "";
        String keyValues = "";
        for (Field field : allField) {
            if (ColumnNames.contains(field.getName().toString())) {
                field.setAccessible(true);
                keys += field.getName().toString() + ", ";
                try {
                    Object value = field.get(object);
                    if (value != null) {
                        if (value.toString().contains("Transform(GeometryFromText"))
                            keyValues += ", " + value + " ";
                        else if (value instanceof Byte)
                            keyValues += ", X'" + value + "' ";
                        else
                            keyValues += ", '" + value + "'";
                    } else {
                        keyValues += ", NULL ";
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        keys = keys.substring(0, keys.length() - 2);
        keyValues = keyValues.substring(1, keyValues.length());

        String sql = "INSERT INTO " + tableName + " (" + keys + ") VALUES(" + keyValues + ")";

        OHibernateConfig.db.exec(sql, null);

        Log.i("OHibernate -> Info", "Table Name:" + tableName + ". Object INSERTED");

        return this;
    }

    public OQuery Update(String tableName) throws Exception {

        if (setParameters != null && setParameters.size() > 0) {
            this.tableName = tableName;

            String keyValues = "";
            for (int i = 0; i < setParameters.size(); i++) {
                if (setParameters.getValue(i) != null) {
                    if (setParameters.getValue(i).toString().contains("Transform(GeometryFromText"))
                        keyValues += ", " + setParameters.getKey(i) + "=" + setParameters.getValue(i) + " ";
                    else if (setParameters.getValue(i) instanceof Byte)
                        keyValues += ", " + setParameters.getKey(i) + "= X'" + setParameters.getValue(i) + "' ";
                    else
                        keyValues += ", " + setParameters.getKey(i) + "='" + setParameters.getValue(i) + "'";
                } else {
                    keyValues += ", " + setParameters.getKey(i) + "=NULL ";
                }
            }
            keyValues = keyValues.substring(1, keyValues.length());

            String sql = "";

            if (this.conditions.getWhereData() != null && this.conditions.getWhereData().size() > 0) {
                String wherer = UpDeWHERE("");

                sql = "UPDATE " + tableName + " SET " + keyValues + " WHERE" + wherer + " ";

                OHibernateConfig.db.exec(sql, null);

                Log.i("OHibernate -> Info", "Table Name:" + tableName + ". Object UPDATED");
            } else {
                Log.e("OHibernate -> Error", "Where Not Found !");
            }
        } else {
            Log.e("OHibernate -> Error", "SetParameters Not Found !");
        }
        return this;
    }

    public OQuery UpdateEntity(String tableName,Object object) throws Exception {
        try {
            this.classType = ((Class<K>) object.getClass()).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Field[] allField = classType.getClass().getDeclaredFields();
        this.tableName = tableName;
        ArrayList<String> ColumnNames = getTableColumnName(tableName);

        String keyValues = "";
        for (Field field : allField) {
            if (ColumnNames.contains(field.getName().toString())) {
                field.setAccessible(true);
                try {
                    Object value = field.get(object);
                    if (value != null) {
                        if (value.toString().contains("Transform(GeometryFromText"))
                            keyValues += field.getName().toString() + "=" + value + ", ";
                        else if (value instanceof Byte)
                            keyValues += field.getName().toString() + "= X'" + value + "', ";
                        else
                            keyValues += field.getName().toString() + "='" + value + "', ";
                    } else {
                        keyValues += field.getName().toString() + "=NULL, ";
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        keyValues = keyValues.substring(0, keyValues.length() - 2);

        if (this.conditions.getWhereData() != null && this.conditions.getWhereData().size() > 0) {
            String wherer = UpDeWHERE("");

            String sql = "UPDATE " + tableName + " SET " + keyValues + " WHERE" + wherer + " ";

            OHibernateConfig.db.exec(sql, null);

            Log.i("OHibernate -> Info", "Table Name:" + tableName + ". Object UPDATED");
        } else {
            Log.e("OHibernate -> Error", "Where Not Found !");
        }

        return this;
    }

    public OQuery Delete(String tableName) throws Exception {

        if (this.conditions.getWhereData() != null && this.conditions.getWhereData().size() > 0) {
            String wherer = UpDeWHERE("");

            String sql = "DELETE FROM " + tableName + " WHERE" + wherer + " ";

            OHibernateConfig.db.exec(sql, null);

            Log.i("OHibernate -> Info", "Table Name:" + tableName + ". Object DELETED");
        } else {
            Log.e("OHibernate -> Error", "Where Not Found !");
        }
        return this;
    }

    public OQuery DeleteAll(String tableName) throws Exception {

        String sql = "DELETE FROM " + tableName + " ";

        OHibernateConfig.db.exec(sql, null);

        Log.i("OHibernate -> Info", "Table Name:" + tableName + ". All Objects DELETED");

        return this;
    }

    public String getObjectId() {
        try {
            return getLastID(tableName);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", "getLastID() -> " + e.getMessage());
        }
        return "";
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    private String selectQueryString() throws Exception {
        String sqlQuery = "SELECT ";
        for (String selectData : selectDatas) {
            sqlQuery += selectData + ", ";
        }
        sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 2);
        sqlQuery += " FROM ";
        for (String fromData : fromDatas) {
            sqlQuery += fromData + ", ";
        }
        sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 2);

        sqlQuery = queryWLOLDAO(sqlQuery);

        sqlQuery += ";";

        String result = "";

        Stmt stmt = OHibernateConfig.db.prepare(sqlQuery);
        while (stmt.step()) {

            for (int i = 0; i < stmt.column_count(); i++)
                result += stmt.column(i).toString() + ", ";
            result = result.substring(0, result.length() - 2);
            return result;
        }
        stmt.close();

        return result;

    }

    private ArrayList<String> selectQueryStrings() throws Exception {
        String sqlQuery = "SELECT ";
        for (String selectData : selectDatas) {
            sqlQuery += selectData + ", ";
        }
        sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 2);
        sqlQuery += " FROM ";
        for (String fromData : fromDatas) {
            sqlQuery += fromData + ", ";
        }
        sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 2);

        sqlQuery = queryWLOLDAO(sqlQuery);

        sqlQuery += ";";

        ArrayList<String> result = new ArrayList<>();

        Stmt stmt = OHibernateConfig.db.prepare(sqlQuery);
        while (stmt.step()) {

            String res = "";
            for (int i = 0; i < stmt.column_count(); i++)
                res += stmt.column(i).toString() + ", ";
            res = res.substring(0, res.length() - 2);
            result.add(res);
        }
        stmt.close();

        return result;

    }

    private K selectQueryObject() throws Exception, IllegalAccessException {
        String sqlQuery = "SELECT ";
        for (String selectData : selectDatas) {
            sqlQuery += selectData + ", ";
        }
        sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 2);
        sqlQuery += " FROM ";
        for (String fromData : fromDatas) {
            sqlQuery += fromData + ", ";
        }
        sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 2);
        sqlQuery += ";";

        ArrayList<String> columNames = getTableColumnName();
        Stmt stmt = OHibernateConfig.db.prepare(sqlQuery);
        while (stmt.step()) {
            Field[] allField = classType.getClass().getDeclaredFields();
            for (Field field : allField) {
                if (selectDatas.size() == 1 && selectDatas.get(0).equals("*")) {
                    for (int i = 0; i < columNames.size(); i++)
                        if (field.getName().toString().equals(columNames.get(i))) {
                            field.setAccessible(true);
                            if (stmt.column(i) != null && stmt.column(i).toString() != null &&
                                    !stmt.column(i).toString().equals("NULL") && !stmt.column(i).toString().equals("null")) {
                                if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                                    field.set(classType, Integer.parseInt(stmt.column(i).toString()));
                                } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                                    field.set(classType, Double.parseDouble(stmt.column(i).toString()));
                                } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                                    field.set(classType, Float.parseFloat(stmt.column(i).toString()));
                                } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                                    field.set(classType, Long.parseLong(stmt.column(i).toString()));
                                } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                                    field.set(classType, Boolean.parseBoolean(stmt.column(i).toString()));
                                } else if (field.getType().equals(byte[].class) || field.getType().equals(Byte[].class)) {
                                    field.set(classType, stmt.column(i));
                                } else
                                    field.set(classType, stmt.column(i).toString());
                            }

                        }
                } else if (selectDatas.size() == 1 && selectDatas.get(0).contains(",")) {
                    String[] datas = selectDatas.get(0).split(",");
                    for (int i = 0; i < datas.length; i++)
                        if (field.getName().toString().equals(datas[i])) {
                            field.setAccessible(true);
                            if (stmt.column(i) != null && stmt.column(i).toString() != null &&
                                    !stmt.column(i).toString().equals("NULL") && !stmt.column(i).toString().equals("null")) {
                                if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                                    field.set(classType, Integer.parseInt(stmt.column(i).toString()));
                                } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                                    field.set(classType, Double.parseDouble(stmt.column(i).toString()));
                                } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                                    field.set(classType, Float.parseFloat(stmt.column(i).toString()));
                                } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                                    field.set(classType, Long.parseLong(stmt.column(i).toString()));
                                } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                                    field.set(classType, Boolean.parseBoolean(stmt.column(i).toString()));
                                } else if (field.getType().equals(byte[].class) || field.getType().equals(Byte[].class)) {
                                    field.set(classType, stmt.column(i));
                                } else
                                    field.set(classType, stmt.column(i).toString());
                            }
                        }
                } else {
                    for (int i = 0; i < selectDatas.size(); i++)
                        if (field.getName().toString().equals(selectDatas.get(i))) {
                            field.setAccessible(true);
                            if (stmt.column(i) != null && stmt.column(i).toString() != null &&
                                    !stmt.column(i).toString().equals("NULL") && !stmt.column(i).toString().equals("null")) {
                                if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                                    field.set(classType, Integer.parseInt(stmt.column(i).toString()));
                                } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                                    field.set(classType, Double.parseDouble(stmt.column(i).toString()));
                                } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                                    field.set(classType, Float.parseFloat(stmt.column(i).toString()));
                                } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                                    field.set(classType, Long.parseLong(stmt.column(i).toString()));
                                } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                                    field.set(classType, Boolean.parseBoolean(stmt.column(i).toString()));
                                } else if (field.getType().equals(byte[].class) || field.getType().equals(Byte[].class)) {
                                    field.set(classType, stmt.column(i));
                                } else
                                    field.set(classType, stmt.column(i).toString());
                            }

                        }
                }
            }

        }
        stmt.close();

        return classType;
    }

    private ArrayList<K> selectQueryObjects() throws Exception, IllegalAccessException {
        String sqlQuery = "SELECT ";

        //->DISTINCT
        if (conditions.isDistinct())
            sqlQuery += "DISTINCT ";
        //->DISTINCT ###

        for (String selectData : selectDatas) {
            sqlQuery += selectData + ", ";
        }
        sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 2);
        sqlQuery += " FROM ";
        for (String fromData : fromDatas) {
            sqlQuery += fromData + ", ";
        }
        sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 2);

        sqlQuery = queryWLOLDAO(sqlQuery);

        sqlQuery += ";";

        ArrayList result = new ArrayList<>();
        ArrayList<String> columNames = getTableColumnName();
        Stmt stmt = OHibernateConfig.db.prepare(sqlQuery);
        while (stmt.step()) {
            Field[] allField = classType.getClass().getDeclaredFields();
            for (Field field : allField) {
                if (selectDatas.size() == 1 && selectDatas.get(0).equals("*")) {
                    for (int i = 0; i < columNames.size(); i++)
                        if (field.getName().toString().equals(columNames.get(i))) {
                            field.setAccessible(true);
                            if (stmt.column(i) != null && stmt.column(i).toString() != null &&
                                    !stmt.column(i).toString().equals("NULL") && !stmt.column(i).toString().equals("null")) {
                                if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                                    field.set(classType, Integer.parseInt(stmt.column(i).toString()));
                                } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                                    field.set(classType, Double.parseDouble(stmt.column(i).toString()));
                                } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                                    field.set(classType, Float.parseFloat(stmt.column(i).toString()));
                                } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                                    field.set(classType, Long.parseLong(stmt.column(i).toString()));
                                } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                                    field.set(classType, Boolean.parseBoolean(stmt.column(i).toString()));
                                } else if (field.getType().equals(byte[].class) || field.getType().equals(Byte[].class)) {
                                    field.set(classType, stmt.column(i));
                                } else
                                    field.set(classType, stmt.column(i).toString());
                            }

                        }
                } else if (selectDatas.size() == 1 && selectDatas.get(0).contains(",")) {
                    String[] datas = selectDatas.get(0).split(",");
                    for (int i = 0; i < datas.length; i++)
                        if (field.getName().toString().equals(datas[i])) {
                            field.setAccessible(true);
                            if (stmt.column(i) != null && stmt.column(i).toString() != null &&
                                    !stmt.column(i).toString().equals("NULL") && !stmt.column(i).toString().equals("null")) {
                                if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                                    field.set(classType, Integer.parseInt(stmt.column(i).toString()));
                                } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                                    field.set(classType, Double.parseDouble(stmt.column(i).toString()));
                                } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                                    field.set(classType, Float.parseFloat(stmt.column(i).toString()));
                                } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                                    field.set(classType, Long.parseLong(stmt.column(i).toString()));
                                } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                                    field.set(classType, Boolean.parseBoolean(stmt.column(i).toString()));
                                } else if (field.getType().equals(byte[].class) || field.getType().equals(Byte[].class)) {
                                    field.set(classType, stmt.column(i));
                                } else
                                    field.set(classType, stmt.column(i).toString());
                            }
                        }
                } else {
                    for (int i = 0; i < selectDatas.size(); i++)
                        if (field.getName().toString().equals(selectDatas.get(i))) {
                            field.setAccessible(true);
                            if (stmt.column(i) != null && stmt.column(i).toString() != null &&
                                    !stmt.column(i).toString().equals("NULL") && !stmt.column(i).toString().equals("null")) {
                                if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                                    field.set(classType, Integer.parseInt(stmt.column(i).toString()));
                                } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                                    field.set(classType, Double.parseDouble(stmt.column(i).toString()));
                                } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                                    field.set(classType, Float.parseFloat(stmt.column(i).toString()));
                                } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                                    field.set(classType, Long.parseLong(stmt.column(i).toString()));
                                } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                                    field.set(classType, Boolean.parseBoolean(stmt.column(i).toString()));
                                } else if (field.getType().equals(byte[].class) || field.getType().equals(Byte[].class)) {
                                    field.set(classType, stmt.column(i));
                                } else
                                    field.set(classType, stmt.column(i).toString());
                            }

                        }
                }
            }
            Object objClone = cloneObject(classType);
            result.add(objClone);

        }
        stmt.close();

        return result;
    }

    private ArrayList<String> getTableColumnName() {
        ArrayList<String> tableColumnNames = new ArrayList<>();
        String sql = "";
        if (fromDatas != null && fromDatas.size() > 0)
            sql = "PRAGMA table_info ('" + fromDatas.get(0) + "')";

        Stmt stmt = null;
        try {
            stmt = OHibernateConfig.db.prepare(sql);
            while (stmt.step()) {
                tableColumnNames.add(stmt.column(1).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableColumnNames;
    }

    private ArrayList<String> getTableColumnName(String tableName) {
        ArrayList<String> tableColumnNames = new ArrayList<>();
        String sql = "";
        if (tableName != null)
            sql = "PRAGMA table_info ('" + tableName + "')";

        Stmt stmt = null;
        try {
            stmt = OHibernateConfig.db.prepare(sql);
            while (stmt.step()) {
                tableColumnNames.add(stmt.column(1).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableColumnNames;
    }

    private Object cloneObject(Object obj) {
        Object clone = null;
        try {
            clone = obj.getClass().newInstance();
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                field.set(clone, field.get(obj));
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return clone;
    }

    private String UpDeWHERE(String wherer) {
        String connector = "and"; // And Or bağlacı eksikse Otomatik olarak And Koyar
        if (conditions.getAndConnector() > 0) {
            connector = "and";
        } else if (conditions.getOrConnector() > 0)
            connector = "or";

        for (int i = 0; i < this.conditions.getWhereData().getKeysArrayList().size(); i++) {
            if (conditions.getLike().get(i) == LikeType.FRONT)
                wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + " like '%" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' " + connector;
            else if (conditions.getLike().get(i) == LikeType.BEHIND)
                wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + " like '" + this.conditions.getWhereData().getValuesArrayList().get(i) + "%' " + connector;
            else if (conditions.getLike().get(i) == LikeType.BOTH)
                wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + " like '%" + this.conditions.getWhereData().getValuesArrayList().get(i) + "%' " + connector;
            else if (conditions.getCondition().get(i) == ConditionType.MAX)
                wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + " > '" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' " + connector;
            else if (conditions.getCondition().get(i) == ConditionType.MIN)
                wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + " < '" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' " + connector;
            else if (conditions.getCondition().get(i) == ConditionType.EQUAL)
                wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + " == '" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' " + connector;
            else if (conditions.getCondition().get(i) == ConditionType.NOT_EQUAL)
                wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + " <> '" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' " + connector;
            else if (conditions.getCondition().get(i) == ConditionType.MIN_EQUAL)
                wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + " <= '" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' " + connector;
            else if (conditions.getCondition().get(i) == ConditionType.MAX_EQUAL)
                wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + " >= '" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' " + connector;
            else if (conditions.getCondition().get(i) == ConditionType.IS)
                wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + " IS '" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' " + connector;
            else if (conditions.getCondition().get(i) == ConditionType.IS_NOT)
                wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + " IS NOT '" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' " + connector;
            else
                wherer += " " + this.conditions.getWhereData().getKeysArrayList().get(i) + "='" + this.conditions.getWhereData().getValuesArrayList().get(i) + "' " + connector;
        }
        if (connector.equals("and")) {
            wherer = wherer.substring(0, wherer.length() - 3);
        } else if (connector.equals("or"))
            wherer = wherer.substring(0, wherer.length() - 2);

        if (conditions.getOrderbyData() != null && conditions.getOrderbyData().size() > 0) {
            wherer += " ORDER BY " + conditions.getOrderbyData().getKey(0) + " " + conditions.getOrderbyData().getValue(0) + " ";
        }
        if (conditions.getLimit() != null)
            wherer += " LIMIT " + conditions.getLimit() + " ";

        return wherer;
    }

    private String queryWLOLDAO(String sqlQuery) {
        //->AND, OR, WHERE, LIKE, ORDERBY, LIMIT
        String connector = "and";
        if (conditions.getAndConnector() > 0) {
            connector = "and";
        } else if (conditions.getOrConnector() > 0)
            connector = "or";
        if (this.conditions.getWhereData() != null && this.conditions.getWhereData().size() > 0) {
            sqlQuery += " WHERE ";
            for (int i = 0; i < conditions.getWhereData().size(); i++) {
                if (conditions.getLike().get(i) == LikeType.FRONT)
                    sqlQuery += " " + conditions.getWhereData().getKey(i) + " like '%" + conditions.getWhereData().getValue(i) + "' " + connector;
                else if (conditions.getLike().get(i) == LikeType.BEHIND)
                    sqlQuery += " " + conditions.getWhereData().getKey(i) + " like '" + conditions.getWhereData().getValue(i) + "%' " + connector;
                else if (conditions.getLike().get(i) == LikeType.BOTH)
                    sqlQuery += " " + conditions.getWhereData().getKey(i) + " like '%" + conditions.getWhereData().getValue(i) + "%' " + connector;
                else if (conditions.getCondition().get(i) == ConditionType.MAX)
                    sqlQuery += " " + conditions.getWhereData().getKey(i) + " > '" + conditions.getWhereData().getValue(i) + "' " + connector;
                else if (conditions.getCondition().get(i) == ConditionType.MIN)
                    sqlQuery += " " + conditions.getWhereData().getKey(i) + " < '" + conditions.getWhereData().getValue(i) + "' " + connector;
                else if (conditions.getCondition().get(i) == ConditionType.EQUAL)
                    sqlQuery += " " + conditions.getWhereData().getKey(i) + " == '" + conditions.getWhereData().getValue(i) + "' " + connector;
                else if (conditions.getCondition().get(i) == ConditionType.NOT_EQUAL)
                    sqlQuery += " " + conditions.getWhereData().getKey(i) + " <> '" + conditions.getWhereData().getValue(i) + "' " + connector;
                else if (conditions.getCondition().get(i) == ConditionType.MIN_EQUAL)
                    sqlQuery += " " + conditions.getWhereData().getKey(i) + " <= '" + conditions.getWhereData().getValue(i) + "' " + connector;
                else if (conditions.getCondition().get(i) == ConditionType.MAX_EQUAL)
                    sqlQuery += " " + conditions.getWhereData().getKey(i) + " >= '" + conditions.getWhereData().getValue(i) + "' " + connector;
                else if (conditions.getCondition().get(i) == ConditionType.IS)
                    sqlQuery += " " + conditions.getWhereData().getKey(i) + " IS '" + conditions.getWhereData().getValue(i) + "' " + connector;
                else if (conditions.getCondition().get(i) == ConditionType.IS_NOT)
                    sqlQuery += " " + conditions.getWhereData().getKey(i) + " IS NOT '" + conditions.getWhereData().getValue(i) + "' " + connector;
                else {
                    sqlQuery += " " + conditions.getWhereData().getKey(i) + "='" + conditions.getWhereData().getValue(i) + "' " + connector;
                }
            }
            if (connector.equals("and")) {
                sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 4);
            } else if (connector.equals("or"))
                sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 3);
        }

        if (conditions.getOrderbyData() != null && conditions.getOrderbyData().size() > 0) {
            sqlQuery += " ORDER BY " + conditions.getOrderbyData().getKey(0) + " " + conditions.getOrderbyData().getValue(0) + "";
        }

        if (conditions.getLimit() != null)
            sqlQuery += " LIMIT " + conditions.getLimit() + "";
        //->AND, OR, WHERE, LIKE, ORDERBY, LIMIT ###
        return sqlQuery;
    }

    private String getLastID(String tableName) throws Exception {
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
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public OQuery Where(String key, Object value) {
        this.conditions.getWhereData().add(key, value);
        this.conditions.getLike().add(null);
        this.conditions.getCondition().add(null);
        return this;
    }

    public OQuery Where(String key, Object value, LikeType like) {
        this.conditions.getWhereData().add(key, value);
        this.conditions.getLike().add(like);
        this.conditions.getCondition().add(null);
        return this;
    }

    public OQuery Where(String key, Object value, ConditionType condition) {
        this.conditions.getWhereData().add(key, value);
        this.conditions.getLike().add(null);
        this.conditions.getCondition().add(condition);
        return this;
    }

    public OQuery And() {
        this.conditions.setAndConnector(this.conditions.getAndConnector() + 1);
        return this;
    }

    public OQuery Or() {
        this.conditions.setOrConnector(this.conditions.getOrConnector() + 1);
        return this;
    }

    public OQuery Distinct() {
        this.conditions.setDistinct(true);
        return this;
    }

    public OQuery OrderBy(String key, OrderByType order_by_type) {
        this.conditions.getOrderbyData().add(key, order_by_type);
        return this;
    }

    public OQuery Limit(Integer limit) {
        this.conditions.setLimit(limit);
        return this;
    }

    public OQuery SetParameter(String key, Object value) {
        setParameters.add(key, value);
        return this;
    }

    public OQuery IdName(String idName) {
        this.id_fieldName = idName;
        return this;
    }

    /**
     * O anki zamanı verir
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public String getNowDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTimeStamp = dateFormat.format(new Date());

        return currentTimeStamp;
    }
}
