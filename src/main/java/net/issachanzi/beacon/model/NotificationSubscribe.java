package net.issachanzi.beacon.model;

import com.google.firebase.messaging.*;
import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.model.AccessType;
import net.issachanzi.resteasy.model.EasyModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class NotificationSubscribe extends EasyModel {
    public User user;
    public String token;

    @Override
    public boolean authorize (
        Connection db,
        String authorization,
        AccessType accessType
    ) throws HttpErrorStatus {
        if (
            accessType == AccessType.CREATE
            || accessType == AccessType.DELETE
        ) {
            Login loginSession = Login.byToken(db, authorization);
            if (loginSession == null) {
                return false;
            }
            else {
                return loginSession.user ().equals (this.user);
            }
        }
        else {
            return false;
        }
    }

    @Override
    public void save(Connection db) throws SQLException {
        // Delete any other subscriptions from the same phone before saving

        var filter = new HashMap<String, String>();
        filter.put ("token", this.token);
        var duplicates = EasyModel.where (
            db,
            filter,
            NotificationSubscribe.class
        );

        for (var duplicate : duplicates) {
            duplicate.delete (db);
        }

        super.save(db);
    }

    public void sendNotification (
        Connection db,
        String title,
        String text
    ) throws FirebaseMessagingException, SQLException {
        var notification = Notification.builder();

        notification.setTitle (title);
        notification.setBody (text);

        var message = Message.builder ();

        message.setNotification(notification.build());
        message.setToken (this.token);

        var firebaseMessaging = FirebaseMessaging.getInstance();
        
        try {
            firebaseMessaging.send(message.build());
            
            System.out.println("Sent message " + message.build());
        } catch (FirebaseMessagingException ex) {
            if (ex.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                this.delete(db);
            }
            else {
                throw ex;
            }
        }
    }

}
