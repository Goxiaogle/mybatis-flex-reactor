package com.mybatis.flex.reactor.spring;

import com.mybatis.flex.reactor.core.ReactorService;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.service.IService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ReactorService 实现类
 * @param <Mapper> Mapper
 * @param <Entity> 实体类型
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ReactorServiceImpl<Mapper extends BaseMapper<Entity>, Entity> implements ReactorService<Entity> {

    /**
     * 外部注入 Mapper
     */
    @Autowired
    protected Mapper mapper;

    /**
     * 外部注入 IService 实现
     */
    @Autowired(required = false)
    protected IService<Entity> blockService;

    @Override
    public BaseMapper<Entity> getMapper() {
        return mapper;
    }

    @Override
    public IService<Entity> getBlockService() {
        if (blockService == null) {
            blockService = ReactorServiceImpl.this::getMapper;
        }
        return blockService;
    }
}
