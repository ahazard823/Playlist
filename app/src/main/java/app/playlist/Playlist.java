package app.playlist;

import androidx.annotation.NonNull;

public class Playlist {
    private boolean collaborative;
    private String id;
    private String name;
    private boolean isPublic;
    private int size;


    public Playlist(boolean collaborative, String id, String name, boolean isPublic, int size) {
        this.collaborative = collaborative;
        this.id = id;
        this.name = name;
        this.isPublic = isPublic;
        this.size = size;
    }

    public boolean isCollaborative() {
        return collaborative;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean getPublic() {
        return isPublic;
    }

    public int getSize() {return size;}

    @NonNull
    @Override
    public String toString() {
        return name + ":\t" + String.valueOf(size) + " songs";
    }

    public boolean contains(String name) {
        if (this.name.equals(name)) {
            return true;
        }
        return false;
    }
}
