package net.issachanzi.beacon.model;

import jakarta.json.JsonObject;
import net.issachanzi.resteasy.controller.exception.Forbidden;
import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.controller.exception.InternalServerError;
import net.issachanzi.resteasy.controller.exception.NotFound;
import net.issachanzi.resteasy.model.AccessType;
import net.issachanzi.resteasy.model.EasyModel;
import net.issachanzi.resteasy.model.annotation.NoPersist;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class Login extends EasyModel {
    private User user;
    private String token;

    @NoPersist
    private String username;
    @NoPersist
    private String password;

    public Login () {
        super();

        this.token = generateToken();
    }

    @Override
    protected void init (
            Connection db,
            JsonObject jsonObject
    ) throws SQLException, HttpErrorStatus {
        super.init(db, jsonObject);

        this.user = User.byUsername(db, this.username);

        if (!user.checkPassword(this.password)) {
            throw new Forbidden("Wrong password");
        }
    }

    @Override
    public boolean authorize(
            Connection db,
            String authorization,
            AccessType accessType
    ) throws HttpErrorStatus {
        if (accessType == AccessType.DELETE) {
            return this.token.equals (authorization);
        }
        else if (accessType == AccessType.CREATE) {
            return true;
        }
        else {
            return false;
        }
    }

    public User user () {
        return this.user;
    }

    public String token () {
        return this.token;
    }

    public void username (String username) {
        this.username = username;
    }

    public void password (String password) {
        this.password = password;
    }

    public static Login byToken (
            Connection db,
            String token
    ) throws HttpErrorStatus {
        Map<String, String> filter = new HashMap<>();

        filter.put("token", token);

        try {
            return EasyModel
                    .where(db, filter, Login.class)
                    .stream()
                    .findAny()
                    .orElseThrow();
        } catch (NoSuchElementException ex) {
            throw new NotFound(ex);
        } catch (SQLException ex) {
            throw new InternalServerError(ex);
        }
    }

    private String generateToken() {
        var random = new SecureRandom();
        byte[] resultBytes = new byte[128 / 8];

        random.nextBytes(resultBytes);

        return new String(
                Base64.getEncoder().encode(resultBytes),
                StandardCharsets.UTF_8
        );
    }
}
