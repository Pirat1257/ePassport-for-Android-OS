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
    void insert(PassTable someInfo);

    @Update
    void update(PassTable someInfo);

    @Delete
    void delete(PassTable someInfo);
}
