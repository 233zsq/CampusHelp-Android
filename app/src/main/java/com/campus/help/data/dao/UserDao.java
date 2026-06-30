package com.campus.help.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.campus.help.data.model.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<User> observeById(long id);

    @Query("SELECT * FROM users WHERE id = :id")
    User getById(long id);

    @Query("SELECT * FROM users WHERE studentId = :studentId LIMIT 1")
    User getByStudentId(String studentId);

    @Query("SELECT * FROM users ORDER BY creditScore DESC")
    LiveData<List<User>> observeAll();

    @Query("SELECT COUNT(*) FROM users")
    int count();
}
