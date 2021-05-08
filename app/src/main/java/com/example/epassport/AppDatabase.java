package com.example.epassport;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {PassTable.class, DG1Table.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PassTableDao passTableDao();
    public abstract DG1TableDao dg1TableDao();
}
