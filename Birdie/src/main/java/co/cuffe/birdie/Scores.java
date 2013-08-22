package co.cuffe.birdie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Scores {

	public static final String TABLE = "scores";
	public static final String ID = "_id";
	public static final String GUID = "score_guid";
	public static final String GAME_GUID = "score_game_guid";
	public static final String PLAYER_GUID = "score_player_guid";
	public static final String TIME = "score_time";
	public static final String HOLE = "score_hole";
	public static final String SCORE = "score_score";

	public static final String CREATE = "create table "
		+ TABLE + "("
		+ ID + " integer autoincrementing primary key,"
		+ GUID + " text,"
		+ GAME_GUID + " text,"
		+ PLAYER_GUID + " text,"
		+ TIME + " integer,"
		+ HOLE + " integer,"
		+ SCORE + " integer,"
		+ "foreign key(" + GAME_GUID + ") references " + Games.TABLE + ","
		+ "foreign key(" + PLAYER_GUID + ") references " + Players.TABLE + ");";

	public static Uri create(Context context, String playerGuid, String gameGuid,
							 int hole, int score) {

		ContentValues values = new ContentValues();
		values.put(Scores.PLAYER_GUID, playerGuid);
		values.put(Scores.GAME_GUID, gameGuid);
		values.put(Scores.GUID, UUID.randomUUID().toString());
		values.put(Scores.HOLE, hole);
		values.put(Scores.SCORE, score);
		values.put(Scores.TIME, new Date().getTime());

		return context.getContentResolver().insert(BirdieProvider.Uris.SCORES, values);
	}

	public static void update(Context context, String scoreGuid, int score) {
		Uri uri = Uri.withAppendedPath(BirdieProvider.Uris.SCORES, scoreGuid);

		ContentValues values = new ContentValues();
		values.put(Scores.SCORE, score);

		context.getContentResolver().update(uri, values, null, null);
	}

	public static int getTotalForGame(Context ctx, String playerGuid, String gameGuid) {
		int total = 0;
		Uri uri = BirdieProvider.Uris.GAMES.buildUpon()
			.appendPath(gameGuid)
			.appendPath(playerGuid)
			.appendPath("total")
			.build();
		String[] projection = { SCORE };

		Cursor cursor = ctx.getContentResolver().query(uri, projection, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			total = cursor.getInt(cursor.getColumnIndex(SCORE));
			cursor.close();
		}

		return total;
	}

	public static Map<Integer, Integer> getScoresForGame(Context ctx, String pGuid, String gGuid) {
		Map<Integer, Integer> holeSet = new HashMap();
		Uri uri = BirdieProvider.Uris.GAMES.buildUpon()
			.appendPath(gGuid)
			.appendPath(pGuid)
			.appendPath("scores")
			.build();
		String[] projection = { SCORE, HOLE };

		Cursor cursor = ctx.getContentResolver().query(uri, projection, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToPosition(-1);
			while(cursor.moveToNext()) {
				int hole = cursor.getInt(cursor.getColumnIndex(HOLE));
				int score = cursor.getInt(cursor.getColumnIndex(SCORE));
				holeSet.put(hole, score);
			}
			cursor.close();
		}

		return holeSet;
	}
}
