package com.mybatis.flex.reactor.core.wrapper;

/**
 * 更新操作结果
 * @author 林钟一六
 */
public class UpdateResult<Entity> {

    /**
     * 更新影响行数
     */
    private final int rows;
    /**
     * 更新后的实体
     */
    private final Entity entity;
    /**
     * 是否成功
     */
    private final boolean isSuccess;

    public UpdateResult(int rows, Entity entity) {
        this.rows = rows;
        this.entity = entity;
        this.isSuccess = rows > 0;
    }

    public int getRows() {
        return rows;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
