package co.cuffe.birdie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.UUID;

public class Courses {

	public static final String TABLE = "courses";
	public static final String ID = "_id";
	public static final String GUID = "course_guid";
	public static final String NAME = "course_name";

	public static final String CREATE = "create table "	+ TABLE + "("
		+ ID + " integer primary key,"
		+ GUID + " text,"
		+ NAME + " text);";

	public static String create(Context context, String name) {
		ContentValues values = new ContentValues();
		values.put(NAME, name);
		values.put(GUID, UUID.randomUUID().toString());
		Uri uri = context.getContentResolver().insert(BirdieProvider.Uris.COURSES, values);

		return uri.getLastPathSegment();
	}

	public static String findByName(Context context, String name) {
		String courseGuid = null;
		String[] projection = { GUID };
		String where = String.format("%s = '%s'", NAME, name);

		Cursor cursor = context.getContentResolver().query(BirdieProvider.Uris.COURSES,
			projection, where, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			courseGuid = cursor.getString(cursor.getColumnIndex(GUID));
			cursor.close();
		}

		return courseGuid;
	}

	public static String findByGuid(Context context, String courseGuid) {
		String courseName = null;
		String[] projection = { NAME };

		Uri uri = Uri.withAppendedPath(BirdieProvider.Uris.COURSES, courseGuid);
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			courseName = cursor.getString(cursor.getColumnIndex(NAME));
			cursor.close();
		}

		return courseName;
	}
}
