package com.campus.help.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.campus.help.core.base.BaseRepository;
import com.campus.help.core.db.AppDatabase;
import com.campus.help.core.utils.AppExecutors;
import com.campus.help.data.dao.CreditDao;
import com.campus.help.data.model.CreditRecord;

import java.util.List;

public class CreditRepository extends BaseRepository {

    private final CreditDao dao;

    public CreditRepository(Context context) {
        dao = AppDatabase.getInstance(context.getApplicationContext()).creditDao();
    }

    public LiveData<List<CreditRecord>> observeByUser(long userId) {
        return dao.observeByUser(userId);
    }

    public void addRecord(CreditRecord record) {
        AppExecutors.get().diskIO().execute(() -> dao.insert(record));
    }
}
