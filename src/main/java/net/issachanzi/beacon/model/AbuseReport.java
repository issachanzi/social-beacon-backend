package net.issachanzi.beacon.model;

import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.model.AccessType;
import net.issachanzi.resteasy.model.EasyModel;

import java.sql.Connection;

public class AbuseReport extends EasyModel {
    @SuppressWarnings("unused")
    private User author;
    public User target;
    public String note;
    
    @Override
    public boolean authorize(
        Connection db,
        String authorization,
        AccessType accessType
    ) throws HttpErrorStatus {
        if (accessType == AccessType.CREATE) {
            Login login = Login.byToken(db, authorization);
            if (login != null) {
                this.author = login.user();
            }
            else {
                this.author = null;
            }
            return true;
        }
        else {
            return false;
        }
    }
}
