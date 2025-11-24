package org.com.timess.retrochat.provider.user;

/**
 * @author eternal
 */
public class FriendProvider {
    /**
     * 1. 查询我的好友列表（已同意 + 按置顶排序）
     *
     * @param userId
     * @return
     */
    public String getFriendList(long userId) {
        return """
                SELECT\s
                    f.friend_id            AS id,
                    u.username,
                    u.phone,
                    u.email,
                    u.user_avatar          AS userAvatar,
                    f.remark,
                    f.top_order            AS topOrder,
                    f.updated_at           AS friendSince,
                    MAX(cr.room_id)             AS roomId,
                    MAX(cr.last_message_id)     AS lastMessageId,
                    MAX(cr.last_message_content) AS lastMessageContent,
                    MAX(cr.last_activity_time)  AS lastActivityTime
                FROM friend f
                INNER JOIN user u ON f.friend_id = u.id AND u.is_delete = 0
                LEFT JOIN (
                    -- 查找两个用户之间的私聊聊天室
                    SELECT\s
                        rm1.user_id as user1_id,
                        rm2.user_id as user2_id,
                        r.id as room_id,
                        r.last_message_id,
                        r.last_message_content,
                        r.last_activity_time
                    FROM chat_room r
                    INNER JOIN chat_room_member rm1 ON rm1.room_id = r.id AND rm1.is_delete = 0
                    INNER JOIN chat_room_member rm2 ON rm2.room_id = r.id AND rm2.is_delete = 0\s
                        AND rm2.user_id != rm1.user_id
                    WHERE r.type = 3\s
                        AND r.is_delete = 0
                        AND r.id IN (
                            -- 确保聊天室只有两个成员（私聊房间）
                            SELECT room_id\s
                            FROM chat_room_member\s
                            WHERE is_delete = 0\s
                            GROUP BY room_id\s
                            HAVING COUNT(*) = 2
                        )
                ) cr ON (cr.user1_id = f.user_id AND cr.user2_id = f.friend_id)\s
                    OR (cr.user1_id = f.friend_id AND cr.user2_id = f.user_id)
                WHERE f.user_id = #{userId}
                  AND f.status = 1
                  AND f.friend_id != #{userId}
                GROUP BY\s
                    f.friend_id,\s
                    u.username,\s
                    u.phone,\s
                    u.email,\s
                    u.user_avatar,\s
                    f.remark,\s
                    f.top_order,\s
                    f.updated_at
                ORDER BY f.top_order DESC, COALESCE(MAX(cr.last_activity_time), f.updated_at) DESC
                """;
    }

    /**
     * 2.查询“谁申请加我”（待确认）
     *
     * @param userId
     * @return
     */
    public String getFriendRequestList(long userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                         SELECT
                             f.user_id AS applicant_id,
                             u.username,
                             u.user_avatar,
                             f.created_at AS apply_time
                         FROM friend f
                         JOIN user u ON f.user_id = u.id
                         WHERE
                             f.friend_id =\s
                        \s""").append(userId)
                .append("""
                        AND f.status = 0;  -- 待确认
                        """);
        return sb.toString();
    }


    /**
     * 检查两人是否为好友
     */
    public String checkFriend(long userId, long friendId) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                SELECT COUNT(*) > 0 AS is_friend
                FROM friend\s
                WHERE\s
                    ((user_id =  AND friend_id = 456) OR (user_id = 456 AND friend_id = 123))
                    AND status = 1;
                """);
        return sb.toString();
    }

    /**
     * 拉黑某人
     * -- 先拒绝所有待确认，再拉黑
     */
    public String blockFriend(long userId, long friendId) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                UPDATE friend\s
                SET status = 3, updated_at = NOW(3)\s
                WHERE\s
                    (user_id = 123 AND friend_id = 456)\s
                    OR (user_id = 456 AND friend_id = 123);
                       \s""");
        return sb.toString();
    }
}
