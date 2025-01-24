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
    public static void main(String[] args) {
        try {
            initFirebase();

            var app = new RestEasy();

            app.init ();

            var garbageCollection = new GarbageCollection (app.db);
            garbageCollection.start();

            //testNotification();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void initFirebase () throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build();

        var app = FirebaseApp.initializeApp(options);

        System.out.println(app.getName());
    }
}