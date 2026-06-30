package com.campus.help.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.campus.help.core.base.BaseRepository;
import com.campus.help.core.db.AppDatabase;
import com.campus.help.core.utils.AppExecutors;
import com.campus.help.data.dao.MessageDao;
import com.campus.help.data.model.ChatMessage;

import java.util.List;

public class MessageRepository extends BaseRepository {

    private final MessageDao dao;

    public MessageRepository(Context context) {
        dao = AppDatabase.getInstance(context.getApplicationContext()).messageDao();
    }

    public LiveData<List<ChatMessage>> observeConversation(String conversationId) {
        return dao.observeByConversation(conversationId);
    }

    public LiveData<List<ChatMessage>> observeAll() {
        return dao.observeAll();
    }

    public void insert(ChatMessage message) {
        AppExecutors.get().diskIO().execute(() -> dao.insert(message));
    }

    public void markRead(String conversationId) {
        AppExecutors.get().diskIO().execute(() -> dao.markRead(conversationId));
    }
}
