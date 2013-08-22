package co.cuffe.birdie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.Date;
import java.util.UUID;

public class Games {

	public static final String TABLE = "games";
	public static final String ID = "_id";
	public static final String GUID = "game_guid";
	public static final String DATE = "game_date";
	public static final String COURSE_GUID = "game_course_guid";
	public static final String LATITUDE = "game_latitude";
	public static final String LONGITUDE = "game_longitude";
	public static final String COMPLETE = "game_complete";

	public static final String CREATE = "create table "
		+ TABLE + "("
		+ ID + " integer autoincrementing primary key,"
		+ GUID + " text,"
		+ DATE + " integer,"
		+ COURSE_GUID + " text,"
		+ COMPLETE + " integer,"
		+ LATITUDE + " real,"
		+ LONGITUDE + " real);";

	public static String create(Context context) {
		String guid = UUID.randomUUID().toString();
		long date = new Date().getTime();

		ContentValues values = new ContentValues();
		values.put(Games.GUID, guid);
		values.put(Games.DATE, date);
		values.put(Games.COMPLETE, 0);

		Uri uri = context.getContentResolver().insert(BirdieProvider.Uris.GAMES, values);
		return uri.getLastPathSegment();
	}

	public static void addPlayer(Context context, String playerGuid, String gameGuid, int currentHole) {
		ContentValues values = new ContentValues();
		values.put(PlayersToGames.PLAYER_GUID, playerGuid);
		values.put(PlayersToGames.GAME_GUID, gameGuid);
		context.getContentResolver().insert(BirdieProvider.Uris.PG_JUNCTION, values);

		Scores.create(context, playerGuid, gameGuid, currentHole, 0);
	}

	public static void setCourse(Context context, String gameGuid, String courseGuid) {
		ContentValues values = new ContentValues();
		values.put(COURSE_GUID, courseGuid);

		Uri uri = Uri.withAppendedPath(BirdieProvider.Uris.GAMES, gameGuid);
		context.getContentResolver().update(uri, values, null, null);
	}

	public static String getCourse(Context context, String gameGuid) {
		// TODO make this a single query with a join
		String courseName = null;
		String[] projection = { COURSE_GUID };

		Uri uri = Uri.withAppendedPath(BirdieProvider.Uris.GAMES, gameGuid);
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			String courseGuid = cursor.getString(cursor.getColumnIndex(COURSE_GUID));
			cursor.close();
			courseName = Courses.findByGuid(context, courseGuid);
		}
		return courseName;
	}

	public static String getActiveGameGuid(Context context) {
		String guid = null;
		String where = COMPLETE + " = 0";
		String[] projection = {GUID};
		Cursor cursor = context.getContentResolver().query(BirdieProvider.Uris.GAMES, projection,
			where, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			guid = cursor.getString(cursor.getColumnIndex(GUID));
			cursor.close();
		}
		return guid;
	}

	public static int getHole(Context context, String gameGuid) {
		int hole = 0;
		Uri uri = BirdieProvider.Uris.GAMES.buildUpon()
			.appendPath(gameGuid)
			.appendPath("hole")
			.build();
		String[] projection = { Scores.HOLE };
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			hole = cursor.getInt(cursor.getColumnIndex(Scores.HOLE));
			cursor.close();
		}
		return hole;
	}

	public static void advanceHole(Context context, String gameGuid, int currentHole) {
		// Create a new score table entry for each player for the upcoming hole if
		// no players currently have a score for it.
		if (getHole(context, gameGuid) == currentHole) {
			// Get a cursor containing guids for each player in this game
			Uri uri = BirdieProvider.Uris.GAMES.buildUpon()
				.appendPath(gameGuid)
				.appendPath("players")
				.build();
			String[] projection = { Players.GUID};
			Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

			// Iterate cursor and create new score for upcoming hole for each player;
			if (cursor.getCount() > 0) {
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					String playerGuid = cursor.getString(cursor.getColumnIndex(Players.GUID));
					Scores.create(context, playerGuid, gameGuid, currentHole + 1, 0);
				}
				cursor.close();
			}
		}
	}

	public static void completeGame(Context context, String gameGuid) {
		ContentValues values = new ContentValues();
		values.put(COMPLETE, 1);

		Uri uri = Uri.withAppendedPath(BirdieProvider.Uris.GAMES, gameGuid);
		context.getContentResolver().update(uri, values, null, null);
	}
}
