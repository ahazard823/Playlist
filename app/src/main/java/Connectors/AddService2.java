package Connectors;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.playlist.AddActivity;

public class AddService2 {
    private static final String SEARCH_ENDPOINT = "https://api.spotify.com/v1/search";
    private static final String ARTIST_ENDPOINT = "https://api.spotify.com/v1/artists/";
    private static final String ALBUM_ENDPOINT = "https://api.spotify.com/v1/albums/";
    private static final String PLAYLIST_ENDPOINT = "https://api.spotify.com/v1/playlists/";
    private SharedPreferences msharedPreferences;
    private RequestQueue mqueue;
    private ArrayList<String > playlists = new ArrayList<>();
    private ArrayList<String> toSearch = new ArrayList<>();
    private ArrayList<String> song_URIs = new ArrayList<>();
    private ArrayList<String> album_ids = new ArrayList<>();
    private ArrayList<String> artists = new ArrayList<>();
    private ArrayList<String> album_names = new ArrayList<>();
    private String currentId;
    private int j;
    private Context context;

    public AddService2(RequestQueue queue, SharedPreferences sharedPreferences, ArrayList<String> playlists, Context context) {
        mqueue = queue;
        msharedPreferences = sharedPreferences;
        this.playlists = playlists;
        this.context = context;
        Log.v("add serv", "start");
    }

    public void addArtists(ArrayList<String> toAdd) {
        toSearch.clear();
        song_URIs.clear();
        for (int i = 0; i <toAdd.size(); i++) {
            String artist = toAdd.get(i);
            String q = SEARCH_ENDPOINT + "?q=artist:" + artist.replace(" ", "%20") + "&type=artist";
            toSearch.add(q);
        }
        /*for (int i = 0; i < toSearch.size(); i++) {
            Log.v("i", String.valueOf(i));
            searchArtist(toSearch.get(i), () -> {
                String add_id = getCurrentArtistId();
                Log.v("inside", "inside");
                artists.add(add_id);
            });
        }*/
        searchArtists(toSearch, () -> {
            artists = getArtists();
            logArtists();
            startNext(artists, 0);
        });
        //Log.v("Test alert", String.valueOf(artists.size()));
        //logArtists();
    }

    private String getCurrenttId() {
        Log.v("Current", currentId);
        return currentId;
    }

    private void searchArtist(String search, VolleyCallBack callBack) {
        Log.v("starting", "starting");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, search, null, response -> {
                    JSONObject results = response.optJSONObject("artists");
                    JSONArray jsonArray = results.optJSONArray("items");
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        currentId = jsonObject.getString("id");
                        //artists.add(currentArtistId);
                        callBack.onSuccess();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = msharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        mqueue.add(jsonObjectRequest);
    }

    private void searchArtists(ArrayList<String> tosearch, VolleyCallBack callBack) {
        Log.v("starting", "starting");
        String search;
        //ArrayList<String> ret = new ArrayList<>();
        for (int i = 0; i < tosearch.size(); i++) {
            search = tosearch.get(i);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, search, null, response -> {
                        JSONObject results = response.optJSONObject("artists");
                        JSONArray jsonArray = results.optJSONArray("items");
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            currentId = jsonObject.getString("id");
                            artists.add(currentId);
                            Log.v("id", currentId);
                            //artists.add(currentArtistId);
                            //callBack.onSuccess();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callBack.onSuccess();
                    }, error -> {
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String token = msharedPreferences.getString("token", "");
                    String auth = "Bearer " + token;
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            mqueue.add(jsonObjectRequest);
        }
    }

    public ArrayList<String> getArtists() {
        return artists;
    }

    public void logArtists() {
        //Log.v("fun", "start");
        for (int i = 0; i < artists.size(); i++) {
            Log.v("fun", artists.get(i));
        }
    }

    public void startNext(ArrayList<String> results, int type) {
        Log.v("results", String.valueOf(results.size()));
        Log.v("search", String.valueOf(toSearch.size()));
        if (results.size() == toSearch.size()) {
            switch (type) {
                case 0:
                    Log.v("start", "start");
                    logArtists();
                    getAlbums(results, () -> {
                        Log.v("j", getJ());
                        if (j == results.size()) {
                            Log.v("next", "start");
                            Log.v("size", String.valueOf(album_ids.size()));
                            //logAlbums();
                            addAlbums(album_ids);
                        } else {
                            Log.v("next", "wait");
                        }
                    });
                    break;
                case 1:
                    addAlbums(album_ids);
                    break;
                case 2:
                    splitURI();
            }
        }
        else {
            Log.v("start", "wait");
        }
    }

    private String getJ() {
        return String.valueOf(j);
    }

    private void getAlbums(ArrayList<String> artists, VolleyCallBack callBack) {
        Log.v("album", "start");
        j = 0;
        for (int i = 0; i < artists.size(); i++){
            Log.v("artist", artists.get(i));
            //j++;
            String endpoint = ARTIST_ENDPOINT + artists.get(i) + "/albums?country=US&limit=50&include_groups=album";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, endpoint, null, response -> {
                        JSONArray jsonArray = response.optJSONArray("items");
                        for (int k =0; k <jsonArray.length(); k++){
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(k);
                                String album_id = jsonObject.getString("id");
                                String album_type = jsonObject.getString("album_type");
                                String name = jsonObject.getString("name");
                                //Log.v("Album ID", album_id);
                                //Log.v("Album Type", album_type);
                                //Log.v("Album Name", name);
                                if (!album_names.contains(name)) {
                                    album_ids.add(album_id);
                                    album_names.add(name);
                                }
                                //callBack.onSuccess();
                                //getSongs(album_id, playlists);
                                //Log.v("k", String.valueOf(k));
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        j++;
                        callBack.onSuccess();
                    }, error -> {
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String token = msharedPreferences.getString("token", "");
                    String auth = "Bearer " + token;
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            mqueue.add(jsonObjectRequest);
        }
    }

    private void logAlbums() {
        for (int i=0; i < album_ids.size(); i++) {
            //Log.v("albums", album_ids.get(i));
            Log.v("albums", album_names.get(i));
        }
    }

    private void addAlbums(ArrayList<String> albums) {
        j = 0;
        for (int i = 0; i < albums.size(); i++) {
            String albumId = albums.get(i);
            getSongs(albumId, () -> {
                Log.v("songs", String.valueOf(song_URIs.size()));
                Log.v("j", getJ());
                if (j == albums.size()) {
                    splitURI();
                }
            });
        }
    }

    private void getSongs(String id, VolleyCallBack callBack) {
        String endpoint = ALBUM_ENDPOINT + id + "/tracks";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    JSONArray jsonArray = response.optJSONArray("items");
                    for (int i =0; i <jsonArray.length(); i++){
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String uri = jsonObject.getString("uri");
                            song_URIs.add(uri);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.v("songs added", String.valueOf(jsonArray.length()));
                    j++;
                    callBack.onSuccess();
                }, error -> {
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = msharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        mqueue.add(jsonObjectRequest);
    }

    private void splitURI() {
        int max= song_URIs.size();
        int curr_index = 0;
        //int count = 0;
        //ArrayList<Integer> temp = new ArrayList<Integer>();
        JSONArray uris = new JSONArray();
        Log.v("max", String.valueOf(max));
        String text = "Adding " + String.valueOf(max) + " song(s)";
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
        while (curr_index < max) {
            if (uris.length() == 75) {
                addSongs(uris);
                uris = new JSONArray();
            }
            uris.put(song_URIs.get(curr_index));
            curr_index++;
        }
        if (uris.length() > 0) {
            addSongs(uris);
        }

    }

    private void addSongs(JSONArray uris) {
        Log.v("songs", "adding");
        JSONObject body = new JSONObject();
        try {
            body.put("uris", uris);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < playlists.size(); i++) {
            //String play_id = msharedPreferences.getString(playlists.get(i), "err");
            String play_id = playlists.get(i);
            String endpoint = PLAYLIST_ENDPOINT + play_id + "/tracks";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, endpoint, body, response -> {
                    }, error -> {
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String token = msharedPreferences.getString("token", "");
                    String auth = "Bearer " + token;
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            mqueue.add(jsonObjectRequest);
        }
    }

    public void albumAdder (ArrayList<String> toAdd) {
        toSearch.clear();
        song_URIs.clear();
        for (int i = 0; i <toAdd.size(); i++) {
            String artist = null;
            String curr = toAdd.get(i);
            String[] split = curr.split(" \\( | \\(|\\( |\\(");
            String album = split[0];
            if (split.length > 1) {
                artist = split[1];
            }
            String q = SEARCH_ENDPOINT + "?q=album:" + album.replace(" ", "%20");
            if (artist != null && !artist.equals("")) {
                q += "%20artist:" + artist.replace(" ", "%20");
                //Log.v("artist", artist);
            }
            q += "&type=album";
            toSearch.add(q);
        }
        searchAlbums(toSearch, () -> {
            album_ids = getAlbumIds();
            //logArtists();
            startNext(album_ids, 1);
        });
    }

    private ArrayList<String> getAlbumIds() {
        return album_ids;
    }

    private void searchAlbums (ArrayList<String> toSearch, VolleyCallBack callBack) {
        Log.v("starting", "starting");
        String search;
        //ArrayList<String> ret = new ArrayList<>();
        for (int i = 0; i < toSearch.size(); i++) {
            search = toSearch.get(i);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, search, null, response -> {
                        JSONObject results = response.optJSONObject("albums");
                        JSONArray jsonArray = results.optJSONArray("items");
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            currentId = jsonObject.getString("id");
                            album_ids.add(currentId);
                            Log.v("id", currentId);
                            //artists.add(currentArtistId);
                            //callBack.onSuccess();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callBack.onSuccess();
                    }, error -> {
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String token = msharedPreferences.getString("token", "");
                    String auth = "Bearer " + token;
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            mqueue.add(jsonObjectRequest);
        }
    }

    public void songAdder(ArrayList<String> toAdd) {
        toSearch.clear();
        song_URIs.clear();
        for(int i = 0; i < toAdd.size(); i++) {
            String artist = null;
            String album = null;
            String curr = toAdd.get(i);
            String[] split = curr.split(" \\( | \\(|\\( |\\(");
            String song = split[0];
            if (split.length > 1) {
                String[] split2 = split[1].split(" ; | ;|; |;");
                artist = split2[0];
                if (split2.length > 1) {
                    album = split2[1];
                }
            }
            String q = SEARCH_ENDPOINT + "?q=track:" + song.replace(" ", "%20");
            Log.v("song", song);
            if (artist != null && !artist.equals("")) {
                q += "%20artist:" + artist.replace(" ", "%20");
                //Log.v("artist", artist);
            }
            if (album != null && !album.equals("")) {
                q += "%20album:" + album.replace(" ", "%20");
                //Log.v("album", album);
            }
            q += "&type=track";
            //Log.v("query", q);
            toSearch.add(q);
        }
        searchSongs(toSearch, () -> {
            song_URIs = getSong_URIs();
            //logArtists();
            startNext(song_URIs, 2);
        });
    }

    private ArrayList<String> getSong_URIs () {
        return song_URIs;
    }

    private void searchSongs(ArrayList<String> toSearch, VolleyCallBack callBack) {
        Log.v("starting", "starting");
        String search;
        //ArrayList<String> ret = new ArrayList<>();
        for (int i = 0; i < toSearch.size(); i++) {
            search = toSearch.get(i);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, search, null, response -> {
                        JSONObject results = response.optJSONObject("tracks");
                        JSONArray jsonArray = results.optJSONArray("items");
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            currentId = jsonObject.getString("uri");
                            song_URIs.add(currentId);
                            Log.v("uri", currentId);
                            //artists.add(currentArtistId);
                            //callBack.onSuccess();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callBack.onSuccess();
                    }, error -> {
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String token = msharedPreferences.getString("token", "");
                    String auth = "Bearer " + token;
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            mqueue.add(jsonObjectRequest);
        }
    }
}
