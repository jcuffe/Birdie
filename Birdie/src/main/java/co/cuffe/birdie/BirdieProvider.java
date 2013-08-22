package co.cuffe.birdie;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class BirdieProvider extends ContentProvider {

	public static final String AUTHORITY = "co.cuffe.birdie.provider";

	public static final class Endpoints {
		public static final int GAMES = 10;
		public static final int GAME_ID = 11;
		public static final int GAMES_WITH_PLAYER_COUNT = 12;
		public static final int HIGHEST_HOLE = 13;
		public static final int PLAYERS_FOR_GAME = 14;
		public static final int PLAYERS = 20;
		public static final int PLAYER_ID = 21;
		public static final int PLAYERS_WITH_HOLE_SCORE = 22;
		public static final int SCORES = 30;
		public static final int SCORE_ID = 31;
		public static final int PLAYER_SCORES_FOR_GAME = 32;
		public static final int PLAYER_TOTAL_FOR_GAME = 33;
		public static final int PG_JUNCTION = 40;
		public static final int PG_JUNCTION_ID = 41;
		public static final int COURSES = 50;
		public static final int COURSES_ID = 51;
	}

	public static final class Paths {
		public static final String GAMES = "games";
		public static final String PLAYERS = "players";
		public static final String SCORES = "scores";
		public static final String PG_JUNCTION = "pg";
		public static final String COURSES = "courses";
	}

	public static final class Uris {
		public static final Uri BASE = Uri.parse("content://" + AUTHORITY);
		public static final Uri GAMES = Uri.withAppendedPath(BASE, Paths.GAMES);
		public static final Uri PLAYERS = Uri.withAppendedPath(BASE, Paths.PLAYERS);
		public static final Uri SCORES = Uri.withAppendedPath(BASE, Paths.SCORES);
		public static final Uri PG_JUNCTION = Uri.withAppendedPath(BASE, Paths.PG_JUNCTION);
		public static final Uri COURSES = Uri.withAppendedPath(BASE, Paths.COURSES);
	}

	public static final class Types {
		public static final String GAMES_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/games";
		public static final String GAME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/game";
		public static final String PLAYERS_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/players";
		public static final String PLAYER_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/player";
		public static final String SCORES_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/scores";
		public static final String SCORE_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/score";
	}

	private BirdieHelper mDatabaseHelper;

	private static UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		mUriMatcher.addURI(AUTHORITY, Paths.GAMES, Endpoints.GAMES);
		mUriMatcher.addURI(AUTHORITY, Paths.GAMES + "/player-count", Endpoints.GAMES_WITH_PLAYER_COUNT);
		mUriMatcher.addURI(AUTHORITY, Paths.GAMES + "/*/hole", Endpoints.HIGHEST_HOLE);
		mUriMatcher.addURI(AUTHORITY, Paths.GAMES + "/*/players", Endpoints.PLAYERS_FOR_GAME);
		mUriMatcher.addURI(AUTHORITY, Paths.GAMES + "/*/players/#", Endpoints.PLAYERS_WITH_HOLE_SCORE);
		mUriMatcher.addURI(AUTHORITY, Paths.GAMES + "/*/*/total", Endpoints.PLAYER_TOTAL_FOR_GAME);
		mUriMatcher.addURI(AUTHORITY, Paths.GAMES + "/*/*/scores", Endpoints.PLAYER_SCORES_FOR_GAME);
		mUriMatcher.addURI(AUTHORITY, Paths.GAMES + "/*", Endpoints.GAME_ID);
		mUriMatcher.addURI(AUTHORITY, Paths.PLAYERS, Endpoints.PLAYERS);
		mUriMatcher.addURI(AUTHORITY, Paths.PLAYERS + "/*", Endpoints.PLAYER_ID);
		mUriMatcher.addURI(AUTHORITY, Paths.SCORES, Endpoints.SCORES);
		mUriMatcher.addURI(AUTHORITY, Paths.SCORES + "/*", Endpoints.SCORE_ID);
		mUriMatcher.addURI(AUTHORITY, Paths.PG_JUNCTION, Endpoints.PG_JUNCTION);
		mUriMatcher.addURI(AUTHORITY, Paths.PG_JUNCTION + "/*", Endpoints.PG_JUNCTION_ID);
		mUriMatcher.addURI(AUTHORITY, Paths.COURSES, Endpoints.COURSES);
		mUriMatcher.addURI(AUTHORITY, Paths.COURSES + "/*", Endpoints.COURSES_ID);
	}

	@Override
	public boolean onCreate() {
		mDatabaseHelper = new BirdieHelper(getContext());
		return false;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
		String where;
		String id;
		String sql;
		String[] args;
		Cursor cursor;
		switch (mUriMatcher.match(uri)) {
			case Endpoints.GAMES:
				queryBuilder.setTables(Games.TABLE);
				break;
			case Endpoints.GAMES_WITH_PLAYER_COUNT:
				sql = "select %s, %s, %s, %s, %s, count(%s) as %s from %s join %s on %s = %s "
					+ "join %s on %s = %s where %s = 1 group by %s order by %s desc";
				args = new String [] {
					Games.TABLE + "." + Games.ID,
					Games.DATE,
					Games.COMPLETE,
					Games.GUID,
					Courses.NAME,
					PlayersToGames.PLAYER_GUID,
					Players.COUNT,
					Games.TABLE,
					PlayersToGames.TABLE,
					Games.GUID,
					PlayersToGames.GAME_GUID,
					Courses.TABLE,
					Games.COURSE_GUID,
					Courses.GUID,
					Games.COMPLETE,
					Games.GUID,
					Games.DATE
				};
				cursor = execSQL(database, sql, args);
				cursor.setNotificationUri(getContext().getContentResolver(), Uris.GAMES);
				return cursor;
			case Endpoints.PLAYERS_FOR_GAME:
				sql = "select %s from %s join %s on %s = %s where %s = '%s' "
					+ "group by %s order by %s;";
				args = new String[] {
					Players.GUID,
					Players.TABLE,
					PlayersToGames.TABLE,
					Players.GUID,
					PlayersToGames.PLAYER_GUID,
					PlayersToGames.GAME_GUID,
					uri.getPathSegments().get(1),
					Players.GUID,
					Players.NAME
				};
				cursor = execSQL(database, sql, args);
				cursor.setNotificationUri(getContext().getContentResolver(), Uris.SCORES);
				return cursor;
			case Endpoints.PLAYERS_WITH_HOLE_SCORE:
				sql = "select %s, %s, %s, %s, %s from %s, %s, %s where %s = %s and %s = %s "
					+ "and %s = %s and %s = '%s' and %s = %s group by %s order by %s";
				args = new String[] {
					Players.TABLE + "." + Players.ID,
					Players.NAME,
					Scores.HOLE,
					Scores.SCORE,
					Scores.GUID,
					Players.TABLE,
					PlayersToGames.TABLE,
					Scores.TABLE,
					Players.GUID,
					PlayersToGames.PLAYER_GUID,
					Players.GUID,
					Scores.PLAYER_GUID,
					PlayersToGames.GAME_GUID,
					Scores.GAME_GUID,
					PlayersToGames.GAME_GUID,
					uri.getPathSegments().get(1),
					Scores.HOLE,
					uri.getLastPathSegment(),
					Players.GUID,
					Players.NAME
				};
				cursor = execSQL(database, sql, args);
				cursor.setNotificationUri(getContext().getContentResolver(), Uris.SCORES);
				return cursor;
			case Endpoints.HIGHEST_HOLE:
				sql = "select max(%s) as %s from %s where %s = '%s'";
				args = new String[] {
					Scores.HOLE,
					Scores.HOLE,
					Scores.TABLE,
					Scores.GAME_GUID,
					uri.getPathSegments().get(1)
				};
				cursor = execSQL(database, sql, args);
				cursor.setNotificationUri(getContext().getContentResolver(), Uris.SCORES);
				return cursor;
			case Endpoints.PLAYER_TOTAL_FOR_GAME:
				sql = "select sum(%s) as %s from %s where %s = '%s' and %s = '%s'";
				args = new String[] {
					Scores.SCORE,
					Scores.SCORE,
					Scores.TABLE,
					Scores.GAME_GUID,
					uri.getPathSegments().get(1),
					Scores.PLAYER_GUID,
					uri.getPathSegments().get(2)
				};
				cursor = execSQL(database, sql, args);
				return cursor;
			case Endpoints.PLAYER_SCORES_FOR_GAME:
				sql = "select %s, %s from %s where %s = '%s' and %s = '%s'";
				args = new String[] {
					Scores.HOLE,
					Scores.SCORE,
					Scores.TABLE,
					Scores.GAME_GUID,
					uri.getPathSegments().get(1),
					Scores.PLAYER_GUID,
					uri.getPathSegments().get(2)
				};
				cursor = execSQL(database, sql, args);
				return cursor;
			case Endpoints.GAME_ID:
				id = uri.getLastPathSegment();
				where = String.format("%s = '%s'", Games.GUID, id);
				queryBuilder.setTables(Games.TABLE);
				queryBuilder.appendWhere(where);
				break;
			case Endpoints.PLAYERS:
				queryBuilder.setTables(Players.TABLE);
				break;
			case Endpoints.PLAYER_ID:
				id = uri.getLastPathSegment();
				where = String.format("%s = '%s'", Players.GUID, id);
				return database.query(Players.TABLE, projection, where, null, null, null, null);
			case Endpoints.SCORES:
				queryBuilder.setTables(Scores.TABLE);
				break;
			case Endpoints.SCORE_ID:
				id = uri.getLastPathSegment();
				queryBuilder.setTables(Scores.TABLE);
				queryBuilder.appendWhere(Scores.GUID + "=" + id);
				break;
			case Endpoints.COURSES:
				queryBuilder.setTables(Courses.TABLE);
				break;
			case Endpoints.COURSES_ID:
				id = uri.getLastPathSegment();
				where = String.format("%s = '%s'", Courses.GUID, id);
				return database.query(Courses.TABLE, projection, where, null, null, null, null);
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		cursor = queryBuilder.query(database, projection, selection,
			selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		String path = null;
		List<Uri> notifyUris = new ArrayList<Uri>();
		notifyUris.add(uri);

		switch (mUriMatcher.match(uri)) {
			case Endpoints.GAMES:
				if (database.insert(Games.TABLE, null, values) > 0) {
					path = Paths.GAMES + "/" + values.get(Games.GUID);
				}
				break;
			case Endpoints.PLAYERS:
				if (database.insert(Players.TABLE, null, values) > 0) {
					path = Paths.PLAYERS + "/" + values.get(Players.GUID);
					notifyUris.add(Uris.SCORES);
				}
				break;
			case Endpoints.SCORES:
				if (database.insert(Scores.TABLE, null, values) > 0) {
					path = Paths.SCORES + "/" + values.get(Scores.GUID);
					notifyUris.add(Uris.PLAYERS);
				}
				break;
			case Endpoints.PG_JUNCTION:
				database.insert(PlayersToGames.TABLE, null, values);
				notifyUris.add(Uris.GAMES);
				break;
			case Endpoints.COURSES:
				if (database.insert(Courses.TABLE, null, values) > 0) {
					path = Paths.COURSES + "/" + values.get(Courses.GUID);
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		notify(notifyUris);
		return Uri.withAppendedPath(Uris.BASE, path);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (mUriMatcher.match(uri)) {
			case Endpoints.GAMES:
				break;
			case Endpoints.GAME_ID:
				break;
			case Endpoints.PLAYERS:
				break;
			case Endpoints.PLAYER_ID:
				break;
			case Endpoints.SCORES:
				break;
			case Endpoints.SCORE_ID:
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		String where;
		String guid = uri.getLastPathSegment();
		List<Uri> notifyUris = new ArrayList<Uri>();

		switch (mUriMatcher.match(uri)) {
			case Endpoints.GAME_ID:
				where = String.format("%s = '%s'", Games.GUID, guid);
				database.update(Games.TABLE, values, where, null);
				break;
			case Endpoints.PLAYER_ID:
				break;
			case Endpoints.SCORE_ID:
				where = String.format("%s = '%s'", Scores.GUID, guid);
				database.update(Scores.TABLE, values, where, null);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		// TODO fix notification scheme
		notifyUris.add(Uris.SCORES);
		notifyUris.add(Uris.GAMES);
		notifyUris.add(Uris.PLAYERS);
		notify(notifyUris);
		return 0;
	}

	private void notify(List<Uri> uris) {
		for (Uri uri : uris) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
	}


	// TODO don't use this method anymore
	private Cursor execSQL(SQLiteDatabase database, String query, Object... formatObjs) {
		return database.rawQuery(String.format(query, formatObjs), null);
	}
}
