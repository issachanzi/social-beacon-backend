package net.issachanzi.beacon.model;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import net.issachanzi.resteasy.controller.exception.Forbidden;
import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.controller.exception.InternalServerError;
import net.issachanzi.resteasy.controller.exception.NotFound;
import net.issachanzi.resteasy.model.EasyModel;
import net.issachanzi.resteasy.model.annotation.CustomMethod;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class User extends EasyModel {
    private static final int PASSWORD_HASH_ITERATIONS = 10;
    private static final int PASSWORD_HASH_MEMORY = 65536;
    private static final int PASSWORD_HASH_PARALLELISM = 1;

    public String username;
    public String displayName;
    private String password;
    private Collection<User> friends = new HashSet<>();

    public void password (String password) {
        Argon2 argon2 = Argon2Factory.create();

        this.password = argon2.hash(
                PASSWORD_HASH_ITERATIONS,
                PASSWORD_HASH_MEMORY,
                PASSWORD_HASH_PARALLELISM,
                password.getBytes(StandardCharsets.UTF_8)
        );
    }

    public Collection<User> friends () {
        return this.friends;
    }

    public static User byUsername (
            Connection db,
            String username
    ) throws HttpErrorStatus {
        Map<String, String> filter = new HashMap<>();

        filter.put("username", username);

        try {
            return EasyModel
                    .where(db, filter, User.class)
                    .stream()
                    .findAny()
                    .orElseThrow();
        } catch (NoSuchElementException ex) {
            throw new NotFound(ex);
        } catch (SQLException ex) {
            throw new InternalServerError(ex);
        }
    }

    boolean checkPassword (String password) {
        Argon2 argon2 = Argon2Factory.create();

        return argon2.verify(
                this.password,
                password.getBytes(StandardCharsets.UTF_8)
        );
    }
}
