package co.cuffe.birdie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.UUID;

public class Players {
	public static final String TABLE = "players";
	public static final String ID = "_id";
	public static final String GUID = "player_guid";
	public static final String NAME = "player_name";

	// Column aliases for aggregate functions
	public static final String COUNT = "playercount";

	public static final String CREATE = "create table "
		+ TABLE + "("
		+ ID + " integer autoincrementing primary key,"
		+ GUID + " text,"
		+ NAME + " text);";

	public static String create(Context context, String name) {
		ContentValues values = new ContentValues();
		values.put(GUID, UUID.randomUUID().toString());
		values.put(NAME, name);

		Uri uri = context.getContentResolver().insert(BirdieProvider.Uris.PLAYERS, values);
		return uri.getLastPathSegment();
	}

	public static String findByName(Context context, String name) {
		String playerGuid = null;
		String[] projection = {GUID};
		String where = String.format("%s = '%s'", NAME, name);

		Cursor cursor = context.getContentResolver().query(BirdieProvider.Uris.PLAYERS,
			projection, where, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			playerGuid = cursor.getString(cursor.getColumnIndex(GUID));
			cursor.close();
		}

		return playerGuid;
	}

	public static String findByGuid(Context context, String playerGuid) {
		String playerName = null;
		String[] projection = { NAME };
		Uri uri = Uri.withAppendedPath(BirdieProvider.Uris.PLAYERS, playerGuid);

		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			playerName = cursor.getString(cursor.getColumnIndex(NAME));
			cursor.close();
		}

		return playerName;
	}
}