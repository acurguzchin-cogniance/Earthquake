package com.example.acurguzchin.earthquake;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Created by alex on 4/20/15.
 */
public class EarthquakeProvider extends ContentProvider {
    private static final String PKG = "com.example.acurguzchin.earthquake";

    public static final Uri CONTENT_URI = Uri.parse("content://" + PKG + "/earthquakes");

    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_DETAILS = "details";
    public static final String KEY_SUMMARY = "summary";
    public static final String KEY_LOCATION_LAT = "latitude";
    public static final String KEY_LOCATION_LNG = "longitude";
    public static final String KEY_MAGNITUDE = "magnitude";
    public static final String KEY_LINK = "link";

    private EarthquakeDatabaseHelper dbHelper;

    private static UriMatcher uriMatcher;
    private static final int QUAKES = 1;
    private static final int QUAKE_ID = 2;
    private static final int SEARCH = 3;

    private static final HashMap<String, String> SEARCH_PROJECTION_MAP;
    static {
        SEARCH_PROJECTION_MAP = new HashMap<>();
        SEARCH_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, KEY_SUMMARY + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        SEARCH_PROJECTION_MAP.put("_id", KEY_ID + " AS " + "_id");
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PKG, "earthquakes", QUAKES);
        uriMatcher.addURI(PKG, "earthquakes/#", QUAKE_ID);
        uriMatcher.addURI(PKG, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
        uriMatcher.addURI(PKG, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
        uriMatcher.addURI(PKG, SearchManager.SUGGEST_URI_PATH_SHORTCUT, SEARCH);
        uriMatcher.addURI(PKG, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SEARCH);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new EarthquakeDatabaseHelper(getContext(), EarthquakeDatabaseHelper.DB_NAME, null, EarthquakeDatabaseHelper.DB_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(EarthquakeDatabaseHelper.DB_TABLE);

        switch (uriMatcher.match(uri)) {
            case QUAKE_ID:
                queryBuilder.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
                break;
            case SEARCH:
                queryBuilder.appendWhere(KEY_SUMMARY + " LIKE \"%" +  uri.getPathSegments().get(1) + "%\"");
                queryBuilder.setProjectionMap(SEARCH_PROJECTION_MAP);
                break;
            default:
                break;
        }

        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = KEY_DATE;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case QUAKES:
                return "vnd.android.cursor.dir/vnd.com.example.acurguzchin.earthquake";
            case QUAKE_ID:
                return "vnd.android.cursor.item/vnd.com.example.acurguzchin.earthquake";
            case SEARCH :
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long rowId = database.insert(EarthquakeDatabaseHelper.DB_TABLE, "details", values);
        if (rowId > -1) {
            Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(resultUri, null);
            return resultUri;
        }
        else {
            return null;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri)) {
            case QUAKES:
                count = database.delete(EarthquakeDatabaseHelper.DB_TABLE, selection, selectionArgs);
                break;
            case QUAKE_ID:
                String rowId = uri.getPathSegments().get(1);
                String where = KEY_ID + "=" + rowId;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND (" + selection + ")";
                }
                count = database.delete(EarthquakeDatabaseHelper.DB_TABLE, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri)) {
            case QUAKES:
                count = database.update(EarthquakeDatabaseHelper.DB_TABLE, values, selection, selectionArgs);
                break;
            case QUAKE_ID:
                String rowId = uri.getPathSegments().get(1);
                String where = KEY_ID + "=" + rowId;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND (" + selection + ")";
                }
                count = database.update(EarthquakeDatabaseHelper.DB_TABLE, values, where, selectionArgs);
                break;
            default: throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private static class EarthquakeDatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG = "EarthquakeProvider";

        private static final String DB_NAME = "earthquakes.db";
        private static final int DB_VERSION = 1;
        private static final String DB_TABLE = "earthquakes";

        private static final String CREATE_SQL =
        "create table " + DB_TABLE + " (" +
                KEY_ID + " integer primary key autoincrement, " +
                KEY_DATE + " INTEGER, " +
                KEY_DETAILS + " TEXT, " +
                KEY_SUMMARY + " TEXT, " +
                KEY_LOCATION_LAT + " FLOAT, " +
                KEY_LOCATION_LNG + " FLOAT, " +
                KEY_MAGNITUDE + " FLOAT, " +
                KEY_LINK + " TEXT" +
            ");";
        
        public EarthquakeDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(db);
        }
    }
}
