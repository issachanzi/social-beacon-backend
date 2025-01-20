package net.issachanzi.beacon.model;

import com.google.firebase.messaging.FirebaseMessagingException;
import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.controller.exception.InternalServerError;
import net.issachanzi.resteasy.model.AccessType;
import net.issachanzi.resteasy.model.EasyModel;
import net.issachanzi.resteasy.model.annotation.CustomMethod;

import java.sql.Connection;
import java.sql.SQLException;

public class FriendRequest extends EasyModel {
    public User from;
    public User to;

    @CustomMethod
    public void accept (Connection db) throws InternalServerError {
        from.friends().add(to);
        to.friends().add(from);

        try {
            from.save(db);
            to.save(db);
            this.delete(db);
        } catch (SQLException ex) {
            throw new InternalServerError(ex);
        }
    }

    @Override
    public boolean authorize (
            Connection db,
            String authorization,
            AccessType accessType
    ) throws HttpErrorStatus {
        User currentUser = Login.byToken(db, authorization).user();

        if (accessType == AccessType.READ || accessType == AccessType.DELETE) {
            return currentUser.equals(from) || currentUser.equals(to);
        }
        else if (accessType == AccessType.CREATE) {
            return currentUser.equals(from);
        }
        else if (accessType == AccessType.CUSTOM_METHOD) {
            return currentUser.equals(to);
        }
        else {
            return false;
        }
    }

    @Override
    public void save (Connection db) throws SQLException {
        super.save (db);

        notifyUser();
    }

    private void notifyUser() {
        for (var device : this.to.notificationDevices()) {
            String title = this.from.displayName + " sent you a friend request!";

            String body = "Accept the friend request?";

            try {
                device.sendNotification (title, body);
            } catch (FirebaseMessagingException ex) {
                ex.printStackTrace();
            }
        }
    }
}
