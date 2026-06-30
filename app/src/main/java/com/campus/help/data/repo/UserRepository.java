package com.campus.help.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.campus.help.core.base.BaseRepository;
import com.campus.help.core.base.Callback;
import com.campus.help.core.db.AppDatabase;
import com.campus.help.core.utils.AppExecutors;
import com.campus.help.data.dao.UserDao;
import com.campus.help.data.model.User;

import java.util.List;

public class UserRepository extends BaseRepository {

    private final UserDao dao;

    public UserRepository(Context context) {
        dao = AppDatabase.getInstance(context.getApplicationContext()).userDao();
    }

    public LiveData<User> observeUser(long id) {
        return dao.observeById(id);
    }

    public LiveData<List<User>> observeAll() {
        return dao.observeAll();
    }

    public void insert(User user, Callback<Long> cb) {
        AppExecutors.get().diskIO().execute(() -> {
            long id = dao.insert(user);
            if (cb != null) {
                AppExecutors.get().main(() -> cb.onResult(id));
            }
        });
    }

    public void updateCredit(long userId, int newScore, Callback<Void> cb) {
        AppExecutors.get().diskIO().execute(() -> {
            User u = dao.getById(userId);
            if (u != null) {
                u.creditScore = newScore;
                dao.update(u);
            }
            if (cb != null) {
                AppExecutors.get().main(() -> cb.onResult(null));
            }
        });
    }
}
