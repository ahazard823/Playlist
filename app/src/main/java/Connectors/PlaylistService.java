package Connectors;

import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.spotify.protocol.mappers.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.playlist.Playlist;

public class PlaylistService {
    private static final String ENDPOINT = "https://api.spotify.com/v1/me/playlists?limit=50";
    private SharedPreferences msharedPreferences;
    private RequestQueue mqueue;
    private ArrayList<Playlist> playlists = new ArrayList<>();

    public PlaylistService(RequestQueue queue, SharedPreferences sharedPreferences) {
        mqueue = queue;
        msharedPreferences = sharedPreferences;
    }

    public ArrayList<Playlist> getPlaylists() {
        return playlists;
    }

    public ArrayList<Playlist> get(final VolleyCallBack callBack) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, ENDPOINT, null, response -> {
                    Gson gson = new Gson();
                    JSONArray jsonArray = response.optJSONArray("items");
                    int total = response.optInt("total");
                    Log.v("total", String.valueOf(total));
                    Log.v("total", String.valueOf(jsonArray.length()));
                    for (int n = 0; n < jsonArray.length(); n++) {
                        try {
                            //Log.v("n", String.valueOf(n));
                            //Log.v("test", jsonArray.getJSONObject(n).toString());
                            JSONObject object = jsonArray.getJSONObject(n);
                            boolean collab = object.getBoolean("collaborative");
                            //Log.v("Collab", String.valueOf(collab));
                            String id = object.getString("id");
                            //Log.v("Id", id);
                            String name = object.getString("name");
                            //Log.v("Name", name);
                            boolean isPublic = object.getBoolean("public");
                            //Log.v("Public", String.valueOf(isPublic));
                            JSONObject user_obj = object.getJSONObject("owner");
                            String user = user_obj.getString("id");
                            String log_user = msharedPreferences.getString("userid", "username");
                            JSONObject tracks = object.getJSONObject("tracks");
                            int size = tracks.getInt("total");
                            //Log.v("logged user", log_user);
                            //Log.v("User Test", user);
                            //Log.v("collab", String.valueOf(collab));
                            //Log.v("comparison", String.valueOf(user == log_user));
                            //Log.v("comparison 2", String.valueOf(user.equals(log_user)));
                            if (collab || user.equals(log_user)) {
                                //Log.v("User Test 2", user);
                                Playlist playlist = new Playlist(collab, id, name, isPublic, size);
                                playlists.add(playlist);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    callBack.onSuccess();
                }, error -> {
                    // TODO: Handle error

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
        return playlists;
    }

    public void create(String name) {
        JSONObject toAdd = new JSONObject();
        try {
            toAdd.put("name", name);
            toAdd.put("public", false);
            toAdd.put("collaborative", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ENDPOINT, toAdd, response -> {
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
