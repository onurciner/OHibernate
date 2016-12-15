package com.onurciner;

import android.util.Log;

import jsqlite.Database;
import jsqlite.Exception;

/**
 * Created by Onur.Ciner on 14.11.2016.
 */
public class OHibernateConfig {

    public static String DB_PATH = "";
    public static String DB_NAME = "";

    private static OHibernateConfig config = null;

    static protected Database db;

    public OHibernateConfig() {

    }

    public static OHibernateConfig getConfig() {
        if (config == null) {
            config = new OHibernateConfig();

            db = new Database();
            try {
                db.open(DB_PATH + DB_NAME, jsqlite.Constants.SQLITE_OPEN_READWRITE);
            } catch (Exception e) {
                Log.e("OHibernate - Database Problem -> Error", "Failed to open database! " + e.getMessage());
            }
        }
        return config;
    }

    public static OHibernateConfig setDatabase(Database database) {
        if (config == null) {
            config = new OHibernateConfig();
            db = database;
        }
        return config;
    }

    public static Database getDb() {
        return db;
    }

}
