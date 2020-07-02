package Connectors;

import android.content.SharedPreferences;
import android.util.Log;

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

public class AddService {
    private static final String SEARCH_ENDPOINT = "https://api.spotify.com/v1/search";
    private static final String ARTIST_ENDPOINT = "https://api.spotify.com/v1/artists/";
    private static final String ALBUM_ENDPOINT = "https://api.spotify.com/v1/albums/";
    private static final String PLAYLIST_ENDPOINT = "https://api.spotify.com/v1/playlists/";
    private SharedPreferences msharedPreferences;
    private RequestQueue mqueue;
    private ArrayList<String> toSearch = new ArrayList<>();
    private ArrayList<String> song_URIs = new ArrayList<>();
    private ArrayList<String> album_ids = new ArrayList<>();
    private ArrayList<String> artists = new ArrayList<>();
    private ArrayList<String> album_names = new ArrayList<>();

    public AddService(RequestQueue queue, SharedPreferences sharedPreferences) {
        mqueue = queue;
        msharedPreferences = sharedPreferences;
    }

    public void songSearch(ArrayList<String> toAdd, ArrayList<String> playlists) {
        toSearch.clear();
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
                Log.v("artist", artist);
            }
            if (album != null && !album.equals("")) {
                q += "%20album:" + album.replace(" ", "%20");
                Log.v("album", album);
            }
            q += "&type=track";
            Log.v("query", q);
            toSearch.add(q);
            //getURI(toSearch);
        }
        getURI(toSearch, playlists);
        //Log.v("test uris", song_URIs.get(0));
    }

    private void getURI(ArrayList<String> toSearch, ArrayList<String> playlists) {
        String search = "";
        song_URIs.clear();
        for (int i=0; i < toSearch.size(); i++) {
            search = toSearch.get(i);
            Log.v("search url", search);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, search, null, response -> {
                        JSONObject results = response.optJSONObject("tracks");
                        JSONArray jsonArray = results.optJSONArray("items");
                        /*if (jsonArray == null) {
                            Log.v("err", "err");
                        }
                        else {
                            Log.v("err", "no err");
                        }*/
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String uri = jsonObject.getString("uri");
                            //Log.v("uri", uri);
                            addSong(uri, playlists);
                            //song_URIs.add(uri);
                            //Log.v("test 2", song_URIs.get(0));
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
    }

    private void addSong(String uri, ArrayList<String> playlists) {
        String endpoint = PLAYLIST_ENDPOINT;
        for (int i = 0; i < playlists.size(); i++) {
            //Log.v("playlists", playlists.get(i));
            String play_id = msharedPreferences.getString(playlists.get(i), "err");
            //Log.v("playlists id", play_id);
            endpoint += play_id + "/tracks?uris=" + uri;
            //Log.v("endpoint", endpoint);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, endpoint, null, response -> {
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

    public void albumSearch(ArrayList<String> toAdd, ArrayList<String> playlists) {
        toSearch.clear();
        for(int i = 0; i < toAdd.size(); i++) {
            String artist = null;
            String curr = toAdd.get(i);
            String[] split = curr.split(" \\( | \\(|\\( |\\(");
            String album = split[0];
            if (split.length > 1) {
                artist = split[1];
            }
            String q = SEARCH_ENDPOINT + "?q=album:" + album.replace(" ", "%20");
            Log.v("album", album);
            if (artist != null && !artist.equals("")) {
                q += "%20artist:" + artist.replace(" ", "%20");
                Log.v("artist", artist);
            }
            q += "&type=album";
            Log.v("query", q);
            toSearch.add(q);
            //getURI(toSearch);
        }
        addAlbums(toSearch, playlists);
        //Log.v("test uris", song_URIs.get(0));
    }

    private void addAlbums(ArrayList<String> toSearch, ArrayList<String> playlists) {
        String search = "";
        album_ids.clear();
        song_URIs.clear();
        for (int i = 0; i < toSearch.size(); i++) {
            search = toSearch.get(i);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, search, null, response -> {
                        JSONObject results = response.optJSONObject("albums");
                        JSONArray jsonArray = results.optJSONArray("items");
                        /*if (jsonArray == null) {
                            Log.v("err", "err");
                        }
                        else {
                            Log.v("err", "no err");
                        }*/
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String id = jsonObject.getString("id");
                            Log.v("id", id);
                            getSongs(id, playlists);
                            //album_ids.add(id);
                            Log.v("test 2", String.valueOf(song_URIs.size()));
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
        for (int i = 0; i < album_ids.size(); i++) {
            Log.v("albums", album_ids.get(i));
        }
    }

    private void getSongs(String id, ArrayList<String> playlists) {
        String endpoint = ALBUM_ENDPOINT + id + "/tracks";
        ArrayList<String> uris = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    JSONArray jsonArray = response.optJSONArray("items");
                    for (int i =0; i <jsonArray.length(); i++){
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String uri = jsonObject.getString("uri");
                            //Log.v("URI", uri);
                            //uris.add(uri);
                            addSong(uri, playlists);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
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
        //return uris;
    }

    public void artistSearch(ArrayList<String> toAdd, ArrayList<String> playlists) {
        toSearch.clear();
        for (int i = 0; i <toAdd.size(); i++) {
            String artist = toAdd.get(i);
            String q = SEARCH_ENDPOINT + "?q=artist:" + artist.replace(" ", "%20") + "&type=artist";
            toSearch.add(q);
        }
        addArtist(toSearch, playlists);
    }

    private void addArtist (ArrayList<String> toSearch, ArrayList<String> playlists) {
        String search = "";
        album_ids.clear();
        song_URIs.clear();
        artists.clear();
        for (int i = 0; i < toSearch.size(); i++) {
            search = toSearch.get(i);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, search, null, response -> {
                        JSONObject results = response.optJSONObject("artists");
                        JSONArray jsonArray = results.optJSONArray("items");
                        /*if (jsonArray == null) {
                            Log.v("err", "err");
                        }
                        else {
                            Log.v("err", "no err");
                        }*/
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String id = jsonObject.getString("id");
                            Log.v("artist id", id);
                            getAlbums(id, playlists);
                            //album_ids.add(id);
                            //Log.v("test 2", String.valueOf(song_URIs.size()));
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
    }

    private void getAlbums(String id, ArrayList<String> playlists) {
        String endpoint = ARTIST_ENDPOINT + id + "/albums?country=US&limit=50&include_groups=album";
        album_names.clear();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    JSONArray jsonArray = response.optJSONArray("items");
                    for (int i =0; i <jsonArray.length(); i++){
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
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
                            //getSongs(album_id, playlists);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.v("testing", String.valueOf(album_ids.size()));
                    for (int i=0; i<album_names.size();i++) {
                        Log.v("album",album_names.get(i));
                        getSongs(album_ids.get(i), playlists);
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

}
