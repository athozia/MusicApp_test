package tam.musicplayer;

/**
 * Created by Tam on 5/24/2017.
 */

public class Track {

    private long id;
    private String title;
    private String artist;
    private String album;
    private String description;

    public Track(long id, String title, String artist, String album, String description)
    {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.description = description;
    }

    public long getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getAlbum()
    {
        return album;
    }

    public String getArtist()
    {
        return artist;
    }

    public String getDescription()
    {
        return description;
    }
}
