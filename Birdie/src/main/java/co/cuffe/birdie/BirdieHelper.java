package co.cuffe.birdie;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BirdieHelper extends SQLiteOpenHelper {

	private static String DB_NAME = "birdiedb";
	private static int DB_VERSION = 1;

	public BirdieHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Games.CREATE);
		db.execSQL(Players.CREATE);
		db.execSQL(Scores.CREATE);
		db.execSQL(PlayersToGames.CREATE);
		db.execSQL(Courses.CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
}