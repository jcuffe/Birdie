package co.cuffe.birdie;

public class PlayersToGames {
	public static final String TABLE = "players_to_games";
	public static final String ID = "_id";
	public static final String PLAYER_GUID = "pg_player_guid";
	public static final String GAME_GUID = "pg_game_guid";

	public static final String CREATE = "create table "
		+ TABLE + "("
		+ ID + " integer,"
		+ PLAYER_GUID + " text,"
		+ GAME_GUID + " text,"
		+ "primary key(" + PLAYER_GUID + "," + GAME_GUID + "),"
		+ "foreign key(" + PLAYER_GUID + ") references " + Players.TABLE + ","
		+ "foreign key(" + GAME_GUID + ") references " + Games.TABLE + ");";
}
