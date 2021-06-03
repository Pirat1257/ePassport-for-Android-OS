package com.example.epassport;
import android.app.Application;
import androidx.room.Room;

import java.net.Socket;

public class App extends Application {
    public static App instance;
    private AppDatabase database;
    private Socket socket;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(this, AppDatabase.class, "database")
                .allowMainThreadQueries() // В этом случае не будут получаться Exception при работе в UI потоке
                .build();
    }
    public void update() {
        instance = this;
        database = Room.databaseBuilder(this, AppDatabase.class, "database")
                .allowMainThreadQueries().build();
    }
    public static App getInstance() { return instance; }
    public AppDatabase getDatabase() { return database; }
    public Socket getSocket() { return socket; }
    public void setSocket(Socket newSocket) { socket = newSocket; }
}
