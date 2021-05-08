package com.example.epassport;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface PassTableDao {
    @Query("SELECT * FROM PassTable")
    List<PassTable> getAll();

    @Query("SELECT * FROM PassTable WHERE id = :id")
    PassTable selectById(int id);

    @Insert
    void insert(PassTable passTable);

    @Update
    void update(PassTable passTable);

    @Delete
    void delete(PassTable passTable);
}
