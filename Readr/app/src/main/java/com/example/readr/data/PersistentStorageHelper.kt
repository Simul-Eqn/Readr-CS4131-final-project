package com.example.readr.data


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class PersistentStorageHelper internal constructor(context: Context?, db_name:String, db_version:Int, val table_name:String, val create_table_query:String) : SQLiteOpenHelper(context, db_name, null, db_version) {
    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(create_table_query)
        } catch (sqle:SQLiteException) {
            Log.w("SQL table attempt create", "Table already exists")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS "+table_name)
        onCreate(db)
    }

    // uncomment when changing table structure
    /*override fun onOpen(db: SQLiteDatabase) {
        db.execSQL(create_table_query)
    }*/
}