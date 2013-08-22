package co.cuffe.birdie;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class PlayersAdapter extends SimpleCursorAdapter {

	private static int LAYOUT = R.layout.player_list_row;
	private static String[] FROM = { Players.NAME };

	private Context mContext;

	public PlayersAdapter(Context context, String gameGuid) {
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
		TextView playerName = (TextView) view.findViewById(R.id.player_row_name);
		TextView playerScore = (TextView) view.findViewById(R.id.player_row_score);
		Button increaseScore = (Button) view.findViewById(R.id.player_increase_score);
		Button decreaseScore = (Button) view.findViewById(R.id.player_decrease_score);

		final String scoreGuid = cursor.getString(cursor.getColumnIndex(Scores.GUID));
		final int holeScore = cursor.getInt(cursor.getColumnIndex(Scores.SCORE));
		String name = cursor.getString(cursor.getColumnIndex(Players.NAME));

		// Disable if current score is 'ace' or 'quintuple bogey'
		if (holeScore == -2) {
			decreaseScore.setEnabled(false);
		} else if (holeScore == 5) {
			increaseScore.setEnabled(false);
		} else {
			decreaseScore.setEnabled(true);
			increaseScore.setEnabled(true);
		}

		increaseScore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Scores.update(mContext, scoreGuid, holeScore + 1);
			}
		});

		decreaseScore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Scores.update(mContext, scoreGuid, holeScore - 1);
			}
		});

		playerName.setText(name);
		playerScore.setText(String.valueOf(holeScore));
	}
}
