package org.com.timess.retrochat.model.entity.user;

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
@Table("permission")
public class Permission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = "snowFlakeId")
    private Long id;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限标识符(system:user:create)
     */
    private String code;

    /**
     * 1-菜单 2-按钮 3-API
     */
    private Integer type;

    /**
     * 前端路由路径
     */
    private String path;

    /**
     * 前端组件
     */
    private String component;

    /**
     * 图标
     */
    private String icon;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * 1-启用 0-禁用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
