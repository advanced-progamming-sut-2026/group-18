package com.compileordie.pvz2.models.user;

import com.badlogic.gdx.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class News {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    public String tile;
    public String details;
    public long datetime;
    public boolean isRead;

    public News() {
    }

    public News(String tile, String details, long datetime) {
        this.tile = tile;
        this.details = details;
        this.datetime = datetime;
        this.isRead = false;
    }

    public News(String tile, String details) {
        this(tile, details, TimeUtils.millis());
    }

    public News(String details) {
        this(null, details);
    }

    public String datetimeToString() {
        return DATE_FORMATTER.format(new Date(datetime));
    }

    @Override
    public String toString() {
        if (tile != null) {
            return tile + System.lineSeparator() + details + System.lineSeparator() + datetimeToString();
        } else {
            return details + System.lineSeparator() + datetimeToString();
        }
    }
}
