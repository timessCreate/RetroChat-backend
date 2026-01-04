package org.com.timess.retrochat.model.entity.user;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  实体类。
 *
 * @author eternal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = "snowFlakeId")
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * BCrypt加密固定60字符
     */
    private String password;

    /**
     * 用户昵称
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 1-正常 0-禁用
     */
    private Integer status;

    /**
     * 最近登录时间
     */
    private LocalDateTime lastLogin;

    /**
     * 用户创建时间
     */
    private LocalDateTime createTime;

    /**
     * 用户更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "is_delete", isLogicDelete = true)
    private Integer isDelete;

}
