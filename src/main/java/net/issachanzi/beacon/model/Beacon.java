package net.issachanzi.beacon.model;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.json.JsonObject;
import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.model.AccessType;
import net.issachanzi.resteasy.model.EasyModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class Beacon extends EasyModel {
    public static final ZoneOffset TIME_ZONE_OFFSET
        = ZoneOffset.ofTotalSeconds((
            TimeZone.getTimeZone("Australia/Melbourne")
                .getOffset(System.currentTimeMillis())
        ) / 1000);

    public User sender;
    public Timestamp timestamp;

    @Override
    protected void init (Connection db, JsonObject jsonObject)
            throws SQLException, HttpErrorStatus {
        super.init(db, jsonObject);

        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public boolean authorize (
            Connection db,
            String authorization,
            AccessType accessType
    ) throws HttpErrorStatus {
        User currentUser = Login.byToken(db, authorization).user();

        if (sender.equals(currentUser)) {
            return true;
        }
        else if (accessType != AccessType.READ) {
            return false;
        }
        else {
            return sender.friends().contains(currentUser);
        }
    }

    @Override
    public void save(Connection db) throws SQLException {
        super.save(db);

        notifyFriends();
    }

    private void notifyFriends() {
        try {
            for (var friend : this.sender.friends()) {
                for (var device : friend.notificationDevices()) {
                    String title = "Beacon from " + this.sender.displayName;

                    String humanTime = formatTime(this.timestamp);
                    String body = this.sender.displayName
                                + " is free "
                                + humanTime;

                    device.sendNotification (title, body);
                }
            }
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    private static String formatTime(Timestamp time) {
        long timeDiffMillis = time.getTime() - System.currentTimeMillis();

        String relativeTime;
        if (timeDiffMillis < 60_000) {
            relativeTime = "now";
        }
        else if (timeDiffMillis < 60 * 60_000) {
            relativeTime = "in " + timeDiffMillis / 60_000 + " minutes";
        }
        else {
            relativeTime = "in " + timeDiffMillis / 60 / 60_000 + " hours";
        }

        LocalDateTime date = LocalDateTime.ofEpochSecond(time.getTime() / 1000, 0, TIME_ZONE_OFFSET);
        var format = DateTimeFormatter.ofPattern("h:mma");
        String absoluteTime = date.format (format);

        if (relativeTime.equals("now")) {
            return relativeTime;
        }
        else {
            return relativeTime + ", at " + absoluteTime;
        }
    }
}
