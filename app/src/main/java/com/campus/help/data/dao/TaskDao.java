package com.campus.help.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.campus.help.data.model.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    LiveData<List<Task>> observeAll();

    @Query("SELECT * FROM tasks WHERE type = :type ORDER BY createdAt DESC")
    LiveData<List<Task>> observeByType(int type);

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY createdAt DESC")
    LiveData<List<Task>> observeByStatus(int status);

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getById(long id);

    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    void updateStatus(long id, int status);

    @Query("SELECT COUNT(*) FROM tasks")
    int count();
}
