package net.issachanzi.beacon.model;

import jakarta.json.JsonObject;
import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.model.AccessType;
import net.issachanzi.resteasy.model.EasyModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Beacon extends EasyModel {
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
}
