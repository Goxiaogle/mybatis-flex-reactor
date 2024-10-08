package com.mybatis.flex.reactor.core;

import com.mybatis.flex.reactor.core.utils.ReactorUtils;
import com.mybatis.flex.reactor.core.wrapper.UpdateResult;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.exception.FlexExceptions;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.CPI;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.service.IService;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.util.ClassUtil;
import com.mybatisflex.core.util.MapperUtil;
import com.mybatisflex.core.util.SqlUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 响应式 Service 接口
 *
 * @param <Entity> 实体类泛型（不是 Mapper）
 * @author 林钟一六
 */
@SuppressWarnings({"unused", "unchecked"})
public interface ReactorService<Entity> {

    int DEFAULT_BATCH_SIZE = 1000;

    /**
     * 获取对应实体类的 Mapper 对象
     *
     * @return 基础映射实体类（BaseMapper）
     */
    BaseMapper<Entity> getMapper();

    /**
     * 获取对应实体类的普通的 IService 对象
     *
     * @return IService 对象（非响应式）
     */
    IService<Entity> getBlockService();

    /**
     * 保存实体类对象数据（忽略空值）
     *
     * @param entity 实体类对象
     * @return 是否保存成功
     */
    default Mono<Boolean> save(Entity entity) {
        return save(entity, true);
    }

    /**
     * 保存实体类对象数据
     *
     * @param entity      实体类对象
     * @param ignoreNulls 是否忽略空值
     * @return 是否保存成功
     */
    default Mono<Boolean> save(Entity entity, boolean ignoreNulls) {
        return Mono.create(emitter ->
                emitter.success(
                        SqlUtil.toBool(getMapper().insert(entity, ignoreNulls))
                )
        );
    }

    /**
     * 批量保存实体类对象数据
     *
     * @param entities    实体类对象集合
     * @param ignoreNulls 是否忽略空值
     * @param batchSize   每次批量保存的记录数
     * @return 返回一个 Flux，每一条记录该条数据是否保存成功以及该实体类对象 {@link UpdateResult}
     */
    default Flux<UpdateResult<Entity>> saveBatch(Collection<Entity> entities, boolean ignoreNulls, int batchSize) {
        Class<BaseMapper<Entity>> usefulClass = (Class<BaseMapper<Entity>>) ClassUtil.getUsefulClass(getMapper().getClass());
        return Flux.create(emitter -> {
            Db.executeBatch(entities, batchSize, usefulClass, (m, e) -> {
                int rows = m.insert(e, ignoreNulls);
                emitter.next(new UpdateResult<>(rows, e));
            });
            emitter.complete();
        });
    }

    /**
     * 批量保存实体类对象数据
     *
     * @param entities    实体类对象集合
     * @param ignoreNulls 是否忽略空值
     * @return 返回一个 Flux，每一条记录该条数据是否保存成功以及该实体类对象 {@link UpdateResult}
     */
    default Flux<UpdateResult<Entity>> saveBatch(Collection<Entity> entities, boolean ignoreNulls) {
        return saveBatch(entities, ignoreNulls, DEFAULT_BATCH_SIZE);
    }

    /**
     * 保存或更新实体类对象数据
     *
     * @param entity      实体类对象
     * @param ignoreNulls 是否忽略空值
     * @return 是否保存成功
     */
    default Mono<Boolean> saveOrUpdate(Entity entity, boolean ignoreNulls) {
        return Mono.create(emitter ->
                emitter.success(
                        SqlUtil.toBool(getMapper().insertOrUpdate(entity, ignoreNulls))
                )
        );
    }

    /**
     * 保存或更新实体类对象数据（忽略空值）
     *
     * @param entity 实体类对象
     * @return 是否保存成功
     */
    default Mono<Boolean> saveOrUpdate(Entity entity) {
        return saveOrUpdate(entity, true);
    }

    /**
     * 批量保存或更新实体类对象数据
     *
     * @param entities    实体类对象集合
     * @param ignoreNulls 是否忽略空值
     * @param batchSize   每次批量保存的记录数
     * @return 返回一个 Flux，每一条记录该条数据是否保存成功以及该实体类对象 {@link UpdateResult}
     */
    default Flux<UpdateResult<Entity>> saveOrUpdateBatch(Collection<Entity> entities, boolean ignoreNulls, int batchSize) {
        Class<BaseMapper<Entity>> usefulClass = ClassUtil.getUsefulClass((Class<BaseMapper<Entity>>) getMapper().getClass());
        return Flux.create(emitter -> {
            Db.executeBatch(entities, batchSize, usefulClass, (m, e) -> {
                int rows = m.insertOrUpdate(e, ignoreNulls);
                emitter.next(new UpdateResult<>(rows, e));
            });
            emitter.complete();
        });
    }

    /**
     * 批量保存或更新实体类对象数据
     *
     * @param entities    实体类对象集合
     * @param ignoreNulls 是否忽略空值
     * @return 返回一个 Flux，每一条记录该条数据是否保存成功以及该实体类对象 {@link UpdateResult}
     */
    default Flux<UpdateResult<Entity>> saveOrUpdateBatch(Collection<Entity> entities, boolean ignoreNulls) {
        return saveOrUpdateBatch(entities, ignoreNulls, DEFAULT_BATCH_SIZE);
    }


    /**
     * 批量保存或更新实体类对象数据（忽略空值）
     *
     * @param entities 实体类对象集合
     * @return 返回一个 Flux，每一条记录该条数据是否保存成功以及该实体类对象 {@link UpdateResult}
     */
    default Flux<UpdateResult<Entity>> saveOrUpdateBatch(Collection<Entity> entities) {
        return saveOrUpdateBatch(entities, true, DEFAULT_BATCH_SIZE);
    }

    /**
     * 根据条件删除数据
     *
     * @param query 条件
     * @return 是否删除成功
     */
    default Mono<Boolean> remove(QueryWrapper query) {
        return Mono.create(emitter ->
                emitter.success(
                        SqlUtil.toBool(getMapper().deleteByQuery(query))
                )
        );
    }

    /**
     * 根据条件删除数据
     *
     * @param condition 条件
     * @return 是否删除成功
     */
    default Mono<Boolean> remove(QueryCondition condition) {
        return remove(QueryWrapper.create().where(condition));
    }

    /**
     * 根据实体主键删除数据
     *
     * @param entity 实体类对象
     * @return 是否删除成功
     */
    default Mono<Boolean> removeById(Entity entity) {
        return Mono.create(emitter ->
                emitter.success(
                        SqlUtil.toBool(getMapper().delete(entity))
                )
        );
    }

    /**
     * 根据主键删除数据
     *
     * @param id 主键
     * @return 是否删除成功
     */
    default Mono<Boolean> removeById(Serializable id) {
        return Mono.create(emitter ->
                emitter.success(SqlUtil.toBool(getMapper().deleteById(id)))
        );
    }

    /**
     * 根据主键集合批量删除数据
     *
     * @param ids 主键集合
     * @return 是否删除成功
     */
    default Mono<Boolean> removeByIds(Collection<Serializable> ids) {
        return Mono.create(emitter ->
                emitter.success(SqlUtil.toBool(getMapper().deleteBatchByIds(ids)))
        );
    }

    /**
     * 根据 Map 构建查询条件删除数据
     *
     * @param query 条件
     * @return 是否删除成功
     */
    default Mono<Boolean> removeByMap(Map<String, Object> query) {
        if (query == null || query.isEmpty()) {
            throw FlexExceptions.wrap("deleteByMap is not allow empty map.");
        }
        return remove(QueryWrapper.create().where(query));
    }

    /**
     * 根据主键更新数据
     *
     * @param entity      实体类对象
     * @param ignoreNulls 是否忽略空值
     * @return 是否更新成功
     */
    default Mono<Boolean> updateById(Entity entity, boolean ignoreNulls) {
        return Mono.create(emitter ->
                emitter.success(
                        SqlUtil.toBool(getMapper().update(entity, ignoreNulls))
                )
        );
    }

    /**
     * 根据主键更新数据（忽略空值）
     *
     * @param entity 实体类对象
     * @return 是否更新成功
     */
    default Mono<Boolean> updateById(Entity entity) {
        return updateById(entity, true);
    }

    /**
     * 根据 Map 构建查询条件更新数据
     *
     * @param entity 实体类对象
     * @param query  条件
     * @return 是否更新成功
     */
    default Mono<Boolean> update(Entity entity, Map<String, Object> query) {
        return update(entity, QueryWrapper.create().where(query));
    }

    /**
     * 根据条件更新数据
     *
     * @param entity    实体类对象
     * @param condition 条件
     * @return 是否更新成功
     */
    default Mono<Boolean> update(Entity entity, QueryCondition condition) {
        return update(entity, QueryWrapper.create().where(condition));
    }

    /**
     * 根据条件更新数据
     *
     * @param entity 实体类对象
     * @param query  条件
     * @return 是否更新成功
     */
    default Mono<Boolean> update(Entity entity, QueryWrapper query) {
        return Mono.create(emitter ->
                emitter.success(
                        SqlUtil.toBool(getMapper().updateByQuery(entity, query))
                )
        );
    }

    /**
     * 根据主键批量更新数据
     *
     * @param entities    实体类对象集合
     * @param ignoreNulls 是否忽略空值
     * @param batchSize   每次批量更新的记录数
     * @return 返回一个 Flux，每一条记录该条数据是否更新成功以及该实体类对象 [UpdateResult]
     */
    default Flux<UpdateResult<Entity>> updateBatch(Collection<Entity> entities, boolean ignoreNulls, int batchSize) {
        Class<BaseMapper<Entity>> usefulClass = ClassUtil.getUsefulClass((Class<BaseMapper<Entity>>) getMapper().getClass());
        return Flux.create(emitter -> {
            Db.executeBatch(entities, batchSize, usefulClass, (m, e) -> {
                int rows = m.update(e, ignoreNulls);
                emitter.next(new UpdateResult<>(rows, e));
            });
            emitter.complete();
        });
    }

    /**
     * 根据主键批量更新数据
     *
     * @param entities    实体类对象集合
     * @param ignoreNulls 是否忽略空值
     * @return 返回一个 Flux，每一条记录该条数据是否更新成功以及该实体类对象 [UpdateResult]
     */
    default Flux<UpdateResult<Entity>> updateBatch(Collection<Entity> entities, boolean ignoreNulls) {
        return updateBatch(entities, ignoreNulls, DEFAULT_BATCH_SIZE);
    }

    /**
     * 根据主键批量更新数据（忽略空值）
     *
     * @param entities 实体类对象集合
     * @return 返回一个 Flux，每一条记录该条数据是否更新成功以及该实体类对象 [UpdateResult]
     */
    default Flux<UpdateResult<Entity>> updateBatch(Collection<Entity> entities) {
        return updateBatch(entities, true, DEFAULT_BATCH_SIZE);
    }

    /**
     * 根据主键查询一条数据
     *
     * @param id 主键
     * @return 查询结果
     */
    default Mono<Entity> getById(Serializable id) {
        return Mono.create(emitter ->
                emitter.success(getMapper().selectOneById(id))
        );
    }

    /**
     * 根据实体主键查询一条数据
     *
     * @param entity 实体类对象
     * @return 查询结果
     */
    default Mono<Entity> getById(Entity entity) {
        return Mono.create(emitter ->
                emitter.success(
                        getMapper().selectOneByEntityId(entity)
                )
        );
    }

    /**
     * 根据条件查询一条数据
     *
     * @param query 条件
     * @return 查询结果
     */
    default Mono<Entity> getOne(QueryWrapper query) {
        return Mono.create(emitter ->
                emitter.success(
                        getMapper().selectOneByQuery(query)
                )
        );
    }

    /**
     * 根据条件查询一条数据，并且转换为指定的 [AS] 类型
     *
     * @param query  条件
     * @param asType 指定的类型
     * @param <AS>   指定类型的泛型
     * @return 查询结果
     */
    default <AS> Mono<AS> getOneAs(QueryWrapper query, Class<AS> asType) {
        return Mono.create(emitter ->
                emitter.success(
                        getMapper().selectOneByQueryAs(query, asType)
                )
        );
    }

    /**
     * 查询结果集中第一列，且第一条数据
     *
     * @param query 条件
     * @return 数据值
     */
    default Mono<Object> getObject(QueryWrapper query) {
        return Mono.create(emitter ->
                emitter.success(
                        getMapper().selectObjectByQuery(query)
                )
        );
    }

    /**
     * 查询结果集中第一列，且第一条数据
     *
     * @param query  条件
     * @param asType 指定的类型
     * @param <AS>   指定类型的泛型
     * @return 数据值
     */
    default <AS> Mono<AS> getObjectAs(QueryWrapper query, Class<AS> asType) {
        return Mono.create(emitter ->
                emitter.success(
                        getMapper().selectObjectByQueryAs(query, asType)
                )
        );
    }

    /**
     * 查询结果集中第一列的所有数据（一次性返回）
     *
     * @param query 条件
     * @return 数据列表
     */
    default Mono<List<Object>> getObjectListOnce(QueryWrapper query) {
        return Mono.create(emitter ->
                emitter.success(
                        getMapper().selectObjectListByQuery(query)
                )
        );
    }

    /**
     * 查询结果集中第一列的所有数据（一次性返回）
     *
     * @param query  条件
     * @param asType 指定的类型
     * @param <AS>   指定类型的泛型
     * @return 数据列表
     */
    default <AS> Mono<List<AS>> getObjectListOnceAs(QueryWrapper query, Class<AS> asType) {
        return Mono.create(emitter ->
                emitter.success(
                        getMapper().selectObjectListByQueryAs(query, asType)
                )
        );
    }

    /**
     * 查询所有数据（会开启事务）
     *
     * @return 数据列表
     */
    default Flux<Entity> list() {
        return list(QueryWrapper.create());
    }

    /**
     * 根据条件查询所有数据（会开启事务）
     *
     * @param query 条件
     * @return 数据列表
     */
    default Flux<Entity> list(QueryCondition query) {
        return list(QueryWrapper.create().where(query));
    }

    /**
     * 根据条件查询所有数据（会开启事务）
     *
     * @param query 条件
     * @return 数据列表
     */
    default Flux<Entity> list(QueryWrapper query) {
        return ReactorUtils.cursorToFlux(() -> getMapper().selectCursorByQuery(query));
    }

    /**
     * 根据条件查询所有数据，并且转换为指定的 [AS] 类型
     *
     * @param query  条件
     * @param asType 指定的类型
     * @param <AS>   指定类型的泛型
     * @return 数据列表
     */
    default <AS> Flux<AS> listAs(QueryWrapper query, Class<AS> asType) {
        return ReactorUtils.cursorToFlux(() -> getMapper().selectCursorByQueryAs(query, asType));
    }

    /**
     * 根据主键查询所有数据（一次性返回）
     *
     * @param ids 主键列表
     * @return 数据列表（Mono）
     */
    default Mono<List<Entity>> listOnceByIds(Collection<Serializable> ids) {
        return Mono.create(emitter ->
                emitter.success(getMapper().selectListByIds(ids))
        );
    }

    /**
     * 根据 Map 构建查询条件查询数据
     *
     * @param query Map
     * @return 数据列表
     */
    default Flux<Entity> listByMap(Map<String, Object> query) {
        return list(QueryWrapper.create().where(query));
    }


    /**
     * 获取分页数据
     *
     * @param page       分页对象
     * @param queryTotal 是否使用 count 查询总数并回填到 page 中（仅在 page 的 totalPage 不存在时才查询，当 totalPage 有值时无论该参数是否为 true 都不会再查询）
     * @return 在该页中的数据
     */
    default Flux<Entity> page(Page<Entity> page, boolean queryTotal) {
        return page(
                page,
                QueryWrapper.create(),
                queryTotal
        );
    }

    /**
     * 获取分页数据
     *
     * @param page 分页对象（若未初始化 totalPage 值，会自动使用 count 函数查询并回填到 page 对象中）
     * @return 在该页中的数据
     */
    default Flux<Entity> page(Page<Entity> page) {
        return page(page, true);
    }

    /**
     * 获取分页数据
     *
     * @param page       分页对象
     * @param query      条件
     * @param queryTotal 是否使用 count 查询总数并回填到 page 中（仅在 page 的 totalPage 不存在时才查询，当 totalPage 有值时无论该参数是否为 true 都不会再查询）
     * @return 在该页中的数据
     */
    default Flux<Entity> page(Page<Entity> page, QueryCondition query, boolean queryTotal) {
        return page(
                page,
                QueryWrapper.create().where(query),
                queryTotal
        );
    }

    /**
     * 获取分页数据
     *
     * @param page  分页对象（若未初始化 totalPage 值，会自动使用 count 函数查询并回填到 page 对象中）
     * @param query 条件
     * @return 在该页中的数据
     */
    default Flux<Entity> page(Page<Entity> page, QueryCondition query) {
        return page(page, query, true);
    }

    /**
     * 获取分页数据
     *
     * @param page  分页对象（若未初始化 totalPage 值，会自动使用 count 函数查询并回填到 page 对象中）
     * @param query 条件
     * @return 在该页中的数据
     */
    default Flux<Entity> page(Page<Entity> page, QueryWrapper query) {
        return page(page, query, true);
    }

    /**
     * 获取分页数据
     *
     * @param page       分页对象
     * @param query      条件
     * @param queryTotal 是否使用 count 查询总数并回填到 page 中（仅在 page 的 totalPage 不存在时才查询，当 totalPage 有值时无论该参数是否为 true 都不会再查询）
     * @return 在该页中的数据
     */
    default Flux<Entity> page(Page<Entity> page, QueryWrapper query, boolean queryTotal) {
        // 为了避免一些序列化框架不使用 setter 方法构建对象，而产生错误的 page 值，所以这里还是需要进行一些处理
        if(page.getPageNumber() < 1) {
            page.setPageNumber(1);
        }
        if(page.getPageSize() < 1) {
            page.setPageSize(FlexGlobalConfig.getDefaultConfig().getDefaultPageSize());
        }

        // ===== 下面的逻辑基本上就是复刻了 MapperUtil#doPaginate 的逻辑 =====

        // 预存一下原始的 limit 信息，因为分页操作逻辑中会修改 limit 信息
        // 为了防止对用户原始 query 对象造成意外的影响，后续我们需要还原原始的 limit 信息
        Long limitRows = CPI.getLimitRows(query);
        Long limitOffset = CPI.getLimitOffset(query);
        try {
            // 此处不采用 == INIT_VALUE 的形式，因为前端传来的 totalPage 可能是其它负数值
            if (page.getTotalPage() < 0 && queryTotal) {
                // 构建 count 查询条件：根据 needOptimizeCountQuery （是否启用优化查询，默认 true）抉择构建方式
                QueryWrapper countQueryWrapper = page.needOptimizeCountQuery() ?
                        // 构建优化的 count 查询条件
                        MapperUtil.optimizeCountQueryWrapper(query) :
                        // 构建原始 count 查询条件
                        MapperUtil.rawCountQueryWrapper(query);
                // 移除原有 limit，避免出现错误的数据
                CPI.setLimitRows(countQueryWrapper, null);
                CPI.setLimitOffset(countQueryWrapper, null);
                page.setTotalRow(
                        getMapper().selectCountByQuery(countQueryWrapper)
                );
            }
            return list(
                    query.limit(page.offset(), page.getPageSize())
            );
        } finally {
            // 还原原有 limit 信息
            CPI.setLimitRows(query, limitRows);
            CPI.setLimitOffset(query, limitOffset);
        }
    }


    /**
     * 根据条件查询数据是否存在
     *
     * @param query 条件
     * @return 是否存在
     */
    default Mono<Boolean> exists(QueryWrapper query) {
        return exists(CPI.getWhereQueryCondition(query));
    }

    /**
     * 根据条件查询数据是否存在
     *
     * @param query 条件
     * @return 是否存在
     */
    default Mono<Boolean> exists(QueryCondition query) {
        return Mono.create(emitter ->
                emitter.success(
                        !getMapper().selectObjectListByQuery(
                                QueryWrapper.create().where(query)
                                        .limit(1)
                        ).isEmpty()
                )
        );
    }

    /**
     * 查询数据总数
     *
     * @return 数据总数
     */
    default Mono<Long> count() {
        return count(QueryWrapper.create());
    }

    /**
     * 根据条件查询数据总数
     *
     * @param query 条件
     * @return 数据总数
     */
    default Mono<Long> count(QueryCondition query) {
        return Mono.create(emitter ->
                emitter.success(
                        getMapper().selectCountByQuery(QueryWrapper.create().where(query))
                )
        );
    }

    /**
     * 根据条件查询数据总数
     *
     * @param query 条件
     * @return 数据总数
     */
    default Mono<Long> count(QueryWrapper query) {
        return Mono.create(emitter ->
                emitter.success(getMapper().selectCountByQuery(query))
        );
    }

    /**
     * 分页查询（一次性查完该页数据）
     *
     * @param page 分页对象
     * @return 分页对象（Mono）
     */
    default Mono<Page<Entity>> pageOnce(Page<Entity> page) {
        return pageOnce(page, QueryWrapper.create());
    }

    /**
     * 分页查询（一次性查完该页数据）
     *
     * @param page  分页对象
     * @param query 条件
     * @return 分页对象（Mono）
     */
    default Mono<Page<Entity>> pageOnce(Page<Entity> page, QueryCondition query) {
        return pageOnce(page, QueryWrapper.create().where(query));
    }

    /**
     * 分页查询（一次性查完该页数据）
     *
     * @param page  分页对象
     * @param query 条件
     * @return 分页对象（Mono）
     */
    default Mono<Page<Entity>> pageOnce(Page<Entity> page, QueryWrapper query) {
        return pageOnceAs(page, query, null);
    }

    /**
     * 分页查询（一次性查完该页数据）
     *
     * @param page   分页对象
     * @param query  条件
     * @param asType 转换的指定类型（可为 null，为 null 时不转换）
     * @param <AS>   指定类型的泛型
     * @return 分页对象（Mono）
     */
    default <AS> Mono<Page<AS>> pageOnceAs(Page<AS> page, QueryWrapper query, Class<AS> asType) {
        return Mono.create(emitter ->
                emitter.success(getMapper().paginateAs(page, query, asType))
        );
    }

    /**
     * 链式查询
     *
     * @return [QueryChain] 对象
     */
    default QueryChain<Entity> queryChain() {
        return QueryChain.of(getMapper());
    }

    /**
     * 链式更新
     *
     * @return [UpdateChain] 对象
     */
    default UpdateChain<Entity> updateChain() {
        return UpdateChain.of(getMapper());
    }

}
