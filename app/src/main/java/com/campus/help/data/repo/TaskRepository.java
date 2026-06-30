package com.campus.help.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.campus.help.core.base.BaseRepository;
import com.campus.help.core.base.Callback;
import com.campus.help.core.db.AppDatabase;
import com.campus.help.core.utils.AppExecutors;
import com.campus.help.data.dao.TaskDao;
import com.campus.help.data.model.Task;

import java.util.List;

public class TaskRepository extends BaseRepository {

    private final TaskDao dao;

    public TaskRepository(Context context) {
        dao = AppDatabase.getInstance(context.getApplicationContext()).taskDao();
    }

    public LiveData<List<Task>> observeAll() {
        return dao.observeAll();
    }

    public LiveData<List<Task>> observeByType(int type) {
        return dao.observeByType(type);
    }

    public LiveData<List<Task>> observeByStatus(int status) {
        return dao.observeByStatus(status);
    }

    public void insert(Task task, Callback<Long> cb) {
        AppExecutors.get().diskIO().execute(() -> {
            long id = dao.insert(task);
            if (cb != null) {
                AppExecutors.get().main(() -> cb.onResult(id));
            }
        });
    }

    public void updateStatus(long taskId, int status) {
        AppExecutors.get().diskIO().execute(() -> dao.updateStatus(taskId, status));
    }
}
