package org.com.timess.retrochat.provider.user;

/**
 * @author eternal
 */

public class UserProvider {

    /**
     * 获取私聊对象信息集合
     * @param userId
     * @return
     */
    public String getFriendList(long userId){
        StringBuilder sb = new StringBuilder();
        sb.append("""
                 select user.id, user.username, user.user_avatar, user.user_profile,
                  user.email, user.phone, user.last_login, room.last_activity_time,
                  room.last_message_content, room.id as roomId
                from chat_room as room
                inner join chat_room_member as room_member
                on  room.id = room_member.room_id
                right join user
                on room_member.user_id = user.id
                where (room.type = 3 or room.type is null) 
                and ( room_member.user_id is null or room_member.user_id <> """).append(userId)
                .append(") and user.id <> ").append(userId);
        return sb.toString();
    }
}
