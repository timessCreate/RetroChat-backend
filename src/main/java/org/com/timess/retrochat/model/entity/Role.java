package org.com.timess.retrochat.model.entity;

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
@Table("role")
public class Role implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码(ROLE_ADMIN)
     */
    private String code;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 1-启用 0-禁用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
