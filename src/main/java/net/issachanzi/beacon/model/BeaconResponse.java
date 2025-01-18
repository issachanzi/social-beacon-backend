package net.issachanzi.beacon.model;

import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.model.AccessType;
import net.issachanzi.resteasy.model.EasyModel;

import java.sql.Connection;
import java.util.Optional;

public class BeaconResponse extends EasyModel {
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
}
