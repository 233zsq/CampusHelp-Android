package com.campus.help.core.network;

import com.campus.help.core.network.dto.UnreadCountResponse;
import com.campus.help.data.model.ChatMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * 消息 API（对应后端 MessageController）。通过 RetrofitClient.create(MessageApi.class) 获取。
 * <p>
 * 历史走 REST，实时走 WebSocket（{@link com.campus.help.feature.im.WebSocketService}），
 * Room 仅作聊天记录缓存。
 */
public interface MessageApi {

    /** 拉取会话历史（按时间正序）。 */
    @GET("api/messages")
    Call<ApiResponse<List<ChatMessage>>> history(@Query("conversationId") String conversationId);

    /** 发送消息（WS 不可用时的 REST 兜底；senderId 由后端强制取登录用户）。 */
    @POST("api/messages")
    Call<ApiResponse<Long>> send(@Body ChatMessage message);

    /** 当前用户的会话列表（每个会话最新一条，按时间倒序）。 */
    @GET("api/messages/conversations")
    Call<ApiResponse<List<ChatMessage>>> conversations();

    /** 当前用户的未读消息总数。 */
    @GET("api/messages/unread/count")
    Call<ApiResponse<UnreadCountResponse>> unreadCount();

    /** 标记会话已读。 */
    @PUT("api/messages/read")
    Call<ApiResponse<Void>> markRead(@Query("conversationId") String conversationId);
}
