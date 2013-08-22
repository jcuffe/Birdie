package co.cuffe.birdie;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

public class CourseDialog extends DialogFragment {

	final static int[] to = new int[] { android.R.id.text1 };
	final static String[] from = new String[] { Courses.NAME };

	private AutoCompleteTextView mCourseName;
	private String mGameGuid;

	static CourseDialog newInstance(String gameGuid) {
		CourseDialog dialog = new CourseDialog();
		Bundle bundle = new Bundle();
		bundle.putString(Games.GUID, gameGuid);
		dialog.setArguments(bundle);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.play_dialog_add_data, null);
		mCourseName = (AutoCompleteTextView) rootView.findViewById(R.id.play_dialog_auto_text);
		mGameGuid = getArguments().getString(Games.GUID);

		// Create a SimpleCursorAdapter for the State Name field.
		SimpleCursorAdapter adapter =
			new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line, null,
				from, to, 0);
		mCourseName.setAdapter(adapter);

		// Set an OnItemClickListener, to update dependent fields when
		// a choice is made in the AutoCompleteTextView.
		mCourseName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				// Get the cursor, positioned to the corresponding row in the
				// result set
				Cursor cursor = (Cursor) listView.getItemAtPosition(position);

				mCourseName.setText(cursor.getString(cursor.getColumnIndex(Courses.NAME)));
			}
		});

		// Set the CursorToStringConverter, to provide the labels for the
		// choices to be displayed in the AutoCompleteTextView.
		adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
			public String convertToString(android.database.Cursor cursor) {
				return cursor.getString(cursor.getColumnIndex(Courses.NAME));
			}
		});

		// Set the FilterQueryProvider, to run queries for choices
		// that match the specified input.
		adapter.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				String[] projection = {
					Courses.ID,
					Courses.NAME
				};
				String where = String.format("%s like '%%%s%%'", Courses.NAME, constraint);
				return getActivity().getContentResolver().query(BirdieProvider.Uris.COURSES,
					projection, where, null, null);
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(rootView)
			.setTitle(R.string.play_dialog_enter_course_title)
			.setPositiveButton(R.string.play_dialog_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String location = mCourseName.getText().toString();
					String guid;
					if ((guid = Courses.findByName(getActivity(), location)) == null) {
						guid = Courses.create(getActivity(), location);
					}
					Games.setCourse(getActivity(), mGameGuid, guid);
				}
			});

		return builder.create();
	}
}
