package com.onurciner;

import android.util.Log;

import com.nutiteq.geometry.Geometry;
import com.nutiteq.utils.Utils;
import com.nutiteq.utils.WkbRead;
import com.onurciner.enums.ConditionType;
import com.onurciner.enums.LikeType;
import com.onurciner.ohibernate.Blob;
import com.onurciner.ohibernate.Column;
import com.onurciner.ohibernate.GeometryColumn;
import com.onurciner.ohibernate.Id;
import com.onurciner.ohibernatetools.Conditions;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import jsqlite.Exception;
import jsqlite.Stmt;

/**
 * Created by Onur.Ciner on 30.11.2016.
 */

public class Selections<K> {

    private K classType;
    private Class<K> clazzOfT;

    public Selections(K classType) {
        this.classType = classType;
    }

    protected K select(ArrayList<String> fields, Conditions conditions, String tableName, String key, Object value) throws Exception {

        ArrayList<String> tempFields = getSelectTempFields(fields);

        String keye = "";
        for (String field : tempFields) {
            keye += ", " + field + "";
        }
        String keys = keye.substring(2, keye.length());

        //----->
        String sql = "SELECT";
        if (conditions.isDistinct())
            sql += " DISTINCT";
        String connector = "";
        if (conditions.getAndConnector() > 0) {
            connector = " and ";
        } else if (conditions.getOrConnector() > 0)
            connector = " or ";
        if (conditions.getLike() != null && conditions.getLike().size() > 0) {
            if (conditions.getWhereData() != null && conditions.getWhereData().size() > 0) {
                sql += " " + keys + " FROM " + tableName + " WHERE ";
                for (int i = 0; i < conditions.getWhereData().size(); i++) {
                    if (conditions.getLike().get(i) == LikeType.FRONT)
                        sql += conditions.getWhereData().getKey(i) + " like '%" + conditions.getWhereData().getValue(i) + "'" + connector;
                    else if (conditions.getLike().get(i) == LikeType.BEHIND)
                        sql += conditions.getWhereData().getKey(i) + " like '" + conditions.getWhereData().getValue(i) + "%' " + connector;
                    else if (conditions.getLike().get(i) == LikeType.BOTH)
                        sql += conditions.getWhereData().getKey(i) + " like '%" + conditions.getWhereData().getValue(i) + "%' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.MAX)
                        sql += conditions.getWhereData().getKey(i) + " > '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.MIN)
                        sql += conditions.getWhereData().getKey(i) + " < '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.EQUAL)
                        sql += conditions.getWhereData().getKey(i) + " == '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.NOT_EQUAL)
                        sql += conditions.getWhereData().getKey(i) + " <> '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.MIN_EQUAL)
                        sql += conditions.getWhereData().getKey(i) + " <= '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.MAX_EQUAL)
                        sql += conditions.getWhereData().getKey(i) + " >= '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.IS)
                        sql += conditions.getWhereData().getKey(i) + " IS '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.IS_NOT)
                        sql += conditions.getWhereData().getKey(i) + " IS NOT '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else {
                        sql += conditions.getWhereData().getKey(i) + "='" + conditions.getWhereData().getValue(i) + "' " + connector;
                    }
                }
                if (conditions.getAndConnector() > 0) {
                    sql = sql.substring(0, sql.length() - 4);
                } else if (conditions.getOrConnector() > 0)
                    sql = sql.substring(0, sql.length() - 3);
            } else {
                if (key == null)
                    sql += " " + keys + " FROM " + tableName + " ";
                else
                    sql += " " + keys + " FROM " + tableName + " WHERE " + key + " = '" + value + "' ";
            }
        } else {
            if (conditions.getWhereData() != null && conditions.getWhereData().size() > 0) {
                sql += " " + keys + " FROM " + tableName + " WHERE ";
                for (int i = 0; i < conditions.getWhereData().size(); i++) {
                    sql += conditions.getWhereData().getKey(i) + "='" + conditions.getWhereData().getValue(i) + "' " + connector;
                }
                if (conditions.getAndConnector() > 0) {
                    sql = sql.substring(0, sql.length() - 4);
                } else if (conditions.getOrConnector() > 0)
                    sql = sql.substring(0, sql.length() - 3);
            } else {
                if (key == null)
                    sql += " " + keys + " FROM " + tableName + " ";
                else
                    sql += " " + keys + " FROM " + tableName + " WHERE " + key + " = '" + value + "' ";
            }
        }
        //-----> ##################################

        if (conditions.getOrderbyData() != null && conditions.getOrderbyData().size() > 0) {
            sql += " ORDER BY " + conditions.getOrderbyData().getKey(0) + " " + conditions.getOrderbyData().getValue(0) + "";
        }

        Stmt stmt = OHibernateConfig.db.prepare(sql);
        while (stmt.step()) {
            K source = getInstance();

            for (int i = 0; i < fields.size(); i++) {
                try {
                    if (!tempFields.get(i).contains("HEX")) {

                        Field field = source.getClass().getDeclaredField(fields.get(i));
                        field.setAccessible(true);
                        if (stmt.column(i) != null && stmt.column(i).toString() != null &&
                                !stmt.column(i).toString().equals("NULL") && !stmt.column(i).toString().equals("null")) {
                            if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                                field.set(source, Integer.parseInt(stmt.column(i).toString()));
                            } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                                field.set(source, Double.parseDouble(stmt.column(i).toString()));
                            } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                                field.set(source, Float.parseFloat(stmt.column(i).toString()));
                            } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                                field.set(source, Long.parseLong(stmt.column(i).toString()));
                            } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                                field.set(source, Boolean.parseBoolean(stmt.column(i).toString()));
                            } else if (field.getType().equals(byte[].class) || field.getType().equals(Byte[].class)) {
                                field.set(source, stmt.column(i));
                            } else
                                field.set(source, stmt.column(i).toString());
                        }
                    } else {
                        Geometry[] geometries = WkbRead.readWkb(new ByteArrayInputStream(Utils.hexStringToByteArray(stmt.column(i).toString())), null);
                        Field field = source.getClass().getDeclaredField(fields.get(i));
                        field.setAccessible(true);
                        if (geometries != null && geometries[0] != null)
                            field.set(source, geometries[0]);

                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    Log.e("OHibernate -> Error", e.getMessage());
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    Log.e("OHibernate -> Error", e.getMessage());
                }
            }

            try {
                new RelationalTableOperations().selectRelationalTableOperations(source);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                Log.e("OHibernate -> Error", e.getMessage());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Log.e("OHibernate -> Error", e.getMessage());
            }

            return source;
        }
        stmt.close();

        return null;
    }

    protected ArrayList<K> selectAll(ArrayList<String> fields, Conditions conditions, String tableName, String key, Object value) throws Exception {

        ArrayList<String> tempFields = getSelectTempFields(fields);

        String keye = "";
        for (String field : tempFields) {
            keye += ", " + field + "";
        }
        String keys = keye.substring(2, keye.length());


        String sql = "SELECT";

        if (conditions.isDistinct())
            sql += " DISTINCT";

        String connector = "";
        if (conditions.getAndConnector() > 0) {
            connector = " and ";
        } else if (conditions.getOrConnector() > 0)
            connector = " or ";

        if (conditions.getLike() != null && conditions.getLike().size() > 0) {
            if (conditions.getWhereData() != null && conditions.getWhereData().size() > 0) {
                sql += " " + keys + " FROM " + tableName + " WHERE ";
                for (int i = 0; i < conditions.getWhereData().size(); i++) {
                    if (conditions.getLike().get(i) == LikeType.FRONT)
                        sql += conditions.getWhereData().getKey(i) + " like '%" + conditions.getWhereData().getValue(i) + "'" + connector;
                    else if (conditions.getLike().get(i) == LikeType.BEHIND)
                        sql += conditions.getWhereData().getKey(i) + " like '" + conditions.getWhereData().getValue(i) + "%' " + connector;
                    else if (conditions.getLike().get(i) == LikeType.BOTH)
                        sql += conditions.getWhereData().getKey(i) + " like '%" + conditions.getWhereData().getValue(i) + "%' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.MAX)
                        sql += conditions.getWhereData().getKey(i) + " > '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.MIN)
                        sql += conditions.getWhereData().getKey(i) + " < '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.EQUAL)
                        sql += conditions.getWhereData().getKey(i) + " == '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.NOT_EQUAL)
                        sql += conditions.getWhereData().getKey(i) + " <> '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.MIN_EQUAL)
                        sql += conditions.getWhereData().getKey(i) + " <= '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.MAX_EQUAL)
                        sql += conditions.getWhereData().getKey(i) + " >= '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.IS)
                        sql += conditions.getWhereData().getKey(i) + " IS '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else if (conditions.getCondition().get(i) == ConditionType.IS_NOT)
                        sql += conditions.getWhereData().getKey(i) + " IS NOT '" + conditions.getWhereData().getValue(i) + "' " + connector;
                    else {
                        sql += conditions.getWhereData().getKey(i) + "='" + conditions.getWhereData().getValue(i) + "' " + connector;
                    }
                }
                if (conditions.getAndConnector() > 0) {
                    sql = sql.substring(0, sql.length() - 4);
                } else if (conditions.getOrConnector() > 0)
                    sql = sql.substring(0, sql.length() - 3);
            } else {
                if (key == null)
                    sql += " " + keys + " FROM " + tableName + " ";
                else
                    sql += " " + keys + " FROM " + tableName + " WHERE " + key + " = '" + value + "' ";
            }
        } else {
            if (conditions.getWhereData() != null && conditions.getWhereData().size() > 0) {
                sql += " " + keys + " FROM " + tableName + " WHERE ";
                for (int i = 0; i < conditions.getWhereData().size(); i++) {
                    sql += conditions.getWhereData().getKey(i) + "='" + conditions.getWhereData().getValue(i) + "' " + connector;
                }
                if (conditions.getAndConnector() > 0) {
                    sql = sql.substring(0, sql.length() - 4);
                } else if (conditions.getOrConnector() > 0)
                    sql = sql.substring(0, sql.length() - 3);
            } else {
                if (key == null)
                    sql += " " + keys + " FROM " + tableName + " ";
                else
                    sql += " " + keys + " FROM " + tableName + " WHERE " + key + " = '" + value + "' ";
            }
        }

        if (conditions.getOrderbyData() != null && conditions.getOrderbyData().size() > 0) {
            sql += " ORDER BY " + conditions.getOrderbyData().getKey(0) + " " + conditions.getOrderbyData().getValue(0) + "";
        }

        if (conditions.getLimit() != null)
            sql += " LIMIT " + conditions.getLimit() + "";

        ArrayList<K> sources = new ArrayList<K>();


        Stmt stmt = OHibernateConfig.db.prepare(sql);
        while (stmt.step()) {

            K source = getInstance();
            for (int i = 0; i < fields.size(); i++) {
                try {
                    if (!tempFields.get(i).contains("HEX")) {
                        Field field = source.getClass().getDeclaredField(fields.get(i));
                        field.setAccessible(true);
                        if (stmt.column(i) != null && stmt.column(i).toString() != null &&
                                !stmt.column(i).toString().equals("NULL") && !stmt.column(i).toString().equals("null")) {
                            if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                                field.set(source, Integer.parseInt(stmt.column(i).toString()));
                            } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                                field.set(source, Double.parseDouble(stmt.column(i).toString()));
                            } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                                field.set(source, Float.parseFloat(stmt.column(i).toString()));
                            } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                                field.set(source, Long.parseLong(stmt.column(i).toString()));
                            } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                                field.set(source, Boolean.parseBoolean(stmt.column(i).toString()));
                            } else if (field.getType().equals(byte[].class) || field.getType().equals(Byte[].class)) {
                                field.set(source, stmt.column(i));
                            } else
                                field.set(source, stmt.column(i).toString());
                        }
                    } else {
                        Geometry[] geometries = WkbRead.readWkb(new ByteArrayInputStream(Utils.hexStringToByteArray(stmt.column(i).toString())), null);
                        Field field = source.getClass().getDeclaredField(fields.get(i));
                        field.setAccessible(true);
                        if (geometries != null && geometries[0] != null)
                            field.set(source, geometries[0]);
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    Log.e("OHibernate -> Error", e.getMessage());
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    Log.e("OHibernate -> Error", e.getMessage());
                }
            }
            try {
                new RelationalTableOperations().selectRelationalTableOperations(source);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                Log.e("OHibernate -> Error", e.getMessage());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Log.e("OHibernate -> Error", e.getMessage());
            }
            sources.add(source);
        }
        stmt.close();

        return sources;
    }

    private K getInstance() {
        try {
            clazzOfT = (Class<K>) classType.getClass();
            return clazzOfT.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }
        return null;
    }

    private ArrayList<String> getSelectTempFields(ArrayList<String> fields) {
        ArrayList<String> tempFields = (ArrayList<String>) fields.clone();

        Field[] allFields = classType.getClass().getDeclaredFields();
        for (Field field33 : allFields) {
            if (field33.isAnnotationPresent(Column.class)) {
                Column test = field33.getAnnotation(Column.class);
                if (!test.NAME().equals("")) {
                    if (tempFields.contains(test.NAME())) {
                        for (int a = 0; a < tempFields.size(); a++) {
                            if (tempFields.get(a).equals(test.NAME())) {

                                tempFields.remove(a);
                                tempFields.add(a, test.NAME() + " as " + field33.getName());

                                fields.remove(a);
                                fields.add(a, field33.getName());
                            }
                        }
                    }
                }
            }
            if (field33.isAnnotationPresent(Blob.class)) {
                Blob test = field33.getAnnotation(Blob.class);
                if (!test.NAME().equals("")) {
                    if (tempFields.contains(test.NAME())) {
                        for (int a = 0; a < tempFields.size(); a++) {
                            if (tempFields.get(a).equals(test.NAME())) {

                                tempFields.remove(a);
                                tempFields.add(a, test.NAME() + " as " + field33.getName());

                                fields.remove(a);
                                fields.add(a, field33.getName());
                            }
                        }
                    }
                }
            }
            if (field33.isAnnotationPresent(Id.class)) {
                Id test = field33.getAnnotation(Id.class);
                if (!test.NAME().equals("")) {
                    if (tempFields.contains(test.NAME())) {
                        for (int a = 0; a < tempFields.size(); a++) {
                            if (tempFields.get(a).equals(test.NAME())) {

                                tempFields.remove(a);
                                tempFields.add(a, test.NAME() + " as " + field33.getName());

                                fields.remove(a);
                                fields.add(a, field33.getName());
                            }
                        }
                    }
                }
            }
            if (field33.isAnnotationPresent(GeometryColumn.class)) {
                GeometryColumn test = field33.getAnnotation(GeometryColumn.class);
                if (!test.NAME().equals("")) {
                    if (tempFields.contains(test.NAME())) {
                        for (int a = 0; a < tempFields.size(); a++) {
                            if (tempFields.get(a).equals(test.NAME())) {

                                tempFields.remove(a);
                                tempFields.add(a, " HEX(AsBinary(Transform(" + test.NAME() + ",3857)))" + " as " + field33.getName());

                                fields.remove(a);
                                fields.add(a, field33.getName());
                            }
                        }
                    }
                } else {
                    if (tempFields.contains(field33.getName())) {
                        for (int a = 0; a < tempFields.size(); a++) {
                            if (tempFields.get(a).equals(field33.getName())) {

                                tempFields.remove(a);
                                tempFields.add(a, " HEX(AsBinary(Transform(" + field33.getName() + ",3857)))" + " as " + field33.getName());

                                fields.remove(a);
                                fields.add(a, field33.getName());
                            }
                        }
                    }
                }
            }

        }
        return tempFields;
    }
}
