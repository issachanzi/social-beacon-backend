package net.issachanzi.beacon;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessagingException;
import net.issachanzi.beacon.model.GarbageCollectable;
import net.issachanzi.beacon.model.NotificationSubscribe;
import net.issachanzi.resteasy.RestEasy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

public class Main {
    private static final String dbUrl  = "jdbc:postgresql://localhost:5432/app?user=postgres";

    public static void main(String[] args) {
        try {
            initFirebase();

            Connection db = DriverManager.getConnection(dbUrl);
            var app = new RestEasy();

            app.init (db);

            var garbageCollection = new GarbageCollection (db);
            garbageCollection.start();

            //testNotification();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void testNotification() throws FirebaseMessagingException {
        var ns = new NotificationSubscribe();

        ns.token = "d1dxBatPTk6zTPO7GGdYRL:APA91bEXk_jvvNKY5R6MnjRRy_G3mD-zbPbG8pdV6Ltclicok0rrWaAlWiz4_Y6GsT4-JoQl9IAXFqWMTKZHL36coVO_MwCByhnir8lWDYMkQYvJmQZpkkY";

        ns.sendNotification (
        "Test notification",
        "Test notification sent from java backend"
        );
    }

    public static void initFirebase () throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build();

        var app = FirebaseApp.initializeApp(options);

        System.out.println(app.getName());
    }
}