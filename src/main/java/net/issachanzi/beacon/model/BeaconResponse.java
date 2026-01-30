package net.issachanzi.beacon.model;

import com.google.firebase.messaging.FirebaseMessagingException;
import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.model.AccessType;
import net.issachanzi.resteasy.model.EasyModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class BeaconResponse extends EasyModel implements GarbageCollectable {
    public Beacon beacon;
    public User user;

    @Override
    public boolean authorize (
        Connection db,
        String authorization,
        AccessType accessType
    ) throws HttpErrorStatus {
        User currentUser = Optional.of(
            Login.byToken(db, authorization)
        )
            .map(Login::user)
            .orElse (null);
        if (currentUser == null) {
            return false;
        }
        else if (accessType == AccessType.READ) {
            return currentUser.equals (this.user)
                || currentUser.equals (this.beacon.sender);
        }
        else if (
            accessType == AccessType.CREATE
        ) {
            return this.user.equals (currentUser)
                && this.beacon.sender.friends().contains(currentUser);
        }
        else if (accessType == AccessType.DELETE) {
            return currentUser.equals (this.user);
        }
        else {
            return false;
        }
    }

    @Override
    public void save (Connection db) throws SQLException {
        super.save (db);

        notifyUser (db);
    }

    private void notifyUser(Connection db) {
        String title = this.user.displayName + " \u2764\ufe0f your beacon!";
        String body = "Text them now to meet up";
        
        this.beacon.sender.sendNotification(db, title, body);
    }

    @Override
    public boolean isGarbage() {
        return this.beacon == null;
    }
}
