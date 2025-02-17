package app.playlist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Connectors.PlaylistService;

public class PlaylistActivity extends AppCompatActivity {
    String user;
    String pass;
    TextView log_user;
    TextView log_pass;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private RequestQueue queue;
    ArrayList<Playlist> playlists;
    ArrayList<String> play_names = new ArrayList<>();
    PlaylistService playlistService;
    Playlist playlist;
    ListView list;
    private static int numPlaylists = 0;
    //PlaylistAdapter adapter;// = new PlaylistAdapter(this, playlists);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        //waitForPlaylists();
        log_user = (TextView) findViewById(R.id.logged_user);
        list = (ListView) findViewById(R.id.list);

        sharedPreferences = getSharedPreferences("SPOTIFY", MODE_PRIVATE);
        queue = Volley.newRequestQueue(this);

        playlistService = new PlaylistService(queue, sharedPreferences);
        //adapter = new PlaylistAdapter(this, playlists);
        Log.v("test", sharedPreferences.getString("userid", "username"));
        //waitForPlaylists();
        user = sharedPreferences.getString("userid", "username") + "'s Playlists";
        //pass = sharedPreferences.getString("playlist", "playlist");
        log_user.setText(user);
        //log_pass.setText(pass);
        waitForPlaylists(0);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public static void setNumPlaylists(int n) {
        numPlaylists = n;
    }
    public static int getNumPlaylists() {return numPlaylists;}

    private void waitForPlaylists(int offset) {
        /*if (playlists != null) {
            playlists.clear();
            play_names.clear();
        }*/
        playlistService.get(offset, () -> {
            playlists = playlistService.getPlaylists();
            //playlists = playlist;
            updatePlaylists(offset);
            //Log.v("First name", playlists.get(7).getName());

        });
        Log.v("done", "waiting");

    }

    private void updatePlaylists (int offset) {
        if (playlists.size() > 0) {
            if (playlists.size() != numPlaylists && (numPlaylists - playlists.size() < 50)) {
                Log.v("done", "offset" + offset);
                waitForPlaylists(offset + 50);
            }
            //log_pass.setText(playlists.get(7).getName());
            else {
                //ArrayAdapter adapter = new ArrayAdapter(this, R.layout.list, playlists);
                PlaylistAdapter adapter = new PlaylistAdapter(this, playlists);
                list.setAdapter(adapter);
                //playlist = playlists.get(7);
                for (int n = 0; n < playlists.size(); n++) {
                    Playlist current = playlists.get(n);
                    play_names.add(n, current.getName());
                    //Log.v("All playlists",current.getName());
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString(current.getName(), current.getId());
                    editor.commit();
                }
            }
        }
    }

    public void next(View view) {
        //Log.v("Test create", "before");
        //playlistService.create("test");
        //Log.v("Test create", "after");
        EditText chosen = (EditText) findViewById(R.id.toedit);
        String unedited = chosen.getText().toString();
        chosen.setText("");
        String[] edited = unedited.split(",  |, |,");
        ArrayList<String> toedit = new ArrayList<>(Arrays.asList(edited));
        PlaylistAdapter adapter = (PlaylistAdapter) list.getAdapter();
        ArrayList<String> checked = adapter.getSelectedPlaylists();
        for (int i = 0; i < toedit.size(); i++) {
            Log.v("testing", "iteration " + i);
            if (toedit.get(i).equals("")) {
                Log.v("testing", "no text entered " + i);
                toedit.remove(i);
            }
        }
        toedit.addAll(checked);
        //for (int i = 0; i < checked.size(); i++){
        //    toedit.add(checked.get(i));
        //}
        if (toedit.size() == 0) {
            Toast toast = Toast.makeText(this, "Please Enter or Select a Playlist", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        for (int i = 0; i < toedit.size(); i++) {
            Log.v("pre", toedit.get(i));
            Log.v("test contains", String.valueOf(play_names.contains(toedit.get(i))));
            Log.v("test part 2", sharedPreferences.getString(toedit.get(i), "bad"));
            Log.v("play size", String.valueOf(playlists.size()));
            if (!play_names.contains(toedit.get(i))) {
                playlistService.create(toedit.get(i));
                Log.v("created", toedit.get(i));
                //waitForPlaylists();
                //updatePlaylists();
            }
        }
        //waitForPlaylists();
        Intent intent = new Intent(this, AddActivity.class);
        intent.putStringArrayListExtra("playlists", toedit);
        Log.v("moving", "on");
        startActivity(intent);
    }

}
