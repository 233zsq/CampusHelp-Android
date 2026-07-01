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

    @Query("UPDATE messages SET read = 1 WHERE conversationId = :conversationId AND receiverId = :me")
    void markRead(String conversationId, long me);

    /** 按 id 查询（用于 WS 入消息去重）。 */
    @Query("SELECT * FROM messages WHERE id = :id")
    ChatMessage findById(long id);

    /** 按 id 删除（用于 ACK 替换临时消息）。 */
    @Query("DELETE FROM messages WHERE id = :id")
    void deleteById(long id);

    /** 清空某会话全部消息（历史刷新前清缓存）。 */
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    void deleteByConversation(String conversationId);

    /** 某会话内未读消息数（本地统计，会话列表小红点用）。 */
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND read = 0 AND receiverId = :me")
    int unreadIn(String conversationId, long me);
}
