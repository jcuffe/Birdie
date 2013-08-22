package co.cuffe.birdie;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class GameDetailPager extends Fragment {

	private ViewPager mViewPager;

	public static GameDetailPager newInstance(String gameGuid) {
		GameDetailPager fragment = new GameDetailPager();
		Bundle bundle = new Bundle();
		bundle.putString(Games.GUID, gameGuid);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_game_detail_pager, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		String gameGuid = getArguments().getString(Games.GUID);
		List<String> playerGuids = getPlayerGuids(gameGuid);

		mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
		mViewPager.setAdapter(new MyAdapter(getChildFragmentManager(), gameGuid, playerGuids));
	}

	private List<String> getPlayerGuids(String gameGuid) {
		// TODO move this function to a helper class
		Uri uri = BirdieProvider.Uris.GAMES.buildUpon()
			.appendPath(gameGuid)
			.appendPath("players")
			.build();
		String[] projection = { Players.GUID};
		Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
		if (cursor.getCount() > 0) {
			List<String> guids = new ArrayList();
			cursor.moveToPosition(-1);
			while (cursor.moveToNext()) {
				guids.add(cursor.getString(cursor.getColumnIndex(Players.GUID)));
			}
			cursor.close();
			return guids;
		} else {
			return null;
		}
	}

	private class MyAdapter extends FragmentPagerAdapter {
		private String mGameGuid;
		private List<String> mPlayerGuids;

		public MyAdapter(FragmentManager fm, String gameGuid, List<String> playerGuids) {
			super(fm);
			mGameGuid = gameGuid;
			mPlayerGuids = playerGuids;
		}

		@Override
		public int getCount() {
			return mPlayerGuids.size();
		}

		@Override
		public Fragment getItem(int position) {
			return GameDetailFragment.newInstance(mGameGuid, mPlayerGuids.get(position));
		}
	}
}
