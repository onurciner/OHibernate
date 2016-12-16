package com.onurciner;

import android.util.Log;

import com.nutiteq.geometry.Geometry;
import com.nutiteq.geometry.Line;
import com.nutiteq.geometry.Point;
import com.nutiteq.geometry.Polygon;
import com.nutiteq.utils.WktWriter;
import com.onurciner.enums.CascadeType;
import com.onurciner.enums.ConditionType;
import com.onurciner.enums.FetchType;
import com.onurciner.enums.GeometryType;
import com.onurciner.enums.LikeType;
import com.onurciner.enums.OrderByType;
import com.onurciner.ohibernate.Blob;
import com.onurciner.ohibernate.Column;
import com.onurciner.ohibernate.Entity;
import com.onurciner.ohibernate.GeometryColumn;
import com.onurciner.ohibernate.Id;
import com.onurciner.ohibernate.ManyToMany;
import com.onurciner.ohibernate.NonColumn;
import com.onurciner.ohibernate.OneToMany;
import com.onurciner.ohibernate.OneToOne;
import com.onurciner.ohibernatetools.Conditions;
import com.onurciner.ohibernatetools.OHashRelational;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import jsqlite.Exception;
import jsqlite.Stmt;

/**
 * Created by Onur.Ciner on 7.11.2016.
 * VERSION 1.0.5
 * ### LOG 1 - 22.11.2016 ###
 * -Birden fazla where koşulu desteği geldi. Where koşullarını bağlamak için and ve or bağlaçları getirildi.
 * -Like sistemi değişti. Where metodunun içerisine 3. parametre olarak like koşulu verilebilmektedir.
 * -Select, Update ve Delete komutları için birden fazla where koşulu kullanılabilir.
 * -Distinct özelliği eklendi.
 * -OrderBy özelliği eklendi.
 * -Persist özelliği eklendi.
 * ### LOG 2 - 01.12.2016 ###
 * -OneToOne, OneToMany yapıları tam olarak tamamlandı.
 * -ManyToMany yapısı sadece insert işlemleri için çalışmaktadır.
 * -Fetch ve Cascade yapıları tamamlandı. Fetch yapındaki Lazy mantığı bilindik lazy yapısı ile çalışmamaktadır. Buradaki lazy yapısında hiç veri gelmez. Ekstra sorgu atmak gerekmektedir.
 * -Bir önceki versiyonda tespit edilen hatalar fix edildi.
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
    private ArrayList<String> fieldsNotNull = new ArrayList<>();

    private String id_fieldName = "";
    private String id_fieldType = "";

    private ArrayList<String> GeoColumnNames = new ArrayList<>();
    private ArrayList<Integer> GeoColumnSRids = new ArrayList<>();
    private ArrayList<GeometryType> GeoColumnTypes = new ArrayList<>();

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
                                        id = Integer.parseInt(getLastNumber(null));
                                        if (id < 0)
                                            id = id - 1;
                                        else {
                                            id = id + 1;
                                            id = -id;
                                        }
                                    } else
                                        id = Integer.parseInt(getLastNumber(null)) + 1;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (!test.NAME().equals(""))
                                fields.add(test.NAME());
                            else
                                fields.add(field.getName());
                        }

                        String autoNumberIncrement = null;
                        String autoNumberRandom = null;
                        if (field.isAnnotationPresent(Column.class)) {
                            Column test = field.getAnnotation(Column.class);
                            if (!test.NAME().equals(""))
                                fields.add(test.NAME());
                            else
                                fields.add(field.getName());

                            if (test.AUTO_INCREMENT_NUMBER()) {
                                if (!test.NAME().equals(""))
                                    autoNumberIncrement = test.NAME();
                                else
                                    autoNumberIncrement = field.getName();
                            } else if (test.AUTO_RANDOM_NUMBER()) {
                                if (!test.NAME().equals(""))
                                    autoNumberRandom = test.NAME();
                                else
                                    autoNumberRandom = field.getName();
                            }
                        } else if (!field.isAnnotationPresent(Id.class)) {
                            fields.add(field.getName());
                        }


                        Field fieldsa = classType.getClass().getDeclaredField(field.getName());
                        fieldsa.setAccessible(true);
                        Object value = fieldsa.get(classType);

                        if (id == null) {
                            if (value == null) {
                                if (autoNumberIncrement == null && autoNumberRandom == null)
                                    fieldsValues.add((String) value);
                                else if (autoNumberIncrement != null) {
                                    Integer plusN = null;
                                    try {
                                        plusN = Integer.parseInt(getLastNumber(autoNumberIncrement)) + 1;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    fieldsValues.add(plusN + "");
                                } else if (autoNumberRandom != null) {
                                    Integer plusN = Integer.parseInt(getRandomNumber(autoNumberRandom));
                                    fieldsValues.add(plusN + "");
                                }
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
                    GeoColumnTypes.add(test.GEOMETRY_TYPE());
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
                    if (test.NOTNULL())
                        fieldsNotNull.add(field.getName());
                }
                if (field.isAnnotationPresent(Column.class)) {
                    Column test = field.getAnnotation(Column.class);
                    if (test.UNIQUE())
                        fieldsUnique.add(field.getName());
                    if (test.NOTNULL())
                        fieldsNotNull.add(field.getName());
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

                                if (fields.contains(test.NAME())) {
                                    fields.remove(test.NAME());
                                    fieldsValues.remove(fieldsValues.size() - 1);
                                    fieldsType.remove(fieldsType.size() - 1);
                                }

                            } else {
                                if (fields.contains(field.getName())) {
                                    if (field.getName().equals(id_fieldName)) {
                                        fields.remove(field.getName());
                                        fieldsValues.remove(fieldsValues.size() - 1);
                                        fieldsType.remove(fieldsType.size() - 1);
                                    }
                                }
                            }

                        }
                    }
                }

                //------------------->
                if (field.isAnnotationPresent(OneToOne.class)) {
                    OneToOne test = field.getAnnotation(OneToOne.class);
                    relationalType = 1;
                    Field fieldsa = classType.getClass().getDeclaredField(field.getName());
                    fieldsa.setAccessible(true);
                    Object value = fieldsa.get(classType);

                    if (value != null)
                        for (Field fieldw : value.getClass().getDeclaredFields()) {
                            fieldw.setAccessible(true);
                            if (fieldw.getName().equals(test.JoinColumn())) {
                                if (!OneToOneR.getJoinColumnArrayList().contains(test.JoinColumn()))
                                    OneToOneR.add(test.JoinColumn(), fieldw, value, field, test.Key(), test.Cascade(), test.Fetch());
                            }
                        }
                } else if (field.isAnnotationPresent(OneToMany.class)) {
                    OneToMany test = field.getAnnotation(OneToMany.class);
                    relationalType = 2;
                    Field fieldsa = classType.getClass().getDeclaredField(field.getName());
                    fieldsa.setAccessible(true);
                    ArrayList value = (ArrayList) fieldsa.get(classType);

                    if (value != null && value.size() > 0)
                        for (Field fieldw : value.get(0).getClass().getDeclaredFields()) {
                            fieldw.setAccessible(true);
                            if (fieldw.getName().equals(test.JoinColumn())) {
                                if (!OneToManyR.getJoinColumnArrayList().contains(test.JoinColumn()))
                                    OneToManyR.add(test.JoinColumn(), fieldw, value, field, test.Key(), test.Cascade(), test.Fetch());
                            }
                        }

                } else if (field.isAnnotationPresent(ManyToMany.class)) {
                    ManyToMany test = field.getAnnotation(ManyToMany.class);
                    relationalType = 3;
                    Field fieldsa = classType.getClass().getDeclaredField(field.getName());
                    fieldsa.setAccessible(true);
                    ArrayList value = (ArrayList) fieldsa.get(classType);

                    if (value != null && value.size() > 0) {
                        for (Field fieldw : value.get(0).getClass().getDeclaredFields()) {
                            fieldw.setAccessible(true);
                            if (fieldw.isAnnotationPresent(Id.class)) {
                                Id test_fieldw = fieldw.getAnnotation(Id.class);
                                String typer = fieldw.getType().getName().toString();
                                String idType = null;
                                if (typer.contains(".")) {
                                    String[] types = typer.split("\\.");
                                    String type = types[types.length - 1];
                                    idType = type;
                                } else {
                                    idType = field.getType().getName().toString();
                                }
                                if (!test_fieldw.NAME().equals("")) {
                                    String[] name = field.toString().split("\\.");
                                    String linkTableName = name[name.length - 1];
                                    manyToManyCreateTable(tableName, linkTableName, id_fieldName, id_fieldType, test_fieldw.NAME(), idType);

                                    if (!test.Key().equals(""))
                                        ManyToManyR.add(test_fieldw.NAME(), fieldw, value, field, test.Key(), test.Cascade(), test.Fetch());
                                    else
                                        ManyToManyR.add(test_fieldw.NAME(), fieldw, value, field, id_fieldName, test.Cascade(), test.Fetch());
                                } else {
                                    String[] name = field.toString().split("\\.");
                                    String linkTableName = name[name.length - 1];
                                    manyToManyCreateTable(tableName, linkTableName, id_fieldName, id_fieldType, fieldw.getName(), idType);
                                    if (!test.Key().equals(""))
                                        ManyToManyR.add(fieldw.getName(), fieldw, value, field, test.Key(), test.Cascade(), test.Fetch());
                                    else
                                        ManyToManyR.add(fieldw.getName(), fieldw, value, field, id_fieldName, test.Cascade(), test.Fetch());
                                }
                            }
                        }
                    } else {
                        String[] name = field.toString().split("\\.");
                        manyToManyCreateTable(tableName, name[name.length - 1], id_fieldName, id_fieldType, "id", "INTEGER");
                    }
                }
                //------------------->
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

        transactions.define(fieldsValues, fields, fieldsType, tableName, id_fieldName, this.conditions);
    }

    private int relationalType = 0; // 1-OneToOne   2-OneToMany     3-ManyToMany

    private String linkTableName;
    private String fkOneName;
    private String fkTwoName;

    private void manyToManyCreateTable(String oneTableName, String twoTableName, String oneColumnName, String oneColumnType, String twoColumnName, String twoColumnType) {
        linkTableName = oneTableName + "_" + twoTableName;
        fkOneName = "fk_" + oneTableName + "_" + oneColumnName;
        fkTwoName = "fk_" + twoTableName + "_" + twoColumnName;

        if (!getTablesName().contains(linkTableName)) {

            String keys = "";

            keys += "id INTEGER PRIMARY KEY AUTOINCREMENT,";
            if (oneColumnType.equals("Integer") || oneColumnType.equals("int")) {
                keys += " fk_" + oneTableName + "_" + oneColumnName + " INTEGER,";
            } else {
                keys += " fk_" + oneTableName + "_" + oneColumnName + " " + oneColumnType + ",";
            }

            if (twoColumnType.equals("Integer") || twoColumnType.equals("int")) {
                keys += " fk_" + twoTableName + "_" + twoColumnName + " INTEGER ";
            } else {
                keys += " fk_" + twoTableName + "_" + twoColumnName + " " + twoColumnType + " ";
            }

            String create = "CREATE TABLE " + linkTableName + " (" + keys + ")";

            try {
                OHibernateConfig.db.exec(create, null);
                Log.i("OHibernate -> Info", "Table Link Created. Table Link Name:" + linkTableName);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("OHibernate -> Error", "Table Link not created : " + e.getMessage());
            }
        }

    }

    private OHashRelational<Object, Object, Object, Object, Object, CascadeType[], FetchType> OneToOneR = new OHashRelational<>();

    private OHashRelational<Object, Object, Object, Object, Object, CascadeType[], FetchType> OneToManyR = new OHashRelational<>();

    private OHashRelational<Object, Object, Object, Object, Object, CascadeType[], FetchType> ManyToManyR = new OHashRelational<>();

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
                    if (fieldsNotNull.contains(id_fieldName))
                        keys += " NOT NULL";
                    keys += ", ";
                }
            for (int i = 0; i < fields.size(); i++) {
                String type = "";
                if (fieldsType.get(i).equals("String") || fieldsType.get(i).equals("string")) {
                    type = "VARCHAR(255)";
                    if (fieldsUnique.contains(fields.get(i)))
                        type += " UNIQUE";
                    if (fieldsNotNull.contains(fields.get(i)))
                        type += " NOT NULL";
                    keys += fields.get(i) + " " + type + ", ";
                } else if (fieldsType.get(i).equals("Integer") || fieldsType.get(i).equals("int")) {
                    type = "INTEGER";
                    if (fieldsUnique.contains(fields.get(i)))
                        type += " UNIQUE ";
                    if (fieldsNotNull.contains(fields.get(i)))
                        type += " NOT NULL ";
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
                    if (fieldsNotNull.contains(fields.get(i)))
                        keys += " NOT NULL ";
                    keys += ", ";
                }

            }
            keys = keys.substring(0, keys.length() - 2);


            String create = "CREATE TABLE " + tableName + " (" + keys + ")";

            try {
                OHibernateConfig.db.exec(create, null);
                Log.i("OHibernate -> Info", "Table Created. Table Name:" + tableName);
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

    // INSERT İŞLEMİ
    public String insert(K obj) throws Exception {
        this.classType = obj;

        try {
            engine(true, true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        }

        String id = transactions.insert();


        if (!fields.contains(id_fieldName)) {
            fields.add(id_fieldName);
            fieldsValues.add(id);
        }

        if (relationalType == 1)
            new RelationalTableOperations().relationalTableOperationsOneToOne(1, OneToOneR, fields, fieldsValues, id_fieldName);
        else if (relationalType == 2)
            new RelationalTableOperations().relationalTableOperationsOneToMany(1, OneToManyR, fields, fieldsValues, id_fieldName);
        else if (relationalType == 3)
            new RelationalTableOperations().relationalTableOperationsManyToMany(1, ManyToManyR, fields, fieldsValues, id_fieldName, id, linkTableName, fkOneName, fkTwoName);

        return id;
    }

    // INSERT İŞLEMİ

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
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        }

        String id = transactions.insert();


        if (idStatus)
            if (!fields.contains(id_fieldName)) {
                fields.add(id_fieldName);
                fieldsValues.add(id);
            }
        if (relationalType == 1)
            new RelationalTableOperations().relationalTableOperationsOneToOne(1, OneToOneR, fields, fieldsValues, id_fieldName);
        else if (relationalType == 2)
            new RelationalTableOperations().relationalTableOperationsOneToMany(1, OneToManyR, fields, fieldsValues, id_fieldName);

        return id;
    }

    // UPDATE İŞLEMİ
    public void update(K obj) throws Exception {
        classType = obj;

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        }

        transactions.update();

        if (relationalType == 1)
            new RelationalTableOperations().relationalTableOperationsOneToOne(2, OneToOneR, fields, fieldsValues, id_fieldName);
        else if (relationalType == 2)
            new RelationalTableOperations().relationalTableOperationsOneToMany(2, OneToManyR, fields, fieldsValues, id_fieldName);
    }

    // UPDATE İŞLEMİ
    public void update() throws Exception {

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        }

        transactions.update();

        if (relationalType == 1)
            new RelationalTableOperations().relationalTableOperationsOneToOne(2, OneToOneR, fields, fieldsValues, id_fieldName);
        else if (relationalType == 2)
            new RelationalTableOperations().relationalTableOperationsOneToMany(2, OneToManyR, fields, fieldsValues, id_fieldName);

    }

    // UPDATE İŞLEMİ
    public void update(K obj, String key, Object value) throws Exception {
        classType = obj;

        try {
            engine(false, true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        }

        transactions.update(key, value);

        if (relationalType == 1)
            new RelationalTableOperations().relationalTableOperationsOneToOne(2, OneToOneR, fields, fieldsValues, id_fieldName);
        else if (relationalType == 2)
            new RelationalTableOperations().relationalTableOperationsOneToMany(2, OneToManyR, fields, fieldsValues, id_fieldName);

    }

    // DELETE İŞLEMİ
    public void delete(K obj) throws Exception {
        classType = obj;

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        }

        transactions.delete();

        if (relationalType == 1)
            new RelationalTableOperations().relationalTableOperationsOneToOne(3, OneToOneR, fields, fieldsValues, id_fieldName);
        else if (relationalType == 2)
            new RelationalTableOperations().relationalTableOperationsOneToMany(3, OneToManyR, fields, fieldsValues, id_fieldName);

    }

    // DELETE İŞLEMİ
    public void delete(K obj, String key, Object value) throws Exception {
        classType = obj;

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        }

        transactions.delete(key, value);

        if (relationalType == 1)
            new RelationalTableOperations().relationalTableOperationsOneToOne(3, OneToOneR, fields, fieldsValues, id_fieldName);
        else if (relationalType == 2)
            new RelationalTableOperations().relationalTableOperationsOneToMany(3, OneToManyR, fields, fieldsValues, id_fieldName);

    }

    //PERSIST - Obje varsa update et yoksa insert et.
    public String persist(K obj) throws Exception {
        classType = obj;

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
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

    //PERSIST
    public String persist(K obj, String key, Object value) throws Exception {
        classType = obj;

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
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
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        }

        K data = new Selections<K>(classType).select(this.fields, this.conditions, this.tableName, null, null);
        restart();
        return data;

    }

    // SELECT İŞLEMİ
    public K select(String key, Object value) throws Exception {

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        }

        K data =  new Selections<K>(classType).select(this.fields, this.conditions, this.tableName, key, value);
        restart();
        return data;

    }

    // SELECTALL İŞLEMİ
    public ArrayList<K> selectAll(String key, Object value) throws Exception {

        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        }

        ArrayList<K> datas = new Selections<K>(classType).selectAll(this.fields, this.conditions, this.tableName, key, value);
        restart();
        return datas;

    }

    // SELECTALL İŞLEMİ
    public ArrayList<K> selectAll() throws Exception {
        try {
            engine(false, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("OHibernate - Engine Problem -> Error", e.getMessage());
        }

        ArrayList<K> datas = new Selections<K>(classType).selectAll(this.fields, this.conditions, this.tableName, null, null);
        restart();
        return datas;
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

    int randomController = 0;

    private String getRandomNumber(String columnName) {

        Random ran = new Random();
        int random = ran.nextInt(899999999) + 100000000;

        String sql = "SELECT " + columnName + " FROM " + tableName + " WHERE " + columnName + "='" + random + "'";
        boolean isData = false;
        try {
            Stmt stmt = OHibernateConfig.db.prepare(sql);
            while (stmt.step()) {
                if (stmt.column(0) != null && stmt.column(0).toString() != null)
                    isData = true;
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OHibernate -> Error", "getLastID() -> " + e.getMessage());
        }
        if (isData) {
            if (randomController == 800000000) {
                return "0";
            }
            randomController++;
            getRandomNumber(columnName);
        }

        return random + "";
    }

    private String getLastNumber(String columnName) throws Exception {
        String one = "";
        String two = "";
        String sql = "";
        if (columnName == null)
            sql = "SELECT MAX(" + id_fieldName + ") FROM " + tableName + " ";
        else
            sql = "SELECT MAX(" + columnName + ") FROM " + tableName + " ";

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
        String sql2 = "";
        if (columnName == null)
            sql2 = "SELECT MIN(" + id_fieldName + ") FROM " + tableName + " ";
        else
            sql2 = "SELECT MIN(" + columnName + ") FROM " + tableName + " ";

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

    private Conditions conditions = new Conditions();

    private void restart() {
        this.conditions = new Conditions();
    }

    public OHibernate limit(Integer limit) {
        this.conditions.setLimit(limit);
        return this;
    }

    public OHibernate where(String key, Object value) {
        this.conditions.getWhereData().add(key, value);
        this.conditions.getLike().add(null);
        this.conditions.getCondition().add(null);
        return this;
    }

    public OHibernate where(String key, Object value, LikeType like) {
        this.conditions.getWhereData().add(key, value);
        this.conditions.getLike().add(like);
        this.conditions.getCondition().add(null);
        return this;
    }

    public OHibernate Where(String key, Object value, ConditionType condition) {
        this.conditions.getWhereData().add(key, value);
        this.conditions.getLike().add(null);
        this.conditions.getCondition().add(condition);
        return this;
    }

    public OHibernate and() {
        this.conditions.setAndConnector(this.conditions.getAndConnector() + 1);
        return this;
    }

    public OHibernate or() {
        this.conditions.setOrConnector(this.conditions.getOrConnector() + 1);
        return this;
    }

    public OHibernate distinct() {
        this.conditions.setDistinct(true);
        return this;
    }

    public OHibernate orderBy(String key, OrderByType order_by_type) {
        this.conditions.getOrderbyData().add(key, order_by_type);
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
