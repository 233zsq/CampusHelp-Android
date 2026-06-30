package com.campus.help.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.campus.help.data.model.ChatMessage;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ChatMessage message);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    LiveData<List<ChatMessage>> observeByConversation(String conversationId);

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    LiveData<List<ChatMessage>> observeAll();

    @Query("UPDATE messages SET read = 1 WHERE conversationId = :conversationId")
    void markRead(String conversationId);
}
