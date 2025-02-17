package app.playlist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PlaylistAdapter extends ArrayAdapter<Playlist> {

    ArrayList<String> selectedPlaylists = new ArrayList<String>();
    public PlaylistAdapter(Context context, ArrayList<Playlist> arrayList) {
        super(context, 0, arrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View currentItemView = convertView;

        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.list,parent,false);
        }

        Playlist currentPosition = getItem(position);

        CheckBox playlist = currentItemView.findViewById(R.id.playlist);
        playlist.setText(currentPosition.getName());

        playlist.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    selectedPlaylists.add(playlist.getText().toString());
                    Log.v("Checked",playlist.getText().toString());
                }
                else {
                    selectedPlaylists.remove(playlist.getText().toString());
                    Log.v("UnChecked",playlist.getText().toString());
                }
            }
        });

        return currentItemView;
    }

    ArrayList<String> getSelectedPlaylists() {return selectedPlaylists;}
}
