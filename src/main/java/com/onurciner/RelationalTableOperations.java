package com.onurciner;

import android.util.Log;


import com.onurciner.enums.CascadeType;
import com.onurciner.enums.FetchType;
import com.onurciner.ohibernate.Id;
import com.onurciner.ohibernate.ManyToMany;
import com.onurciner.ohibernate.OneToMany;
import com.onurciner.ohibernate.OneToOne;
import com.onurciner.ohibernatetools.OHashRelational;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import jsqlite.Exception;
import jsqlite.Stmt;

/**
 * Created by Onur.Ciner on 28.11.2016.
 */

public class RelationalTableOperations {

    public void relationalTableOperationsOneToOne(int status, OHashRelational OneToOne, ArrayList<String> fields, ArrayList<String> fieldsValues, String id_fieldName) {

        for (int i = 0; i < OneToOne.size(); i++) {
            for (int f = 0; f < fields.size(); f++) {
                if (!OneToOne.getKey(i).equals("")) {
                    if (OneToOne.getKey(i).equals(fields.get(f))) {
                        try {
                            ((Field) OneToOne.getJoinColumnField(i)).setAccessible(true);

                            if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(int.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Integer.class)) {
                                ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), Integer.parseInt(fieldsValues.get(f).toString()));
                            } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(double.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Double.class)) {
                                ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), Double.parseDouble(fieldsValues.get(f).toString()));
                            } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(float.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Float.class)) {
                                ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), Float.parseFloat(fieldsValues.get(f).toString()));
                            } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(long.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Long.class)) {
                                if (fieldsValues.get(f) != null && !fieldsValues.get(f).toString().equals(""))
                                    ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), Long.parseLong(fieldsValues.get(f).toString()));
                                else
                                    ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), null);
                            } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(boolean.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Boolean.class)) {
                                ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), Boolean.parseBoolean(fieldsValues.get(f).toString()));
                            } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(byte[].class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Byte[].class)) {
                                ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), fieldsValues.get(f));
                            } else
                                ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), fieldsValues.get(f).toString());

                            Directional directional = new Directional<>((Class) OneToOne.getOneBean(i).getClass());

                            int Cascade = 0;
                            for (CascadeType cascadeType : (CascadeType[]) OneToOne.getCascade(i))
                                if (cascadeType.equals(CascadeType.ALL))
                                    Cascade = 1;
                                else if (cascadeType.equals(CascadeType.PERSIST))
                                    Cascade = 2;
                                else if (cascadeType.equals(CascadeType.INSERT))
                                    Cascade = 3;
                                else if (cascadeType.equals(CascadeType.UPDATE))
                                    Cascade = 4;
                                else if (cascadeType.equals(CascadeType.DELETE))
                                    Cascade = 5;

                            if (status == 1) {
                                if (Cascade == 1 || Cascade == 2 || Cascade == 3)
                                    directional.insert(OneToOne.getOneBean(i));
                            } else if (status == 2) {
                                if (Cascade == 1 || Cascade == 2 || Cascade == 4)
                                    directional.update(OneToOne.getOneBean(i));
                            } else if (status == 3) {
                                if (Cascade == 1 || Cascade == 5)
                                    directional.delete(OneToOne.getOneBean(i));
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (id_fieldName.equals(fields.get(f))) {
                        try {
                            ((Field) OneToOne.getJoinColumnField(i)).setAccessible(true);

                            if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(int.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Integer.class)) {
                                ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), Integer.parseInt(fieldsValues.get(f).toString()));
                            } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(double.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Double.class)) {
                                ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), Double.parseDouble(fieldsValues.get(f).toString()));
                            } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(float.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Float.class)) {
                                ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), Float.parseFloat(fieldsValues.get(f).toString()));
                            } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(long.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Long.class)) {
                                if (fieldsValues.get(f) != null && !fieldsValues.get(f).toString().equals(""))
                                    ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), Long.parseLong(fieldsValues.get(f).toString()));
                                else
                                    ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), fieldsValues.get(f).toString());
                            } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(boolean.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Boolean.class)) {
                                ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), Boolean.parseBoolean(fieldsValues.get(f).toString()));
                            } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(byte[].class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Byte[].class)) {
                                ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), fieldsValues.get(f));
                            } else
                                ((Field) OneToOne.getJoinColumnField(i)).set(OneToOne.getOneBean(i), fieldsValues.get(f).toString());

                            Directional directional = new Directional<>((Class) OneToOne.getOneBean(i).getClass());

                            ArrayList<Integer> Cascade = new ArrayList<>();
                            for (CascadeType cascadeType : (CascadeType[]) OneToOne.getCascade(i))
                                if (cascadeType.equals(CascadeType.ALL))
                                    Cascade.add(1);
                                else if (cascadeType.equals(CascadeType.PERSIST))
                                    Cascade.add(2);
                                else if (cascadeType.equals(CascadeType.INSERT))
                                    Cascade.add(3);
                                else if (cascadeType.equals(CascadeType.UPDATE))
                                    Cascade.add(4);
                                else if (cascadeType.equals(CascadeType.DELETE))
                                    Cascade.add(5);

                            if (status == 1) {
                                if (Cascade.contains(1) || Cascade.contains(2) || Cascade.contains(3))
                                    directional.persist(OneToOne.getOneBean(i));
                            } else if (status == 2) {
                                if (Cascade.contains(1) || Cascade.contains(2) || Cascade.contains(4))
                                    directional.persist(OneToOne.getOneBean(i));
                            } else if (status == 3) {
                                if (Cascade.contains(1) || Cascade.contains(5))
                                    directional.delete(OneToOne.getOneBean(i));
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    public void relationalTableOperationsOneToMany(int status, OHashRelational OneToOne, ArrayList<String> fields, ArrayList<String> fieldsValues, String id_fieldName) {

        for (int i = 0; i < OneToOne.size(); i++) {
            for (int f = 0; f < fields.size(); f++) {
                if (!OneToOne.getKey(i).equals("")) {
                    if (OneToOne.getKey(i).equals(fields.get(f))) {
                        try {
                            if (OneToOne.getOneBean(i) instanceof ArrayList) {
                                for (Object obje : (ArrayList) OneToOne.getOneBean(i)) {

                                    ((Field) OneToOne.getJoinColumnField(i)).setAccessible(true);

                                    if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(int.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Integer.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Integer.parseInt(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(double.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Double.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Double.parseDouble(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(float.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Float.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Float.parseFloat(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(long.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Long.class)) {
                                        if (fieldsValues.get(f) != null && !fieldsValues.get(f).toString().equals(""))
                                            ((Field) OneToOne.getJoinColumnField(i)).set(obje, Long.parseLong(fieldsValues.get(f).toString()));
                                        else
                                            ((Field) OneToOne.getJoinColumnField(i)).set(obje, null);
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(boolean.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Boolean.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Boolean.parseBoolean(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(byte[].class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Byte[].class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, fieldsValues.get(f));
                                    } else
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, fieldsValues.get(f).toString());

                                    Directional directional = new Directional<>((Class) obje.getClass());

                                    int Cascade = 0;
                                    for (CascadeType cascadeType : (CascadeType[]) OneToOne.getCascade(i))
                                        if (cascadeType.equals(CascadeType.ALL))
                                            Cascade = 1;
                                        else if (cascadeType.equals(CascadeType.PERSIST))
                                            Cascade = 2;
                                        else if (cascadeType.equals(CascadeType.INSERT))
                                            Cascade = 3;
                                        else if (cascadeType.equals(CascadeType.UPDATE))
                                            Cascade = 4;
                                        else if (cascadeType.equals(CascadeType.DELETE))
                                            Cascade = 5;

                                    if (status == 1) {
                                        if (Cascade == 1 || Cascade == 2 || Cascade == 3) {
                                            String id = directional.persist(obje);
                                            for (int r = 0; r < ((ArrayList) OneToOne.getOneBean(i)).size(); r++) {
                                                if (((ArrayList) OneToOne.getOneBean(i)).get(r).equals(obje)) {
                                                    ((ArrayList) OneToOne.getOneBean(i)).set(r, directional.select(OneToOne.getKey(i).toString(), id));
                                                }
                                            }
                                        }
                                    } else if (status == 2) {
                                        if (Cascade == 1 || Cascade == 2 || Cascade == 4) {
                                            String id = directional.persist(obje);
                                            for (int r = 0; r < ((ArrayList) OneToOne.getOneBean(i)).size(); r++) {
                                                if (((ArrayList) OneToOne.getOneBean(i)).get(r).equals(obje)) {
                                                    ((ArrayList) OneToOne.getOneBean(i)).set(r, directional.select(OneToOne.getKey(i).toString(), id));
                                                }
                                            }
                                        }
                                    } else if (status == 3) {
                                        if (Cascade == 1 || Cascade == 5)
                                            directional.delete(obje);
                                    }
                                }
                                String masterObjID = null;
                                for (int s = 0; s < fields.size(); s++) {
                                    if (fields.get(s).equals(OneToOne.getKey(i).toString())) {
                                        masterObjID = fieldsValues.get(s);
                                    }
                                }
                                Directional directional = new Directional<>((Class) ((ArrayList) OneToOne.getOneBean(0)).get(0).getClass());
                                ArrayList<?> arrayList = directional.selectAll(OneToOne.getJoinColumn(i).toString(), masterObjID);

                                Iterator<Object> rr = (Iterator<Object>) arrayList.iterator();
                                while (rr.hasNext()) {
                                    Object o = rr.next();
                                    boolean varmi = false;
                                    for (int y = 0; y < ((ArrayList) OneToOne.getOneBean(i)).size(); y++) {
                                        Field[] all = ((ArrayList) OneToOne.getOneBean(i)).get(y).getClass().getDeclaredFields();
                                        for (Field field : all) {
                                            if (field.isAnnotationPresent(Id.class)) {
                                                field.setAccessible(true);

                                                Field[] allRR = o.getClass().getDeclaredFields();
                                                for (Field fieldRR : allRR) {
                                                    if (fieldRR.isAnnotationPresent(Id.class)) {
                                                        fieldRR.setAccessible(true);

                                                        if (field.get(((ArrayList) OneToOne.getOneBean(i)).get(y)).equals(fieldRR.get(o))) {
                                                            varmi = true;
                                                        }
                                                    }
                                                }

                                            }
                                        }

                                    }
                                    if (varmi) {
                                        rr.remove();
                                    }

                                }

                                if (arrayList != null && arrayList.size() > 0) {
                                    for (Object obj : arrayList) {
                                        directional.delete(obj);
                                    }
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (id_fieldName.equals(fields.get(f))) {
                        try {
                            if (OneToOne.getOneBean(i) instanceof ArrayList) {
                                for (Object obje : (ArrayList) OneToOne.getOneBean(i)) {

                                    ((Field) OneToOne.getJoinColumnField(i)).setAccessible(true);

                                    if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(int.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Integer.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Integer.parseInt(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(double.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Double.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Double.parseDouble(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(float.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Float.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Float.parseFloat(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(long.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Long.class)) {
                                        if (fieldsValues.get(f) != null && !fieldsValues.get(f).toString().equals(""))
                                            ((Field) OneToOne.getJoinColumnField(i)).set(obje, Long.parseLong(fieldsValues.get(f).toString()));
                                        else
                                            ((Field) OneToOne.getJoinColumnField(i)).set(obje, fieldsValues.get(f).toString());
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(boolean.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Boolean.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Boolean.parseBoolean(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(byte[].class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Byte[].class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, fieldsValues.get(f));
                                    } else
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, fieldsValues.get(f).toString());

                                    Directional directional = new Directional<>((Class) obje.getClass());

                                    ArrayList<Integer> Cascade = new ArrayList<>();
                                    for (CascadeType cascadeType : (CascadeType[]) OneToOne.getCascade(i))
                                        if (cascadeType.equals(CascadeType.ALL))
                                            Cascade.add(1);
                                        else if (cascadeType.equals(CascadeType.PERSIST))
                                            Cascade.add(2);
                                        else if (cascadeType.equals(CascadeType.INSERT))
                                            Cascade.add(3);
                                        else if (cascadeType.equals(CascadeType.UPDATE))
                                            Cascade.add(4);
                                        else if (cascadeType.equals(CascadeType.DELETE))
                                            Cascade.add(5);

                                    if (status == 1) {
                                        if (Cascade.contains(1) || Cascade.contains(2) || Cascade.contains(3)) {
                                            String id = directional.persist(obje);
                                            for (int r = 0; r < ((ArrayList) OneToOne.getOneBean(i)).size(); r++) {
                                                if (((ArrayList) OneToOne.getOneBean(i)).get(r).equals(obje)) {
                                                    ((ArrayList) OneToOne.getOneBean(i)).set(r, directional.select(id_fieldName, id));
                                                }
                                            }
                                        }
                                    } else if (status == 2) {
                                        if (Cascade.contains(1) || Cascade.contains(2) || Cascade.contains(4)) {
                                            String id = directional.persist(obje);
                                            for (int r = 0; r < ((ArrayList) OneToOne.getOneBean(i)).size(); r++) {
                                                if (((ArrayList) OneToOne.getOneBean(i)).get(r).equals(obje)) {
                                                    ((ArrayList) OneToOne.getOneBean(i)).set(r, directional.select(id_fieldName, id));
                                                }
                                            }
                                        }
                                    } else if (status == 3) {
                                        if (Cascade.contains(1) || Cascade.contains(5))
                                            directional.delete(obje);
                                    }

                                }
                                String masterObjID = null;
                                for (int s = 0; s < fields.size(); s++) {
                                    if (fields.get(s).equals(id_fieldName)) {
                                        masterObjID = fieldsValues.get(s);
                                    }
                                }
                                Directional directional = new Directional<>((Class) ((ArrayList) OneToOne.getOneBean(0)).get(0).getClass());
                                ArrayList<?> arrayList = directional.selectAll(OneToOne.getJoinColumn(i).toString(), masterObjID);

                                Iterator<Object> rr = (Iterator<Object>) arrayList.iterator();
                                while (rr.hasNext()) {
                                    Object o = rr.next();
                                    boolean varmi = false;
                                    for (int y = 0; y < ((ArrayList) OneToOne.getOneBean(i)).size(); y++) {
                                        Field[] all = ((ArrayList) OneToOne.getOneBean(i)).get(y).getClass().getDeclaredFields();
                                        for (Field field : all) {
                                            if (field.isAnnotationPresent(Id.class)) {
                                                field.setAccessible(true);

                                                Field[] allRR = o.getClass().getDeclaredFields();
                                                for (Field fieldRR : allRR) {
                                                    if (fieldRR.isAnnotationPresent(Id.class)) {
                                                        fieldRR.setAccessible(true);

                                                        if (field.get(((ArrayList) OneToOne.getOneBean(i)).get(y)).equals(fieldRR.get(o))) {
                                                            varmi = true;
                                                        }
                                                    }
                                                }

                                            }
                                        }

                                    }
                                    if (varmi) {
                                        rr.remove();
                                    }

                                }

                                if (arrayList != null && arrayList.size() > 0) {
                                    for (Object obj : arrayList) {
                                        directional.delete(obj);
                                    }
                                }

                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }


    public void relationalTableOperationsManyToMany(int status, OHashRelational OneToOne, ArrayList<String> fields, ArrayList<String> fieldsValues, String id_fieldName, String oneID, String linkTableName, String fkOneName, String fkTwoName) {

        for (int i = 0; i < OneToOne.size(); i++) {
            for (int f = 0; f < fields.size(); f++) {
                if (!OneToOne.getKey(i).equals("")) {
                    if (OneToOne.getKey(i).equals(fields.get(f))) {
                        try {
                            if (OneToOne.getOneBean(i) instanceof ArrayList) {
                                for (Object obje : (ArrayList) OneToOne.getOneBean(i)) {

                                    ((Field) OneToOne.getJoinColumnField(i)).setAccessible(true);

                                    if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(int.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Integer.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Integer.parseInt(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(double.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Double.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Double.parseDouble(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(float.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Float.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Float.parseFloat(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(long.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Long.class)) {
                                        if (fieldsValues.get(f) != null && !fieldsValues.get(f).toString().equals(""))
                                            ((Field) OneToOne.getJoinColumnField(i)).set(obje, Long.parseLong(fieldsValues.get(f).toString()));
                                        else
                                            ((Field) OneToOne.getJoinColumnField(i)).set(obje, null);
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(boolean.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Boolean.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Boolean.parseBoolean(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(byte[].class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Byte[].class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, fieldsValues.get(f));
                                    } else
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, fieldsValues.get(f).toString());

                                    Directional directional = new Directional<>((Class) obje.getClass());

                                    int Cascade = 0;
                                    for (CascadeType cascadeType : (CascadeType[]) OneToOne.getCascade(i))
                                        if (cascadeType.equals(CascadeType.ALL))
                                            Cascade = 1;
                                        else if (cascadeType.equals(CascadeType.PERSIST))
                                            Cascade = 2;
                                        else if (cascadeType.equals(CascadeType.INSERT))
                                            Cascade = 3;
                                        else if (cascadeType.equals(CascadeType.UPDATE))
                                            Cascade = 4;
                                        else if (cascadeType.equals(CascadeType.DELETE))
                                            Cascade = 5;

                                    if (status == 1) {
                                        if (Cascade == 1 || Cascade == 2 || Cascade == 3) {
                                            String twoID = directional.insert(obje);
                                            link_insert(linkTableName, fkOneName, fkTwoName, oneID, twoID);
                                        }
                                    } else if (status == 2) {
                                        if (Cascade == 1 || Cascade == 2 || Cascade == 4)
                                            directional.update(obje);
                                    } else if (status == 3) {
                                        if (Cascade == 1 || Cascade == 5)
                                            directional.delete(obje);
                                    }

                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (id_fieldName.equals(fields.get(f))) {
                        try {
                            if (OneToOne.getOneBean(i) instanceof ArrayList) {
                                for (Object obje : (ArrayList) OneToOne.getOneBean(i)) {

                                    ((Field) OneToOne.getJoinColumnField(i)).setAccessible(true);

                                    if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(int.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Integer.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Integer.parseInt(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(double.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Double.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Double.parseDouble(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(float.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Float.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Float.parseFloat(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(long.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Long.class)) {
                                        if (fieldsValues.get(f) != null && !fieldsValues.get(f).toString().equals(""))
                                            ((Field) OneToOne.getJoinColumnField(i)).set(obje, Long.parseLong(fieldsValues.get(f).toString()));
                                        else
                                            ((Field) OneToOne.getJoinColumnField(i)).set(obje, fieldsValues.get(f).toString());
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(boolean.class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Boolean.class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, Boolean.parseBoolean(fieldsValues.get(f).toString()));
                                    } else if (((Field) OneToOne.getJoinColumnField(i)).getType().equals(byte[].class) || ((Field) OneToOne.getJoinColumnField(i)).getType().equals(Byte[].class)) {
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, fieldsValues.get(f));
                                    } else
                                        ((Field) OneToOne.getJoinColumnField(i)).set(obje, fieldsValues.get(f).toString());

                                    Directional directional = new Directional<>((Class) obje.getClass());

                                    ArrayList<Integer> Cascade = new ArrayList<>();
                                    for (CascadeType cascadeType : (CascadeType[]) OneToOne.getCascade(i))
                                        if (cascadeType.equals(CascadeType.ALL))
                                            Cascade.add(1);
                                        else if (cascadeType.equals(CascadeType.PERSIST))
                                            Cascade.add(2);
                                        else if (cascadeType.equals(CascadeType.INSERT))
                                            Cascade.add(3);
                                        else if (cascadeType.equals(CascadeType.UPDATE))
                                            Cascade.add(4);
                                        else if (cascadeType.equals(CascadeType.DELETE))
                                            Cascade.add(5);

                                    String twoID = null;
                                    if (status == 1) {
                                        if (Cascade.contains(1) || Cascade.contains(2) || Cascade.contains(3)) {
                                            twoID = directional.insert(obje);

                                            link_insert(linkTableName, fkOneName, fkTwoName, oneID, twoID);
                                        }
                                    } else if (status == 2) {
                                        if (Cascade.contains(1) || Cascade.contains(2) || Cascade.contains(4))
                                            directional.update(obje);
                                    } else if (status == 3) {
                                        if (Cascade.contains(1) || Cascade.contains(5))
                                            directional.delete(obje);
                                    }


                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    private void link_insert(String tableName, String oneFKname, String twoFKname, Object oneFKValue, Object twoFKValue) {
        if (oneFKValue instanceof String) {
            oneFKValue = "'" + oneFKValue + "'";
        }
        if (twoFKValue instanceof String) {
            twoFKValue = "'" + twoFKValue + "'";
        }
        String sql = "INSERT INTO " + tableName + " (" + oneFKname + "," + twoFKname + ") VALUES(" + oneFKValue + " , " + twoFKValue + ")";

        try {
            OHibernateConfig.db.exec(sql, null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OHibernate -> Link Table Insert Error", e.getMessage());
        }

    }

    //----------------------------------------------------------------------------------------------

    public void selectRelationalTableOperations(Object source) throws NoSuchFieldException, IllegalAccessException {

        Field[] allFields = source.getClass().getDeclaredFields();

        for (Field field : allFields) {

            if (field.isAnnotationPresent(OneToOne.class)) {
                OneToOne test = field.getAnnotation(OneToOne.class);

                if (test.Fetch().equals(FetchType.EAGER)) {

                    field.setAccessible(true);
                    if (test.Key().equals("")) {
                        Object id = null;
                        Field[] allFieldsRA = source.getClass().getDeclaredFields();
                        for (Field field2 : allFieldsRA) {
                            if (field2.isAnnotationPresent(Id.class)) {
                                field2.setAccessible(true);
                                Object value = field2.get(source);
                                id = value;
                            }
                        }
                        Directional directional = new Directional<>(field.getType());
                        Object obj = directional.select(test.JoinColumn(), id);
                        field.set(source, obj);
                    } else {
                        Object key = null;
                        Field[] allFieldsRA = source.getClass().getDeclaredFields();
                        for (Field field2 : allFieldsRA) {
                            if (field2.getName().equals(test.Key())) {
                                field2.setAccessible(true);
                                Object value = field2.get(source);
                                key = value;
                            }
                        }
                        Directional directional = new Directional<>(field.getType());
                        Object obj = directional.select(test.JoinColumn(), key);
                        field.set(source, obj);
                    }
                }
            } else if (field.isAnnotationPresent(OneToMany.class)) {
                OneToMany test = field.getAnnotation(OneToMany.class);

                if (test.Fetch().equals(FetchType.EAGER)) {

                    field.setAccessible(true);
                    if (test.Key().equals("")) {
                        Object id = null;
                        Field[] allFieldsRA = source.getClass().getDeclaredFields();
                        for (Field field2 : allFieldsRA) {
                            if (field2.isAnnotationPresent(Id.class)) {
                                field2.setAccessible(true);
                                Object value = field2.get(source);
                                id = value;
                            }
                        }
                        if (field.getType().equals(ArrayList.class)) {

                            Type a = field.getGenericType();
                            ParameterizedType pType = (ParameterizedType) a;
                            Type[] arr = pType.getActualTypeArguments();

                            Directional directional = new Directional<>((Class<?>) arr[0]);
                            Object obj = directional.selectAll(test.JoinColumn(), id);
                            field.set(source, obj);
                        }
                    } else {
                        Object key = null;
                        Field[] allFieldsRA = source.getClass().getDeclaredFields();
                        for (Field field2 : allFieldsRA) {
                            if (field2.getName().equals(test.Key())) {
                                field2.setAccessible(true);
                                Object value = field2.get(source);
                                key = value;
                            }
                        }
                        if (field.getType().equals(ArrayList.class)) {

                            Type a = field.getGenericType();
                            ParameterizedType pType = (ParameterizedType) a;
                            Type[] arr = pType.getActualTypeArguments();

                            Directional directional = new Directional<>((Class<?>) arr[0]);
                            Object obj = directional.selectAll(test.JoinColumn(), key);
                            field.set(source, obj);
                        }

                    }
                }

            }
        }

    }


    public void selectRelationalTableOperationsManyToMany(Object source, String linkTableName, String oneFKName, String twoFKName) throws NoSuchFieldException, IllegalAccessException {

        Field[] allFields = source.getClass().getDeclaredFields();

        for (Field field : allFields) {

            if (field.isAnnotationPresent(ManyToMany.class)) {
                ManyToMany test = field.getAnnotation(ManyToMany.class);

                if (test.Fetch().equals(FetchType.EAGER)) {

                    field.setAccessible(true);

                    Object id = null;
                    String idColumnName = null;
                    Field[] allFieldsRA = source.getClass().getDeclaredFields();
                    for (Field field2 : allFieldsRA) {
                        if (field2.isAnnotationPresent(Id.class)) {
                            field2.setAccessible(true);
                            Object value = field2.get(source);
                            id = value;
                            idColumnName = field2.getName();
                        }
                    }

                    ArrayList<String> ob = linkDataControl(linkTableName, oneFKName, twoFKName, id);

                    if (field.getType().equals(ArrayList.class)) {

                        Type a = field.getGenericType();
                        ParameterizedType pType = (ParameterizedType) a;
                        Type[] arr = pType.getActualTypeArguments();

                        Directional directional = new Directional<>((Class<?>) arr[0]);
                        ArrayList<Object> twoData = new ArrayList<>();
                        for (String idr : ob) {
                            Object o = directional.select(idColumnName, idr);
                            twoData.add(o);
                        }

                        field.set(source, twoData);
                    }

                }
            }
        }

    }

    private ArrayList<String> linkDataControl(String tableName, String oneFK, String twoFK, Object id) {

        String sql = "SELECT " + twoFK + " FROM " + tableName + " WHERE " + oneFK + "='" + id + "' ";
        ArrayList<String> fkIDs = new ArrayList<>();
        try {
            Stmt stmt = OHibernateConfig.db.prepare(sql);
            while (stmt.step()) {
                fkIDs.add(stmt.column(0).toString());
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fkIDs;
    }

}
