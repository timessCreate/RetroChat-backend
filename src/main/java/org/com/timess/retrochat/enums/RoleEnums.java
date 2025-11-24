package org.com.timess.retrochat.enums;

/**
 * @author eternal
 */

public enum RoleEnums {

    NORMAL(1, "普通成员"),
    ADMIN(2, "管理员"),
    OWNER(3, "群主");
    /**
     * 成员角色：1-普通成员 2-管理员 3-群主
     */
    private Integer type;

    private String desc;
    public Integer getType() {
        return type;
    }
    public String getDesc() {
        return desc;
    }

    public void setType(Integer type) {
        this.type = type;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    private RoleEnums(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
