package org.com.timess.retrochat.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.com.timess.retrochat.model.entity.chat.ChatRoom;

/**
 * 聊天室表 映射层。
 *
 * @author eternal
 */
public interface ChatRoomMapper extends BaseMapper<ChatRoom> {

    @Select("""
            SELECT * 
            FROM chat_room r
            INNER JOIN chat_room_member rm1 ON rm1.room_id = r.id AND rm1.user_id = #{userId1} AND rm1.is_delete = 0
            INNER JOIN chat_room_member rm2 ON rm2.room_id = r.id AND rm2.user_id = #{userId2} AND rm2.is_delete = 0
            WHERE r.type = 3 
                AND r.is_delete = 0
                AND r.id IN (
                    SELECT room_id 
                    FROM chat_room_member 
                    WHERE is_delete = 0 
                    GROUP BY room_id 
                    HAVING COUNT(*) = 2
                )
            LIMIT 1
            """)
    ChatRoom findPrivateRoomByUserPair(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
