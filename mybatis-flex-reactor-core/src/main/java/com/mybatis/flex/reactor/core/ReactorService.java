package com.mybatis.flex.reactor.core;

import com.mybatis.flex.reactor.core.wrapper.UpdateResult;
import com.mybatisflex.core.BaseMapper;
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
import com.mybatisflex.core.util.SqlUtil;
import org.apache.ibatis.cursor.Cursor;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
     * 保存实体类对象数据
     *
     * @param entity 实体类对象
     * @return 是否保存成功
     */
    default Mono<Boolean> save(Entity entity) {
        return Mono.just(SqlUtil.toBool(getMapper().insert(entity)));
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
        return Mono.just(SqlUtil.toBool(getMapper().insertOrUpdate(entity, ignoreNulls)));
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
        return Mono.just(SqlUtil.toBool(getMapper().deleteByQuery(query)));
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
        return Mono.just(SqlUtil.toBool(getMapper().delete(entity)));
    }

    /**
     * 根据主键删除数据
     *
     * @param id 主键
     * @return 是否删除成功
     */
    default Mono<Boolean> removeById(Serializable id) {
        return Mono.just(SqlUtil.toBool(getMapper().deleteById(id)));
    }

    /**
     * 根据主键集合批量删除数据
     *
     * @param ids 主键集合
     * @return 是否删除成功
     */
    default Mono<Boolean> removeByIds(Collection<Serializable> ids) {
        return Mono.just(SqlUtil.toBool(getMapper().deleteBatchByIds(ids)));
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
        return Mono.just(SqlUtil.toBool(getMapper().update(entity, ignoreNulls)));
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
        return Mono.just(SqlUtil.toBool(getMapper().updateByQuery(entity, query)));
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
        return Mono.justOrEmpty(getMapper().selectOneById(id));
    }

    /**
     * 根据实体主键查询一条数据
     *
     * @param entity 实体类对象
     * @return 查询结果
     */
    default Mono<Entity> getById(Entity entity) {
        return Mono.justOrEmpty(getMapper().selectOneByEntityId(entity));
    }

    /**
     * 根据条件查询一条数据
     *
     * @param query 条件
     * @return 查询结果
     */
    default Mono<Entity> getOne(QueryWrapper query) {
        return Mono.justOrEmpty(getMapper().selectOneByQuery(query));
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
        return Mono.justOrEmpty(getMapper().selectOneByQueryAs(query, asType));
    }

    /**
     * 查询结果集中第一列，且第一条数据
     *
     * @param query 条件
     * @return 数据值
     */
    default Mono<Object> getObject(QueryWrapper query) {
        return Mono.justOrEmpty(getMapper().selectObjectByQuery(query));
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
        return Mono.justOrEmpty(getMapper().selectObjectByQueryAs(query, asType));
    }

    /**
     * 查询结果集中第一列的所有数据（一次性返回）
     *
     * @param query 条件
     * @return 数据列表
     */
    default Mono<List<Object>> getObjectListOnce(QueryWrapper query) {
        return Mono.justOrEmpty(getMapper().selectObjectListByQuery(query));
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
        return Mono.justOrEmpty(getMapper().selectObjectListByQueryAs(query, asType));
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
        return Flux.create(emitter -> {
            Db.tx(() -> {
                try (Cursor<Entity> cursor = getMapper().selectCursorByQuery(query)) {
                    for (Entity it : cursor) {
                        emitter.next(it);
                    }
                } catch (Exception e) {
                    emitter.error(e);
                    return false;
                }
                emitter.complete();
                return true;
            });
        });
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
        return Flux.create(emitter -> {
            Db.tx(() -> {
                try (Cursor<AS> cursor = getMapper().selectCursorByQueryAs(query, asType)) {
                    for (AS it : cursor) {
                        emitter.next(it);
                    }
                } catch (Exception e) {
                    emitter.error(e);
                    return false;
                }
                emitter.complete();
                return true;
            });
        });
    }

    /**
     * 根据主键查询所有数据（一次性返回）
     *
     * @param ids 主键列表
     * @return 数据列表（Mono）
     */
    default Mono<List<Entity>> listOnceByIds(Collection<Serializable> ids) {
        return Mono.just(getMapper().selectListByIds(ids));
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
        return Mono.just(
                !getMapper().selectObjectListByQuery(
                        QueryWrapper.create().where(query)
                                .limit(1)
                ).isEmpty()
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
        return Mono.just(getMapper().selectCountByQuery(QueryWrapper.create().where(query)));
    }

    /**
     * 根据条件查询数据总数
     *
     * @param query 条件
     * @return 数据总数
     */
    default Mono<Long> count(QueryWrapper query) {
        return Mono.just(getMapper().selectCountByQuery(query));
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
        return Mono.just(getMapper().paginateAs(page, query, asType));
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
