package com.lots.lots.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lots.lots.dao.mapper.LotsMenuMapper;
import com.lots.lots.dao.mapper.LotsRoleMapper;
import com.lots.lots.dao.mapper.LotsRoleMenuRelationMapper;
import com.lots.lots.dao.mapper.LotsRoleResourceRelationMapper;
import com.lots.lots.entity.vo.*;
import com.lots.lots.service.LotsRoleService;
import com.lots.lots.service.LotsUserCacheService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 后台用户角色表(LotsRole)表服务实现类
 *
 * @author lots
 * @since 2021-04-28
 */
@Service
public class LotsRoleServiceImpl implements LotsRoleService {
    @Resource
    private LotsRoleMapper lotsRoleMapper;
    @Resource
    private LotsUserCacheService lotsUserCacheService;
    @Resource
    private LotsMenuMapper lotsMenuMapper;
    @Resource
    private LotsRoleMenuRelationMapper lotsRoleMenuRelationMapper;
    @Resource
    private LotsRoleResourceRelationMapper lotsRoleResourceRelationMapper;


    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public LotsRoleVo findById(Long id) {
        return this.lotsRoleMapper.findById(id);
    }

    /**
     * 新增数据
     *
     * @param lotsRole 实例对象
     * @return 实例对象
     */
    @Override
    public LotsRoleVo insert(LotsRoleVo lotsRole) {
        this.lotsRoleMapper.insert(lotsRole);
        return lotsRole;
    }

    /**
     * 修改数据
     *
     * @param lotsRole 实例对象
     * @return 实例对象
     */
    @Override
    public Integer update(LotsRoleVo lotsRole) {
        return this.lotsRoleMapper.update(lotsRole);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Long id) {
        return this.lotsRoleMapper.deleteById(id.toString()) > 0;
    }


    @Override
    public List<LotsMenuVo> getMenuList(Long userId) {
        return lotsMenuMapper.getMenuList(userId);
    }

    @Override
    public int create(LotsRoleVo role) {
        role.setCreateTime(new Date());
        role.setAdminCount(0);
        role.setSort(0);
        return lotsRoleMapper.insert(role);
    }

    @Override
    public int deleteByIds(String ids) {
        int count = lotsRoleMapper.deleteById(ids);
        String[] splitIds = ids.split(",");
        List<Long> longIds = new ArrayList<>();
        for (String splitId : splitIds) {
            longIds.add(Long.valueOf(splitId));
        }
        lotsUserCacheService.delResourceListByRoleIds(longIds);
        return count;
    }


    @Override
    public List<LotsRoleVo> findAll() {
        return lotsRoleMapper.queryAll(new LotsRoleVo());
    }

    @Override
    public IPage<LotsRoleVo> pageList(String keyword, Integer pageSize, Integer pageNum) {
        LotsRoleVo lotsRoleVo = new LotsRoleVo();
        lotsRoleVo.setName(keyword);
        return lotsRoleMapper.queryAllPage(new Page(pageNum,pageSize),lotsRoleVo);
    }

    @Override
    public List<LotsMenuVo> getMenuByRoleId(Long roleId) {
        return lotsRoleMapper.getMenuListByRoleId(roleId);
    }

    @Override
    public List<LotsResourceVo> listResource(Long roleId) {
        return lotsRoleMapper.getResourceListByRoleId(roleId);
    }

    @Override
    public int allocMenu(Long roleId, String menuIds) {
        //先删除原有关系
        lotsRoleMenuRelationMapper.deleteByRoleId(roleId);
        //批量插入新关系
        List<LotsRoleMenuRelationVo> relationList = new ArrayList<>();
        String comma = ",";
        for (String menuId : menuIds.split(comma)) {
            LotsRoleMenuRelationVo relation = new LotsRoleMenuRelationVo();
            relation.setRoleId(roleId);
            relation.setMenuId(Long.valueOf(menuId));
            relationList.add(relation);
        }
        return lotsRoleMenuRelationMapper.insertList(relationList);
    }

    @Override
    public int allocResource(Long roleId, String resourceIds) {
        //先删除原有关系
        lotsRoleResourceRelationMapper.deleteByRoleId(roleId);
        //批量插入新关系
        List<LotsRoleResourceRelationVo> relationList = new ArrayList<>();
        String comma = ",";
        for (String resourceId : resourceIds.split(comma)) {
            LotsRoleResourceRelationVo relation = new LotsRoleResourceRelationVo();
            relation.setRoleId(roleId);
            relation.setResourceId(Long.valueOf(resourceId));
            relationList.add(relation);
        }
        int i = lotsRoleResourceRelationMapper.insertList(relationList);
        lotsUserCacheService.delResourceListByRole(roleId);
        return i;
    }
}