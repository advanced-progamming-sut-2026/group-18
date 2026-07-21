package com.compileordie.pvz2.models.missions;

import com.badlogic.gdx.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class News {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    public String title;
    public String details;
    public long datetime;
    public boolean isRead;

    public News() {
    }

    public News(String title, String details, long datetime) {
        this.title = title;
        this.details = details;
        this.datetime = datetime;
        this.isRead = false;
    }

    public News(String title, String details) {
        this(title, details, TimeUtils.millis());
    }

    public News(String details) {
        this(null, details);
    }

    public String datetimeToString() {
        return DATE_FORMATTER.format(new Date(datetime));
    }

    @Override
    public String toString() {
        if (title != null) {
            return title + System.lineSeparator() + details + System.lineSeparator() + datetimeToString();
        } else {
            return details + System.lineSeparator() + datetimeToString();
        }
    }
}
