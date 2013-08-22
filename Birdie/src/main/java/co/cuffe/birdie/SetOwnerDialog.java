package co.cuffe.birdie;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class SetOwnerDialog extends DialogFragment {

	private EditText mOwnerName;
	private String mGameGuid;

	public static SetOwnerDialog newInstance(String gameGuid) {
		SetOwnerDialog dialog = new SetOwnerDialog();
		Bundle bundle = new Bundle();
		bundle.putString(Games.GUID, gameGuid);
		dialog.setArguments(bundle);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.play_dialog_set_owner, null);
		mGameGuid = getArguments().getString(Games.GUID);
		mOwnerName = (EditText) rootView.findViewById(R.id.play_dialog_edit_text);

		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.play_dialog_set_owner_title)
			.setView(rootView)
			.setPositiveButton(R.string.play_dialog_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String ownerName = mOwnerName.getText().toString();
					String ownerGuid = Players.create(getActivity(), ownerName);
					getActivity()
						.getPreferences(Context.MODE_PRIVATE)
						.edit()
						.putString("owner_guid", ownerGuid)
						.commit();
					Games.addPlayer(getActivity(), ownerGuid, mGameGuid, 1);
				}
			})
			.create();
	}
}
