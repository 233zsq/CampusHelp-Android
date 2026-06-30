package com.campus.help.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.campus.help.data.model.CreditRecord;

import java.util.List;

@Dao
public interface CreditDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CreditRecord record);

    @Query("SELECT * FROM credit_records WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<CreditRecord>> observeByUser(long userId);

    @Query("SELECT IFNULL(SUM(delta), 0) FROM credit_records WHERE userId = :userId")
    int sumDelta(long userId);
}
