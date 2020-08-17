package app.playlist;


import android.content.Intent;

import androidx.annotation.NonNull;

public class Album {
    String artist;
    String id;
    String title;
    int year;
    int month;
    int day;

    public Album (String artist, String id, String title, String date) {
        this.artist = artist;
        this.id = id;
        this.title = title;
        String[] split = date.split("-");
        year = Integer.valueOf(split[0]);
        month = (split.length > 0) ? Integer.valueOf(split[1]) : 0;
        day = (split.length > 1) ? Integer.valueOf(split[2]) : 0;
    }

    @NonNull
    @Override
    public String toString() {
        String ret = (month != 0) ?  month + "/" + year : String.valueOf(year);
        ret = (day != 0) ? day + "/" + ret : ret;
        return ret;
    }
}
