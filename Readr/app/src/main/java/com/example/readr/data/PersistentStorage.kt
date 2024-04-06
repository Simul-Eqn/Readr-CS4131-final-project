package com.example.readr.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri

class PersistentStorage: ContentProvider() {
    companion object {
        const val PROVIDER_NAME = "com.example.readr.first.time"
        const val URL = "content://$PROVIDER_NAME/firsttime"

        // parse uri
        val CONTENT_URI = Uri.parse(URL)
        const val id = "id"
        const val rdm = "rdm"
        const val uriCode = 1
        var uriMatcher: UriMatcher? = null
        private val values: HashMap<String, String>? = null

        const val DATABASE_NAME = "First Time"
        const val TABLE_NAME = "FirstTime"
        const val DATABASE_VERSION = 18 // --------------------------------------------- CHANGE THIS
        const val CREATE_DB_TABLE = (" CREATE TABLE "+ TABLE_NAME
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT, rdm INTEGER NOT NULL);")

        init {
            uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            uriMatcher!!.addURI(PROVIDER_NAME, "firsttime", uriCode)
            uriMatcher!!.addURI(PROVIDER_NAME, "firsttime/*", uriCode)
        }

        var DB:SQLiteDatabase? = null
    }

    lateinit var dbHelper: PersistentStorageHelper
    var db: SQLiteDatabase? = null

    override fun onCreate(): Boolean {
        val context = context
        dbHelper =
            PersistentStorageHelper(context, DATABASE_NAME, DATABASE_VERSION, TABLE_NAME, CREATE_DB_TABLE)
        db = dbHelper.getWritableDatabase()
        PersistentStorage.DB = db
        return db != null
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher!!.match(uri)) {
            uriCode -> "vnd.android.cursor.dir/firsttime"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        if (db == null) throw IllegalStateException("DATABASE NOT FOUND")
        var sortOrder = sortOrder
        val qb = SQLiteQueryBuilder()
        qb.tables = TABLE_NAME
        when (uriMatcher!!.match(uri)) {
            uriCode -> qb.projectionMap = values
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        if (sortOrder == null || sortOrder === "") {
            sortOrder = id
        }

        val c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        c.setNotificationUri(context!!.contentResolver, uri)
        return c
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val rowID = db!!.insert(TABLE_NAME, "", values)
        if (rowID > 0) {
            val _uri = ContentUris.withAppendedId(CONTENT_URI, rowID)
            context!!.contentResolver.notifyChange(_uri, null)
            return _uri
        }
        throw SQLiteException("Failed to add a record into $uri")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        var count = 0
        count = when (uriMatcher!!.match(uri)) {
            uriCode -> db!!.update(TABLE_NAME, values, selection, selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        var count = 0
        count = when (uriMatcher!!.match(uri)) {
            uriCode -> db!!.delete(TABLE_NAME, selection, selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }
}