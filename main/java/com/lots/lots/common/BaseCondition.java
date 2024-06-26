package com.lots.lots.common;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 查询条件的基础类，用于继承
 *
 * @author lots
 * @version 1.0.0 2022-03-24
 */
public abstract class BaseCondition extends PagingCondition {
    /** 版本号 */
    private static final long serialVersionUID = -2369459314543352900L;

    /**
     * 创建查询条件构造器，将对象或Map转Bean对象，
     * 并把对象或Map中的值拷贝给Bean对象，拷贝进来的这些值会自动产生查询条件
     *
     * @param clazz 目标的Bean类型
     * @return 查询条件构造器
     */
    public <T> QueryWrapper<T> buildQueryWrapper(Class<T> clazz) {
        T entity = BeanUtil.toBean(this, clazz);
        return new QueryWrapper<>(entity);
    }

    /**
     * 创建查询条件构造器，不会自动产生查询条件
     *
     * @return 查询条件构造器
     */
    public <T> QueryWrapper<T> buildQueryWrapper() {
        return new QueryWrapper<>();
    }
}