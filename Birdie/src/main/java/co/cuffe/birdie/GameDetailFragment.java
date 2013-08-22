package co.cuffe.birdie;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class GameDetailFragment extends Fragment {

	private String mGameGuid;
	private String mPlayerGuid;

	public static GameDetailFragment newInstance(String gameGuid, String playerGuid) {
		GameDetailFragment fragment = new GameDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Games.GUID, gameGuid);
		bundle.putString(Players.GUID, playerGuid);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mGameGuid = getArguments().getString(Games.GUID);
		mPlayerGuid = getArguments().getString(Players.GUID);
		return inflater.inflate(R.layout.fragment_game_detail, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		TextView name = (TextView) view.findViewById(R.id.detail_player_name);
		TextView total = (TextView) view.findViewById(R.id.detail_player_total);
		GridView scoreGrid = (GridView) view.findViewById(R.id.detail_score_grid);

		name.setText(Players.findByGuid(getActivity(), mPlayerGuid));
		total.setText(String.valueOf(
			Scores.getTotalForGame(getActivity(), mPlayerGuid, mGameGuid)));
		scoreGrid.setAdapter(new HashMapAdapter(
			Scores.getScoresForGame(getActivity(), mPlayerGuid, mGameGuid)));
	}

	public class HashMapAdapter extends BaseAdapter {
		private Map<Integer, Integer> mData = new HashMap();

		public HashMapAdapter(Map<Integer, Integer> data){
			mData = data;
		}

		@Override
		public int getCount() {
			return 18;
		}

		@Override
		public Object getItem(int position) {
			// Dataset is indexed at 1
			return mData.get(position + 1);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}



		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			TextView tv;
			int[] scoreColors = {
				getResources().getColor(R.color.score_ace),
				getResources().getColor(R.color.score_birdie),
				getResources().getColor(R.color.score_par),
				getResources().getColor(R.color.score_bogey)
			};

			if (convertView == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				tv = (TextView) inflater.inflate(R.layout.score_grid_item, null);
			} else {
				tv = (TextView) convertView;
			}

			if (getItem(pos) == null) {
				tv.setText("-");
			} else {
				int score = (Integer) getItem(pos);
				if (score < 2) {
					tv.setBackgroundColor(scoreColors[score + 2]);
				}
				tv.setText(String.valueOf(getItem(pos)));
			}

			return tv;
		}
	}
}
