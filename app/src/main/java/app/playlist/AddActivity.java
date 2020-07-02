package app.playlist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;

import Connectors.AddService;
import Connectors.AddService2;
import Connectors.PlaylistService;

public class AddActivity extends AppCompatActivity {
    ArrayList<String> playlist_names;
    ArrayList<Playlist> playlists;
    ArrayList<String> play_ids;
    ArrayList<String> toAdd;
    AddService addService;
    AddService2 addService2;
    PlaylistService playlistService;
    private SharedPreferences sharedPreferences;
    private RequestQueue queue;
    EditText artist_list;
    EditText album_list;
    EditText song_list;
    TextView editing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        toAdd = new ArrayList<>();
        artist_list = (EditText) findViewById(R.id.artists);
        album_list = (EditText) findViewById(R.id.albums);
        song_list =(EditText) findViewById(R.id.songs);
        play_ids = new ArrayList<>();
        playlist_names = getIntent().getStringArrayListExtra("playlists");
        editing = (TextView) findViewById(R.id.playlists);
        editing.setText(playlist_names.toString().substring(1,playlist_names.toString().length()-1));
        for (int i = 0; i < playlist_names.size(); i++) {
            Log.v("post", playlist_names.get(i));
        }
        sharedPreferences = getSharedPreferences("SPOTIFY", MODE_PRIVATE);
        queue = Volley.newRequestQueue(this);
        //addService = new AddService(queue, sharedPreferences);
        playlistService = new PlaylistService(queue, sharedPreferences);
        waitForPlaylists();
        //addService2 = new AddService2(queue, sharedPreferences, playlist_names);
        //test = new ArrayList<>();
        //test.add("thnks fr th mmrs");
        //test.add("i write sins");
        //test.add("infinity on high");
        //test.add("from under the cork tree");
        //test.add("Fall Out Boy");
        //test.add("Rise Against");
        //test.add("New Found Glory");
        //test.add("greatest hits (Foo Fighters");
        //test.add("feel good drag (;Never take friendship");
        //test.add("addicted (lit");
        //addService.artistSearch(test, playlists);
        //addService2.addArtists(test);
        //addService2.albumAdder(test);
        //addService2.songAdder(test);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //list.setAdapter(adapter);
        addService2 = new AddService2(queue, sharedPreferences, play_ids, this);
        //addService2.addArtists(test);
    }

    private void waitForPlaylists() {
        if (playlists != null) {
            playlists.clear();
        }
        playlistService.get(() -> {
            playlists = playlistService.getPlaylists();
            //playlists = playlist;
            updatePlaylists();
            //Log.v("First name", playlists.get(7).getName());

        });
        Log.v("done", "waiting");

    }

    private void updatePlaylists () {
        if (playlists.size() > 0) {
            //log_pass.setText(playlists.get(7).getName());
            //ArrayAdapter adapter = new ArrayAdapter(this, R.layout.list, playlists);
            //list.setAdapter(adapter);
            //playlist = playlists.get(7);
            for (int n=0; n < playlists.size(); n++) {
                Playlist current = playlists.get(n);
                if (playlist_names.contains(current.getName())) {
                    Log.v("name check", current.getName());
                    play_ids.add(current.getId());
                }
                //play_names.add(n, current.getName());
                //editor = getSharedPreferences("SPOTIFY", 0).edit();
                //editor.putString(current.getName(), current.getId());
                //editor.commit();
            }
        }
    }

    public void addArtists(View view) {
        toAdd.clear();
        String unedited = artist_list.getText().toString();
        String[] edited = unedited.split(",  |, |,");
        for (String artist: edited) {
            Log.v("adding", artist);
            toAdd.add(artist);
        }
        Log.v("adding", "done");
        artist_list.setText("");
        addService2.addArtists(toAdd);
    }

    public void addAlbums(View view) {
        toAdd.clear();
        String unedited = album_list.getText().toString();
        String[] edited = unedited.split("\\) , |\\) ,|\\), |\\),|\\) |\\)");
        for (String album : edited) {
            Log.v("adding", album);
            toAdd.add(album);
        }
        album_list.setText("");
        addService2.albumAdder(toAdd);
    }

    public void addSongs(View view) {
        toAdd.clear();
        String unedited = song_list.getText().toString();
        String[] edited = unedited.split("\\) , |\\) ,|\\), |\\),|\\) |\\)");
        for (String song : edited) {
            Log.v("adding", song);
            toAdd.add(song);
        }
        song_list.setText("");
        addService2.songAdder(toAdd);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void changePlaylists(View view) {
        onBackPressed();
    }
}
