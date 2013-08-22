package co.cuffe.birdie;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class PlayFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private PlayersAdapter mAdapter;
	private String mGameGuid;
	private int mCurrentHole;
	private Button mPrevHole;
	private Button mNextHole;
	private TextView mCourseName;
	private TextView mHole;
	private ListView mPlayersList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_play, container, false);

		mCourseName = (TextView) rootView.findViewById(R.id.play_course);
		mHole = (TextView) rootView.findViewById(R.id.play_hole);
		mPlayersList = (ListView) rootView.findViewById(android.R.id.list);
		mNextHole = (Button) rootView.findViewById(R.id.play_next_hole);
		mPrevHole = (Button) rootView.findViewById(R.id.play_prev_hole);

		// Show this fragment's options menu items
		setHasOptionsMenu(true);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if ((mGameGuid = Games.getActiveGameGuid(getActivity())) == null) {
			startGame();
		} else {
			setHole(getHole());
		}

		// Set the course name as soon as the game has a course assigned
		(new SetCourseTask(mCourseName, mGameGuid)).execute();

		// Set enabled status of prev/next hole buttons
		if (mCurrentHole == 1) {
			mPrevHole.setEnabled(false);
		} else if (mCurrentHole == 18) {
			mNextHole.setEnabled(false);
		}

		// Set onclick listeners for prev/next hole
		mPrevHole.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { prevHole(); }
		});
		mNextHole.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { nextHole(); }
		});

		mAdapter = new PlayersAdapter(getActivity(), mGameGuid);

		getLoaderManager().initLoader(0, null, this);
		mPlayersList.setAdapter(mAdapter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.play, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.play_add_player:
				showAddPlayerDialog(false);
				return true;
			case R.id.play_end_game:
				confirmCompleteGame();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		String[] projection = {
			Players.ID,
			Players.GUID,
			Players.NAME
		};
		Uri uri = BirdieProvider.Uris.GAMES.buildUpon()
			.appendPath(mGameGuid)
			.appendPath("players")
			.appendPath(String.valueOf(mCurrentHole))
			.build();
		mCourseName.setText(Games.getCourse(getActivity(), mGameGuid));
		return new CursorLoader(getActivity(), uri,
			projection, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		mAdapter.swapCursor(null);
	}

	// onClick listener for play_next_hole
	public void nextHole() {
		if (mCurrentHole == getHole()) {
			Games.advanceHole(getActivity(), mGameGuid, mCurrentHole);
		}
		setHole(mCurrentHole + 1);

		getLoaderManager().restartLoader(0, null, this);

		if (!mPrevHole.isEnabled()) {
			mPrevHole.setEnabled(true);
		}
		if (mCurrentHole == 18) {
			mNextHole.setEnabled(false);
		}
	}

	// onClick listener for play_prev_hole
	public void prevHole() {
		setHole(mCurrentHole - 1);
		if (mCurrentHole == 1) {
			mPrevHole.setEnabled(false);
		}
		getLoaderManager().restartLoader(0, null, this);
	}

	private void startGame() {
		mGameGuid = Games.create(getActivity());
		setHole(1);
		showCourseDialog();

		SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
		String ownerGuid = preferences.getString("owner_guid", null);
		if (ownerGuid == null) {
			showSetOwnerDialog();
		} else {
			Games.addPlayer(getActivity(), ownerGuid, mGameGuid, mCurrentHole);
		}
	}

	private void showCourseDialog() {
		CourseDialog dialog = CourseDialog.newInstance(mGameGuid);
		dialog.setCancelable(false);
		dialog.show(getChildFragmentManager(), "course");
	}

	private void showAddPlayerDialog(boolean newGame) {
		AddPlayerDialog dialog = AddPlayerDialog.newInstance(mGameGuid, mCurrentHole, newGame);
		if (newGame) {
			dialog.setCancelable(false);
		}
		dialog.show(getChildFragmentManager(), "add");
	}

	private void showSetOwnerDialog() {
		SetOwnerDialog dialog = SetOwnerDialog.newInstance(mGameGuid);
		dialog.setCancelable(false);
		dialog.show(getChildFragmentManager(), "owner");
	}

	private void confirmCompleteGame() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.play_dialog_complete_game_title)
			.setPositiveButton(R.string.play_dialog_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Games.completeGame(getActivity(), mGameGuid);
					GameDetailPager fragment = GameDetailPager.newInstance(mGameGuid);
					getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.slide_in_right, 0)
						.replace(R.id.content_frame, fragment)
						.commit();
					getActivity().setTitle(R.string.title_review_fragment);
				}
			})
			.setNegativeButton(R.string.play_dialog_cancel, null)
			.create()
			.show();
	}

	private void setHole(int hole) {
		if (hole == 0) {
			showAddPlayerDialog(true);
		}
		mCurrentHole = hole;
		mHole.setText(String.valueOf(mCurrentHole));
	}

	private int getHole() {
		return Games.getHole(getActivity(), mGameGuid);
	}

	private class SetCourseTask extends AsyncTask<Void, Void, String> {

		private TextView mCourseName;
		private String mGameGuid;

		public SetCourseTask(TextView courseName, String gameGuid) {
			mCourseName = courseName;
			mGameGuid = gameGuid;
		}

		@Override
		protected String doInBackground(Void... params) {
			String name = null;
			while (name == null) {
				name = Games.getCourse(getActivity(), mGameGuid);
			}
			return name;
		}

		@Override
		protected void onPostExecute(String s) {
			mCourseName.setText(s);
		}
	}
}
