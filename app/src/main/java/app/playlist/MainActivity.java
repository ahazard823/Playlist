package app.playlist;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.util.ArrayList;

import Connectors.PlaylistService;
import Connectors.UserService;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;

    private RequestQueue queue;

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "playlist://callback";
    private static final String CLIENT_ID = "ae6cbf25b5944dd49178e8ed2a863eed";
    private static final String SCOPES = "user-read-recently-played,user-library-modify,user-read-email,user-read-private,playlist-read-private,playlist-read-collaborative,playlist-modify-public,playlist-modify-private";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login();

        msharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(this);
    }

    public void login() {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{SCOPES});
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
        //AuthorizationClient.openLoginInBrowser(this, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    editor.apply();
                    //Intent newintent = new Intent(MainActivity.this, PlaylistActivity.class);
                    waitForUserInfo();
                    //waitForPlaylists();
                    startMainActivity();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Toast toast = Toast.makeText(this,response.getError(), Toast.LENGTH_LONG);
                    toast.show();
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }


    private void waitForUserInfo() {
        UserService userService = new UserService(queue, msharedPreferences);
        userService.get(() -> {
            User user = userService.getUser();
            editor = getSharedPreferences("SPOTIFY", 0).edit();
            editor.putString("userid", user.id);
            // We use commit instead of apply because we need the information stored immediately
            editor.commit();
            //startMainActivity();
        });
    }

    /*private void waitForPlaylists() {
        PlaylistService playlistService = new PlaylistService(queue, msharedPreferences);
        playlistService.get(() -> {
            ArrayList<Playlist> playlists = playlistService.getPlaylists();
            editor = getSharedPreferences("SPOTIFY", 0).edit();
            Log.v("First name", playlists.get(1).getName());
            editor.putString("playlist", playlists.get(1).getName());
            editor.commit();
        });

    }*/

    private void startMainActivity() {
        Intent newintent = new Intent(MainActivity.this, PlaylistActivity.class);
        startActivity(newintent);
    }
}
