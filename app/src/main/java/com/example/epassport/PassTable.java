package com.example.epassport;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Таблица, содержащая хеш пароля
@Entity
public class PassTable {
    @PrimaryKey
    @NonNull
    int id;
    public String pass_hash;
}
