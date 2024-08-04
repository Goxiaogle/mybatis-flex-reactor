package com.mybatis.flex.reactor.spring;

import com.mybatis.flex.reactor.core.ReactorService;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.service.IService;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ReactorServiceImpl<Mapper extends BaseMapper<Entity>, Entity> implements ReactorService<Entity> {

    @Autowired
    protected Mapper mapper;

    @Autowired(required = false)
    protected IService<Entity> blockService;

    @Override
    public BaseMapper<Entity> getMapper() {
        return mapper;
    }

    /**
     * 获取阻塞式非响应式 Service。默认会注入已有的 IService 实现，如果没有则会尝试创建一个默认的 IService 实现。
     * @return IService 实现对象
     */
    @Override
    public IService<Entity> getBlockService() {
        if (blockService == null) {
            blockService = ReactorServiceImpl.this::getMapper;
        }
        return blockService;
    }
}
