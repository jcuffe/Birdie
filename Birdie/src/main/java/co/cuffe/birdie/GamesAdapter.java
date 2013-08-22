package co.cuffe.birdie;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class GamesAdapter extends SimpleCursorAdapter {

	private static int LAYOUT = R.layout.game_list_row;
	private static String[] FROM = { };

	public GamesAdapter(Context context) {
		super(context, LAYOUT, null, FROM, null, 0);
		mContext = context;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(LAYOUT, parent, false);

		fillView(cursor, view);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		fillView(cursor, view);
	}

	private void fillView(Cursor cursor, View view) {
		TextView gameCourse = (TextView) view.findViewById(R.id.game_row_course);
		TextView gameDate = (TextView) view.findViewById(R.id.game_row_date);
		TextView gamePlayers = (TextView) view.findViewById(R.id.game_row_players);

		long date = cursor.getLong(1);

		// Tag this view so that onClick listener can access guid
		view.setTag(cursor.getString(cursor.getColumnIndex(Games.GUID)));

		gameCourse.setText(cursor.getString(cursor.getColumnIndex(Courses.NAME)));
		gameDate.setText(SimpleDateFormat.getDateInstance().format(date));
		gamePlayers.setText(cursor.getString(cursor.getColumnIndex(Players.COUNT)));
	}
}
