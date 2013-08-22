package co.cuffe.birdie;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class GamesListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private GamesAdapter mAdapter;
	private ListView mGamesList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_games_list, container, false);
		mGamesList = (ListView) rootView.findViewById(android.R.id.list);
		mAdapter = new GamesAdapter(getActivity());

		getLoaderManager().initLoader(1, null, this);
		mGamesList.setAdapter(mAdapter);
		mGamesList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				GameDetailPager fragment = GameDetailPager.newInstance((String) view.getTag());
				getFragmentManager().beginTransaction()
					.addToBackStack(null)
					.replace(R.id.content_frame, fragment)
					.commit();
			}
		});
		return rootView;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		Uri uri = Uri.withAppendedPath(BirdieProvider.Uris.GAMES, "player-count");
		return new CursorLoader(getActivity(), uri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		mAdapter.swapCursor(null);
	}
}
