package com.onurciner;

import android.util.Log;

import com.nutiteq.geometry.Geometry;
import com.nutiteq.geometry.Line;
import com.nutiteq.geometry.Point;
import com.nutiteq.geometry.Polygon;
import com.nutiteq.utils.Utils;
import com.nutiteq.utils.WkbRead;
import com.nutiteq.utils.WktWriter;
import com.onurciner.enums.LIKE_TYPE;
import com.onurciner.enums.ORDER_BY_TYPE;
import com.onurciner.ohibernate.Blob;
import com.onurciner.ohibernate.Column;
import com.onurciner.ohibernate.Entity;
import com.onurciner.ohibernate.GeometryColumn;
import com.onurciner.ohibernate.Id;
import com.onurciner.ohibernate.NonColumn;
import com.onurciner.ohibernatetools.OHash;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jsqlite.Exception;
import jsqlite.Stmt;

/**
 * Created by Onur.Ciner on 7.11.2016.
 * VERSION 1.0.1
 * ### LOG 1 - 22.11.2016 ###
 * -Birden fazla where koşulu desteği geldi. Where koşullarını bağlamak için and ve or bağlaçları getirildi.
 * -Like sistemi değişti. Where metodunun içerisine 3. parametre olarak like koşulu verilebilmektedir.
 * -Select, Update ve Delete komutları için birden fazla where koşulu kullanılabilir.
 * -Distinct özelliği eklendi.
 * -OrderBy özelliği eklendi.
 * -Persist özelliği eklendi.
 */

public class OHibernate<K> {

    private K classType;

    public OHibernate setObj(K classType) {
        this.classType = classType;
        return this;
    }

    public OHibernate(Class<K> kClass) {
        try {
            this.classType = kClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        new OHibernateConfig().getConfig();
    }

    private Transactions transactions = new Process();

    private String tableName = "";

    private ArrayList<String> fields = new ArrayList<>();
    private ArrayList<String> fieldsValues = new ArrayList<>();
    private ArrayList<String> fieldsType = new ArrayList<>();
    private ArrayList<String> fieldsUnique = new ArrayList<>();

    private String id_fieldName = "";
    private String id_fieldType = "";

    private ArrayList<String> GeoColumnNames = new ArrayList<>();
    private ArrayList<Integer> GeoColumnSRids = new ArrayList<>();
    private ArrayList<GeometryColumn.GEO_TYPE> GeoColumnTypes = new ArrayList<>();

    private boolean idPrimeryKey = false;

    private void engine(boolean idStatus, boolean idRemove) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        if (classType.getClass().isAnnotationPresent(Entity.class)) {

            Field[] all = classType.getClass().getDeclaredFields();
            for (Field field : all) {
                if (field.isAnnotationPresent(Id.class)) {
                    Id test = field.getAnnotation(Id.class);
                    if (test.NAME().equals("")) {
                        id_fieldName = field.getName();
                    } else {
                        id_fieldName = test.NAME();
                    }
                    //----
                    String typer = field.getType().getName().toString();
                    if (typer.contains(".")) {
                        String[] types = typer.split("\\.");
                        String type = types[types.length - 1];
                        id_fieldType = type;
                    } else {
                        id_fieldType = field.getType().getName().toString();
                    }
                    //----

                    if (test.PRIMARY_KEY_AUTOINCREMENT())
                        idPrimeryKey = true;
                }
            }

            Entity oent = classType.getClass().getAnnotation(Entity.class);
            if (!oent.TABLE_NAME().equals("")) {
                tableName = oent.TABLE_NAME();
            } else {
                tableName = classType.getClass().getSimpleName().toLowerCase().toString();
            }

            fields.clear();
            fieldsValues.clear();
            fieldsType.clear();

            Field[] allFields = classType.getClass().getDeclaredFields();
            for (Field field : allFields) {
                if (!field.getName().equals("serialVersionUID")) {
                    if (field.getType().equals(String.class) || field.getType().equals(Integer.class) || field.getType().equals(Long.class) || field.getType().equals(Double.class) || field.getType().equals(Float.class)
                            || field.getType().equals(int.class) || field.getType().equals(long.class) || field.getType().equals(double.class) || field.getType().equals(float.class) || field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {

                        Integer id = null;
                        if (field.isAnnotationPresent(Id.class)) {
                            Id test = field.getAnnotation(Id.class);
                            if (test.AUTO_ID() && idStatus) {
                                try {
                                    if (test.NEGATIVE()) {
                                        id = Integer.parseInt(getLastID());
                                        if (id < 0)
                                            id = id - 1;
                                        else {
                                            id = id + 1;
                                            id = -id;
                                        }
                                    } else
                                        id = Integer.parseInt(getLastID()) + 1;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (!test.NAME().equals(""))
                                fields.add(test.NAME());
                            else
                                fields.add(field.getName());
                        }

                        if (field.isAnnotationPresent(Column.class)) {
                            Column test = field.getAnnotation(Column.class);
                            if (!test.NAME().equals(""))
                                fields.add(test.NAME());
                            else
                                fields.add(field.getName());
                        } else if (!field.isAnnotationPresent(Id.class)) {
                            fields.add(field.getName());
                        }


                        Field fieldsa = classType.getClass().getDeclaredField(field.getName());
                        fieldsa.setAccessible(true);
                        Object value = fieldsa.get(classType);

                        if (id == null) {
                            if (value == null) {
                                value = "";
                                fieldsValues.add(value.toString());
                            } else
                                fieldsValues.add(value.toString());
                        } else {
                            fieldsValues.add(id + "");
                        }


                        String typer = field.getType().getName().toString();
                        if (typer.contains(".")) {
                            String[] types = typer.split("\\.");
                            String type = types[types.length - 1];
                            fieldsType.add(type);
                        } else {
                            fieldsType.add(field.getType().getName().toString());
                        }


                    }
                }

                if (field.isAnnotationPresent(GeometryColumn.class)) {
                    GeometryColumn test = field.getAnnotation(GeometryColumn.class);

                    if (!test.NAME().equals("")) {
                        fields.add(test.NAME());
                        GeoColumnNames.add(test.NAME());
                    } else {
                        fields.add(field.getName());
                        GeoColumnNames.add(field.getName());
                    }
                    Field fieldsa = classType.getClass().getDeclaredField(field.getName());
                    fieldsa.setAccessible(true);
                    Object value = fieldsa.get(classType);

                    String wktGeom = WktWriter.writeWkt((Geometry) value, getGeoType((Geometry) value));
                    fieldsValues.add("Transform(GeometryFromText('" + wktGeom + "'," + SDK_SRID + "), " + DEFAULT_SRID + ")");
                    //----
                    String typer = field.getType().getName().toString();
                    if (typer.contains(".")) {
                        String[] types = typer.split("\\.");
                        String type = types[types.length - 1];
                        fieldsType.add(type);
                    } else {
                        fieldsType.add(field.getType().getName().toString());
                    }
                    //----

                    GeoColumnSRids.add(test.SRID());
                    GeoColumnTypes.add(test.GEO_TYPE());
                }


                if (field.isAnnotationPresent(Blob.class)) {
                    Blob test = field.getAnnotation(Blob.class);

                    if (!test.NAME().equals("")) {
                        fields.add(test.NAME());
                    } else {
                        fields.add(field.getName());
                    }

                    Field fieldsa = classType.getClass().getDeclaredField(field.getName());
                    fieldsa.setAccessible(true);
                    Object value = fieldsa.get(classType);
                    if (test.IMAGE() || test.BYTES_TO_HEX())
                        fieldsValues.add(bytesToHex((byte[]) value));
                    else
                        fieldsValues.add(value.toString());
                    fieldsType.add("BLOB");

                }

                if (field.isAnnotationPresent(Id.class)) {
                    Id test = field.getAnnotation(Id.class);
                    if (test.UNIQUE())
                        fieldsUnique.add(field.getName());
                }
                if (field.isAnnotationPresent(Column.class)) {
                    Column test = field.getAnnotation(Column.class);
                    if (test.UNIQUE())
                        fieldsUnique.add(field.getName());
                }

                if (field.isAnnotationPresent(NonColumn.class)) {
                    if (field.getName().equals(fields.get(fields.size() - 1))) {
                        fields.remove(fields.size() - 1);
                        fieldsValues.remove(fieldsValues.size() - 1);

                        fieldsType.remove(fieldsType.size() - 1);
                    }
                }

                if (field.isAnnotationPresent(Column.class)) {
                    Column test = field.getAnnotation(Column.class);
                    if (test.DATETIME()) {
                        fieldsValues.remove(fieldsValues.size() - 1);
                        fieldsValues.add(getNowDateTime());
                    }
                }

                //insert işleminde  id silme işlemi gerekmekte aksi taktirde id Auto_id ise hata verir.
                //Ama Select işleminde id'ye ihitiyac var o tip durumlarda silinmemeli.
                if (idRemove) {
                    if (field.isAnnotationPresent(Id.class)) {
                        Id test = field.getAnnotation(Id.class);
                        if (!test.AUTO_ID()) {

                            if (!test.NAME().equals("")) {
                                if (field.getName().equals(id_fieldName)) {
                                    if (fields.contains(test.NAME())) {

                                        Field fieldsa = classType.getClass().getDeclaredField(field.getName());
                                        fieldsa.setAccessible(true);
                                        Object value = fieldsa.get(classType);

                                        fields.remove(test.NAME());
                                        fieldsValues.remove(fieldsValues.size() - 1);

                                        fieldsType.remove(fieldsType.size() - 1);
                                    }
                                }
                            } else {
                                if (fields.contains(field.getName())) {
                                    if (field.getName().equals(id_fieldName)) {
                                        Field fieldsa = classType.getClass().getDeclaredField(field.getName());
                                        fieldsa.setAccessible(true);
                                        Object value = fieldsa.get(classType);

                                        fields.remove(field.getName());
                                        fieldsValues.remove(fieldsValues.size() - 1);

                                        fieldsType.remove(fieldsType.size() - 1);
                                    }
                                }
                            }

                        }
                    }
                }

            }

            //TABLO İŞLEMLERİ
            Entity entity = classType.getClass().getAnnotation(Entity.class);
            if (entity.TABLE_OPERATION().equals(Entity.TABLE_OPERATION_TYPE.CREATE)) {
                tableCreate();
            } else if (entity.TABLE_OPERATION().equals(Entity.TABLE_OPERATION_TYPE.DROP_AND_CREATE)) {
                tableDelete();
                tableCreate();
            }


        }

        transactions.define(fieldsValues, fields, fieldsType, tableName, id_fieldName, whereData, andConnector, orConnector);
    }

    private void tableCreate() {
        if (!getTablesName().contains(tableName)) {
            //ID varsa onu başa almak için
            int teo = -1;
            for (int s = 0; s < fields.size(); s++) {
                if (fields.get(s).equals(id_fieldName)) {
                    fields.remove(s);
                    fields.add(0, id_fieldName);
                    teo = s;
                }
            }
            if (teo != -1) {
                String teoo = fieldsType.get(teo);
                fieldsType.remove(teo);
                fieldsType.add(0, teoo);

                String teooVal = fieldsValues.get(teo);
                fieldsValues.remove(teo);
                fieldsValues.add(0, teooVal);
            }
            //--------------------------------------------
            String keys = "";

            if (idPrimeryKey)
                if (id_fieldType.equals("Integer") || id_fieldType.equals("int")) {
                    keys += id_fieldName + " INTEGER PRIMARY KEY AUTOINCREMENT";
                    if (fieldsUnique.contains(id_fieldName))
                        keys += " UNIQUE";
                    keys += ", ";
                }
            for (int i = 0; i < fields.size(); i++) {
                String type = "";
                if (fieldsType.get(i).equals("String") || fieldsType.get(i).equals("string")) {
                    type = "VARCHAR(255)";
                    if (fieldsUnique.contains(fields.get(i)))
                        type += " UNIQUE";
                    keys += fields.get(i) + " " + type + ", ";
                } else if (fieldsType.get(i).equals("Integer") || fieldsType.get(i).equals("int")) {
                    type = "INTEGER";
                    if (fieldsUnique.contains(fields.get(i)))
                        type += " UNIQUE ";
                    keys += fields.get(i) + " " + type + ", ";
                } else if (fieldsType.get(i).equals("Geometry") || fieldsType.get(i).equals("GEOMETRY")
                        || fieldsType.get(i).equals("Point") || fieldsType.get(i).equals("POINT")
                        || fieldsType.get(i).equals("Line") || fieldsType.get(i).equals("LINE")
                        || fieldsType.get(i).equals("Polygon") || fieldsType.get(i).equals("POLYGON")) {

                } else {
                    type = fieldsType.get(i).toUpperCase();
                    keys += fields.get(i) + " " + type + " ";
                    if (fieldsUnique.contains(fields.get(i)))
                        keys += "UNIQUE";
                    keys += ", ";
                }

            }
            keys = keys.substring(0, keys.length() - 2);


            String create = "CREATE TABLE " + tableName + " (" + keys + ")";

            try {
                OHibernateConfig.db.exec(create, null);
                Log.i("OHibernate -> Info", "Table Created");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("OHibernate -> Error", "Table not created : " + e.getMessage());
            }

            createGeometryColumns();
        }
    }

    private void createGeometryColumns() {
        if (GeoColumnNames.size() > 0) {
            for (int i = 0; i < GeoColumnNames.size(); i++) {
                String create = "SELECT AddGeometryColumn('" + tableName + "', '" + GeoColumnNames.get(i) + "', " + GeoColumnSRids.get(i) + ", '" + GeoColumnTypes.get(i) + "', 'XY')";

                try {
                    OHibernateConfig.db.exec(create, null);
                    Log.i("OHibernate -> Info", "Geometry Column Created");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("OHibernate -> Error", "Geometry column not created : " + e.getMessage());
                }
            }
        }
    }

    private void tableDelete() {
        String drop = "DROP TABLE " + tableName + " ";

        try {
            OHibernateConfig.db.exec(drop, null);
            Log.i("OHibernate -> Info", "Table deleted");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", "Table could not be deleted : " + e.getMessage());
        }
    }

    private ArrayList<String> getTablesName() {

        ArrayList<String> tableNames = new ArrayList<>();

        String sql = "SELECT name FROM sqlite_master WHERE type = 'table'";

        Stmt stmt = null;
        try {
            stmt = OHibernateConfig.db.prepare(sql);

            while (stmt.step()) {
                tableNames.add(stmt.column(0).toString());
            }
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }
        return tableNames;
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    // INSERT İŞLEMİ
    public String insert(K obj) throws Exception {
        this.classType = obj;

        try {
            engine(true, true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        String id = transactions.insert();

        return id;
    }

    /**
     * idStatus eğer true olursa otomatik olarak id ataması yapar. Default şeklide bu(true) şekildedir. Eğer false ise o zaman id'ye dokunmaz.
     * yani ID'yi normal bir kolon olarak düşünür.
     * Eğer idStatus değerini false olarak belirlersek objenin id'sini elle set etmiş olmamız gerekir aksi taktirde id değerine null değeri atanır.
     *
     * @param obj      insert edilecek obje.
     * @param idStatus false ise insert edilecek objenin id'sini custom olarak biz belirleriz.
     * @return id değeri dönderir.
     * @throws Exception
     */
    public String insert(K obj, boolean idStatus) throws Exception {
        this.classType = obj;

        try {
            engine(idStatus, true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        String id = transactions.insert();

        return id;

    }

    // UPDATE İŞLEMİ
    public void update(K obj) throws Exception {
        classType = obj;

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        transactions.update();

    }

    public void update() throws Exception {

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        transactions.update();
    }

    public void update(K obj, String key, Object value) throws Exception {
        classType = obj;

        try {
            engine(false, true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        transactions.update(key, value);

    }

    // DELETE İŞLEMİ
    public void delete(K obj) throws Exception {
        classType = obj;

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        transactions.delete();

    }

    public void delete(K obj, String key, Object value) throws Exception {
        classType = obj;

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        transactions.delete(key, value);
    }

    //PERSIST - Obje varsa update et yoksa insert et.
    public String persist(K obj) throws Exception {
        classType = obj;

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        String id = null;
        if (id_fieldName != null) {
            for (int i = 0; i < fields.size(); i++) {
                if (fields.get(i).equals(id_fieldName)) {
                    if (fieldsValues.get(i) == null || fieldsValues.get(i).equals("")) {
                        id = insert(obj);
                        Log.i("OHibernate -> Info", "Object successfully inserted");
                        break;
                    } else {
                        if (select(id_fieldName, fieldsValues.get(i)) == null) {
                            id = insert(obj);
                            Log.i("OHibernate -> Info", "Object successfully inserted");
                            break;
                        } else {
                            update(obj);
                            id = fieldsValues.get(i);
                            Log.i("OHibernate -> Info", "Object successfully updated");
                            break;
                        }
                    }
                }
            }
        }
        return id;
    }

    public String persist(K obj, String key, Object value) throws Exception {
        classType = obj;

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        String id = null;
        if (value != null) {
            for (int i = 0; i < fields.size(); i++) {
                if (fields.get(i).equals(key)) {
                    if (fieldsValues.get(i) == null || fieldsValues.get(i).equals("")) {
                        id = insert(obj);
                        Log.i("OHibernate -> Info", "Object successfully inserted");
                        break;
                    } else {
                        if (select(key, value) == null) {
                            id = insert(obj);
                            Log.i("OHibernate -> Info", "Object successfully inserted");
                            break;
                        } else {
                            update(obj, key, value);
                            if (id_fieldName != null) {
                                for (int w = 0; w < fields.size(); w++) {
                                    if (fields.get(w).equals(id_fieldName)) {
                                        if (fieldsValues.get(w) != null || !fieldsValues.get(w).equals("")) {
                                            id = fieldsValues.get(w);
                                        }
                                    }
                                }
                            }
                            Log.i("OHibernate -> Info", "Object successfully updated");
                            break;
                        }
                    }
                }
            }
        }
        return id;
    }

    // SELECT İŞLEMİ
    public K select() throws Exception {

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        ArrayList<String> tempFields = getSelectTempFields();

        String keye = "";
        for (String field : tempFields) {
            keye += ", " + field + "";
        }
        String keys = keye.substring(2, keye.length());

        //----->
        String sql = "SELECT";
        if (distinct)
            sql += " DISTINCT";
        String connector = "";
        if (andConnector > 0) {
            connector = " and ";
        } else if (orConnector > 0)
            connector = " or ";
        if (this.like != null && this.like.size() > 0) {
            if (this.whereData != null && this.whereData.size() > 0) {
                sql += " " + keys + " FROM " + tableName + " WHERE ";
                for (int i = 0; i < whereData.size(); i++) {
                    if (this.like.get(i) == LIKE_TYPE.FRONT)
                        sql += whereData.getKey(i) + " like '%" + whereData.getValue(i) + "'" + connector;
                    else if (this.like.get(i) == LIKE_TYPE.BEHIND)
                        sql += whereData.getKey(i) + " like '" + whereData.getValue(i) + "%' " + connector;
                    else if (this.like.get(i) == LIKE_TYPE.BOTH)
                        sql += whereData.getKey(i) + " like '%" + whereData.getValue(i) + "%' " + connector;
                    else {
                        sql += whereData.getKey(i) + "='" + whereData.getValue(i) + "' " + connector;
                    }
                }
                if (andConnector > 0) {
                    sql = sql.substring(0, sql.length() - 4);
                } else if (orConnector > 0)
                    sql = sql.substring(0, sql.length() - 3);
            } else {
                sql += " " + keys + " FROM " + tableName + " ";
            }
        } else {
            if (this.whereData != null && this.whereData.size() > 0) {
                sql += " " + keys + " FROM " + tableName + " WHERE ";
                for (int i = 0; i < whereData.size(); i++) {
                    sql += whereData.getKey(i) + "='" + whereData.getValue(i) + "' " + connector;
                }
                if (andConnector > 0) {
                    sql = sql.substring(0, sql.length() - 4);
                } else if (orConnector > 0)
                    sql = sql.substring(0, sql.length() - 3);
            } else {
                sql += " " + keys + " FROM " + tableName + " ";
            }
        }
        //-----> ##################################

        if (this.orderbyData != null && this.orderbyData.size() > 0) {
            sql += " ORDER BY " + this.orderbyData.getKey(0) + " " + this.orderbyData.getValue(0) + "";
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
                        field.set(source, geometries[0]);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
            return source;
        }
        stmt.close();

        return null;
    }

    public K select(String key, Object value) throws Exception {

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        ArrayList<String> tempFields = getSelectTempFields();

        String keye = "";
        for (String field : tempFields) {
            keye += ", " + field + "";
        }
        String keys = keye.substring(2, keye.length());

        //----->
        String sql = "SELECT";
        if (distinct)
            sql += " DISTINCT";
        String connector = "";
        if (andConnector > 0) {
            connector = " and ";
        } else if (orConnector > 0)
            connector = " or ";
        if (this.like != null && this.like.size() > 0) {
            if (this.whereData != null && this.whereData.size() > 0) {
                sql += " " + keys + " FROM " + tableName + " WHERE ";
                for (int i = 0; i < whereData.size(); i++) {
                    if (this.like.get(i) == LIKE_TYPE.FRONT)
                        sql += whereData.getKey(i) + " like '%" + whereData.getValue(i) + "'" + connector;
                    else if (this.like.get(i) == LIKE_TYPE.BEHIND)
                        sql += whereData.getKey(i) + " like '" + whereData.getValue(i) + "%' " + connector;
                    else if (this.like.get(i) == LIKE_TYPE.BOTH)
                        sql += whereData.getKey(i) + " like '%" + whereData.getValue(i) + "%' " + connector;
                    else {
                        sql += whereData.getKey(i) + "='" + whereData.getValue(i) + "' " + connector;
                    }
                }
                if (andConnector > 0) {
                    sql = sql.substring(0, sql.length() - 4);
                } else if (orConnector > 0)
                    sql = sql.substring(0, sql.length() - 3);
            } else {
                sql += " " + keys + " FROM " + tableName + " WHERE " + key + " = '" + value + "'";
            }
        } else {
            if (this.whereData != null && this.whereData.size() > 0) {
                sql += " " + keys + " FROM " + tableName + " WHERE ";
                for (int i = 0; i < whereData.size(); i++) {
                    sql += whereData.getKey(i) + "='" + whereData.getValue(i) + "' " + connector;
                }
                if (andConnector > 0) {
                    sql = sql.substring(0, sql.length() - 4);
                } else if (orConnector > 0)
                    sql = sql.substring(0, sql.length() - 3);
            } else {
                sql += " " + keys + " FROM " + tableName + " WHERE " + key + " = '" + value + "'";
            }
        }
        //-----> ##################################

        if (this.orderbyData != null && this.orderbyData.size() > 0) {
            sql += " ORDER BY " + this.orderbyData.getKey(0) + " " + this.orderbyData.getValue(0) + "";
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
                        field.set(source, geometries[0]);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
            return source;
        }
        stmt.close();

        return null;
    }

    // SELECTALL İŞLEMİ
    public ArrayList<K> selectAll(String key, Object value) throws Exception {

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        ArrayList<String> tempFields = getSelectTempFields();

        String keye = "";
        for (String field : tempFields) {
            keye += ", " + field + "";
        }
        String keys = keye.substring(2, keye.length());

        //----->
        String sql = "SELECT";
        if (distinct)
            sql += " DISTINCT";
        String connector = "";
        if (andConnector > 0) {
            connector = " and ";
        } else if (orConnector > 0)
            connector = " or ";
        if (this.like != null && this.like.size() > 0) {
            if (this.whereData != null && this.whereData.size() > 0) {
                sql += " " + keys + " FROM " + tableName + " WHERE ";
                for (int i = 0; i < whereData.size(); i++) {
                    if (this.like.get(i) == LIKE_TYPE.FRONT)
                        sql += whereData.getKey(i) + " like '%" + whereData.getValue(i) + "'" + connector;
                    else if (this.like.get(i) == LIKE_TYPE.BEHIND)
                        sql += whereData.getKey(i) + " like '" + whereData.getValue(i) + "%' " + connector;
                    else if (this.like.get(i) == LIKE_TYPE.BOTH)
                        sql += whereData.getKey(i) + " like '%" + whereData.getValue(i) + "%' " + connector;
                    else {
                        sql += whereData.getKey(i) + "='" + whereData.getValue(i) + "' " + connector;
                    }
                }
                if (andConnector > 0) {
                    sql = sql.substring(0, sql.length() - 4);
                } else if (orConnector > 0)
                    sql = sql.substring(0, sql.length() - 3);
            } else {
                sql += " " + keys + " FROM " + tableName + " WHERE " + key + " = '" + value + "'";
            }
        } else {
            if (this.whereData != null && this.whereData.size() > 0) {
                sql += " " + keys + " FROM " + tableName + " WHERE ";
                for (int i = 0; i < whereData.size(); i++) {
                    sql += whereData.getKey(i) + "='" + whereData.getValue(i) + "' " + connector;
                }
                if (andConnector > 0) {
                    sql = sql.substring(0, sql.length() - 4);
                } else if (orConnector > 0)
                    sql = sql.substring(0, sql.length() - 3);
            } else {
                sql += " " + keys + " FROM " + tableName + " WHERE " + key + " = '" + value + "'";
            }
        }
        //-----> ##################################

        if (this.orderbyData != null && this.orderbyData.size() > 0) {
            sql += " ORDER BY " + this.orderbyData.getKey(0) + " " + this.orderbyData.getValue(0) + "";
        }

        if (this.limit != null)
            sql += " LIMIT " + this.limit + "";

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
                        field.set(source, geometries[0]);
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }

            sources.add(source);
        }
        stmt.close();


        restart();

        return sources;
    }

    // SELECTALL İŞLEMİ
    public ArrayList<K> selectAll() throws Exception {
        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", e.getMessage());
        }

        ArrayList<String> tempFields = getSelectTempFields();

        String keye = "";
        for (String field : tempFields) {
            keye += ", " + field + "";
        }
        String keys = keye.substring(2, keye.length());


        String sql = "SELECT";

        if (distinct)
            sql += " DISTINCT";

        String connector = "";
        if (andConnector > 0) {
            connector = " and ";
        } else if (orConnector > 0)
            connector = " or ";

        if (this.like != null && this.like.size() > 0) {
            if (this.whereData != null && this.whereData.size() > 0) {
                sql += " " + keys + " FROM " + tableName + " WHERE ";
                for (int i = 0; i < whereData.size(); i++) {
                    if (this.like.get(i) == LIKE_TYPE.FRONT)
                        sql += whereData.getKey(i) + " like '%" + whereData.getValue(i) + "'" + connector;
                    else if (this.like.get(i) == LIKE_TYPE.BEHIND)
                        sql += whereData.getKey(i) + " like '" + whereData.getValue(i) + "%' " + connector;
                    else if (this.like.get(i) == LIKE_TYPE.BOTH)
                        sql += whereData.getKey(i) + " like '%" + whereData.getValue(i) + "%' " + connector;
                    else {
                        sql += whereData.getKey(i) + "='" + whereData.getValue(i) + "' " + connector;
                    }
                }
                if (andConnector > 0) {
                    sql = sql.substring(0, sql.length() - 4);
                } else if (orConnector > 0)
                    sql = sql.substring(0, sql.length() - 3);
            } else {
                sql += " " + keys + " FROM " + tableName + " ";
            }
        } else {
            if (this.whereData != null && this.whereData.size() > 0) {
                sql += " " + keys + " FROM " + tableName + " WHERE ";
                for (int i = 0; i < whereData.size(); i++) {
                    sql += whereData.getKey(i) + "='" + whereData.getValue(i) + "' " + connector;
                }
                if (andConnector > 0) {
                    sql = sql.substring(0, sql.length() - 4);
                } else if (orConnector > 0)
                    sql = sql.substring(0, sql.length() - 3);
            } else {
                sql += " " + keys + " FROM " + tableName + " ";
            }
        }

        if (this.orderbyData != null && this.orderbyData.size() > 0) {
            sql += " ORDER BY " + this.orderbyData.getKey(0) + " " + this.orderbyData.getValue(0) + "";
        }

        if (this.limit != null)
            sql += " LIMIT " + this.limit + "";

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
                        field.set(source, geometries[0]);
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }

            sources.add(source);
        }
        stmt.close();

        return sources;
    }

    //----------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    private final int DEFAULT_SRID = 4326;
    private final int SDK_SRID = 3857;

    private String getGeoType(Geometry geo) {
        if (geo instanceof Point)
            return "POINT";
        else if (geo instanceof Polygon)
            return "POLYGON";
        else if (geo instanceof Line)
            return "LINESTRING";
        else
            return null;
    }

    private Class<K> clazzOfT;

    private K getInstance() throws Exception {
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

    private String getNowDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTimeStamp = dateFormat.format(new Date());

        return currentTimeStamp;
    }

    private ArrayList<String> getSelectTempFields() {
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

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        if (bytes != null) {
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
        return "";
    }
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    private void restart() {
        this.limit = null;
        this.like = null;
        this.whereData = new OHash<>();
        this.andConnector = 0;
        this.orConnector = 0;
        this.distinct = false;
        this.orderbyData = new OHash<>();
    }

    private Integer limit = null;

    public OHibernate limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    private ArrayList<LIKE_TYPE> like = new ArrayList<>();
    private OHash<String, Object> whereData = new OHash<>();

    public OHibernate where(String key, Object value) {
        whereData.add(key, value);
        this.like.add(null);
        return this;
    }

    public OHibernate where(String key, Object value, LIKE_TYPE like) {
        whereData.add(key, value);
        this.like.add(like);
        return this;
    }

    private Integer andConnector = 0;

    public OHibernate and() {
        andConnector++;
        return this;
    }

    private Integer orConnector = 0;

    public OHibernate or() {
        orConnector++;
        return this;
    }

    private boolean distinct = false;

    public OHibernate distinct() {
        distinct = true;
        return this;
    }


    private OHash<String, ORDER_BY_TYPE> orderbyData = new OHash<>();

    public OHibernate orderBy(String key, ORDER_BY_TYPE order_by_type) {
        orderbyData.add(key, order_by_type);
        return this;
    }
    /*  NOTLAR
    //----------------------------------------------------------------------------------------------
        Parametreli select ve selectAll metotlarında eğer where metodu ve parametreler aynı anda verilirse
      where koşulları kabul edilir ve parametreleri görmezden gelir. Aynı zamanda parametrili select ve selectAll
      metotlarına 'like' eklenemez.
        Update ve delete işlemlerinde birden fazla where koşulları kullanılabilir.

    //----------------------------------------------------------------------------------------------
    */
}
