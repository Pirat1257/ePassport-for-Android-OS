package com.example.epassport;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface DG1TableDao {
    @Query("SELECT * FROM DG1Table")
    List<DG1Table> getAll();

    @Query("SELECT * FROM DG1Table WHERE id = :id")
    DG1Table selectById(int id);

    @Insert
    void insert(DG1Table dg1Table);

    @Update
    void update(DG1Table dg1Table);

    @Delete
    void delete(DG1Table dg1Table);
}
