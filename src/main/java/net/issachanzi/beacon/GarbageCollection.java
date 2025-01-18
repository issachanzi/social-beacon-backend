package net.issachanzi.beacon;

import net.issachanzi.beacon.model.Beacon;
import net.issachanzi.beacon.model.BeaconResponse;
import net.issachanzi.beacon.model.GarbageCollectable;
import net.issachanzi.resteasy.model.EasyModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public class GarbageCollection implements Runnable {

    private static long GARBAGE_COLLECTION_PERIOD = 10_000;

    private Connection db;

    public GarbageCollection (Connection db) {
        this.db = db;
    }

    public void start () {
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                collectGarbage ();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            try {
                Thread.sleep (GARBAGE_COLLECTION_PERIOD);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void collectGarbage () throws SQLException {
        collectGarbage (Beacon.class);
        collectGarbage (BeaconResponse.class);
    }

    private <M extends EasyModel & GarbageCollectable> void collectGarbage (
            Class <M> clazz
    ) throws SQLException {
        Collection <M> models = EasyModel.all (this.db, clazz);
        for (M model : models) {
            if (model.isGarbage ()) {
                model.delete (db);
            }
        }
    }
}
