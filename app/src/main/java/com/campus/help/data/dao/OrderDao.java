package com.campus.help.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.campus.help.data.model.Order;

import java.util.List;

@Dao
public interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Order order);

    @Query("SELECT * FROM orders WHERE takerId = :takerId ORDER BY acceptedAt DESC")
    LiveData<List<Order>> observeByTaker(long takerId);

    @Query("SELECT * FROM orders WHERE taskId = :taskId LIMIT 1")
    Order getByTask(long taskId);

    @Query("UPDATE orders SET status = :status, completedAt = :completedAt WHERE id = :id")
    void complete(long id, int status, long completedAt);
}
