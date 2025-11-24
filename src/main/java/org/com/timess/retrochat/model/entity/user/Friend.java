package org.com.timess.retrochat.model.entity.user;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 好友关系表 实体类。
 *
 * @author eternal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("friend")
public class Friend implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Auto)
    private BigInteger id;

    /**
     * 用户ID（主动发起方）
     */
    private BigInteger userId;

    /**
     * 好友ID（被添加方）
     */
    private BigInteger friendId;

    /**
     * 关系状态：0-待确认 1-已同意 2-已拒绝 3-已拉黑
     */
    private Integer status;

    /**
     * 好友备注名（仅对自己可见）
     */
    private String remark;

    /**
     * 好友分组ID（可关联 friend_group 表）
     */
    private BigInteger groupId;

    /**
     * 自定义好友头像（覆盖原用户头像）
     */
    private String avatar;

    /**
     * 置顶排序：0-不置顶 >0-置顶（数值越小越靠前）
     */
    private Integer topOrder;

    /**
     * 消息免打扰截止时间
     */
    private LocalDateTime muteUntil;

    /**
     * 创建时间（申请时间）
     */
    private LocalDateTime createdAt;

    /**
     * 最后更新时间（同意/拉黑时间）
     */
    private LocalDateTime updatedAt;

}
