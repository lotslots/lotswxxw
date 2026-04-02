package com.lots.lots.controller.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lots.lots.common.*;
import com.lots.lots.entity.vo.LotsRoleVo;
import com.lots.lots.entity.vo.LotsUser.LoginParam;
import com.lots.lots.entity.vo.LotsUser.RegisterParam;
import com.lots.lots.entity.vo.LotsUser.UpdateParam;
import com.lots.lots.entity.vo.LotsUser.UpdateRoleParam;
import com.lots.lots.entity.vo.LotsUserVo;
import com.lots.lots.entity.vo.UpdateAdminPasswordParam;
import com.lots.lots.service.LotsRoleService;
import com.lots.lots.service.LotsUserService;
import com.lots.lots.util.CaptchaUtils;
import com.lots.lots.util.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.lots.lots.common.validation.*;
/**
 * 后台用户表(LotsUser)表控制层
 *
 * @author lots
 * @since 2021-04-28
 */
@RestController
@RequestMapping("admin")
@Api(tags = "后台用户管理")
public class LotsUserController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LotsUserController.class);

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Resource
    private LotsUserService adminService;
    @Resource
    private LotsRoleService roleService;
    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "用户注册")
    @PostMapping(value = "/register")
    public JsonResult register(@RequestBody @Validated(Insert.class) RegisterParam registerParam ) {

        return adminService.register(registerParam);
    }

    @ApiOperation(value = "登录以后返回token")
    @PostMapping(value = "/login")
    public JsonResult login( @RequestBody @Validated(Select.class) LoginParam lotsUserVo) {
        String token = adminService.login(lotsUserVo.getUsername(), lotsUserVo.getPassword());
        if (token == null) {
            return JsonResult.failed("用户名或密码错误");
        }
        Map<String, String> tokenMap = new HashMap<>(20);
        tokenMap.put("token", tokenHead + token);
        return JsonResult.success(tokenMap);
    }

    @ApiOperation(value = "刷新token")
    @PostMapping(value = "/refreshToken")
    public JsonResult refreshToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        String refreshToken = adminService.refreshToken(token);
        if (refreshToken == null) {
            return JsonResult.failed("token已经过期！");
        }
        Map<String, String> tokenMap = new HashMap<>(20);
        tokenMap.put("token", tokenHead + refreshToken);
        return JsonResult.success(tokenMap);
    }

    @ApiOperation(value = "获取当前登录用户信息")
    @PostMapping(value = "/info")
    public JsonResult getAdminInfo(Principal principal) {
        if (principal == null) {
            return JsonResult.unauthorized(null);
        }
        String username = principal.getName();
        LotsUserVo lotsUser = adminService.getAdminByUsername(username);
        Map<String, Object> data = new HashMap<>(20);
        data.put("username", lotsUser.getUsername());
        data.put("menus", roleService.getMenuList(lotsUser.getId()));
        data.put("icon", lotsUser.getIcon());
        List<LotsRoleVo> roleList = adminService.getRoleList(lotsUser.getId());
        if (CollUtil.isNotEmpty(roleList)) {
            List<String> roles = roleList.stream().map(LotsRoleVo::getName).collect(Collectors.toList());
            data.put("roles", roles);
        }
        return JsonResult.success(data);
    }

    @ApiOperation(value = "登出功能")
    @PostMapping(value = "/logout")
    public JsonResult logout() {
        return JsonResult.success(null);
    }

    @ApiOperation("根据用户名或姓名分页获取用户列表")
    @GetMapping(value = "/list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword", value = "用户名或姓名", dataType = "string", dataTypeClass = String.class, paramType="query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", dataTypeClass = Integer.class, paramType="query", defaultValue = "10", required = true),
            @ApiImplicitParam(name = "pageNum", value = "第几页", dataType = "int", dataTypeClass = Integer.class, paramType="query", defaultValue = "1", required = true)
    })
    public JsonResult<Paging<LotsUserVo>> list(String keyword,
                                               @NotNull(message = "每页数量不能为空")Integer pageSize,
                                               @NotNull(message = "第几页不能为空")Integer pageNum) {
        IPage<LotsUserVo> adminList = adminService.list(keyword, pageSize, pageNum);
        return JsonResult.success(Paging.buildPaging(adminList));
    }

    @ApiOperation("获取指定用户信息")
    @GetMapping(value = "/get/")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户ID", dataType = "long", dataTypeClass = Long.class, paramType="query", required = true)
    })
    public JsonResult<LotsUserVo> getItem( @NotNull(message = "id不能为空")Long id) {
        LotsUserVo admin = adminService.findById(id);
        return JsonResult.success(admin);
    }

    @ApiOperation("修改指定用户信息")
    @PostMapping(value = "/update/")
    public JsonResult update( @RequestBody @Validated(Update.class) UpdateParam admin) {
        int count = adminService.update(admin);
        if (count > 0) {
            return JsonResult.success(count);
        }
        return JsonResult.failed();
    }

    @ApiOperation("修改指定用户密码")
    @PostMapping(value = "/updatePassword")
    public JsonResult updatePassword(@RequestBody @Validated(Update.class) UpdateAdminPasswordParam updatePasswordParam) {
        int status = adminService.updatePassword(updatePasswordParam);
        Integer parameterInvalid = -1;
        Integer notFoundUser = -2;
        Integer oldPasswordError = -3;
        if (status > 0) {
            return JsonResult.success(status);
        } else if (status == parameterInvalid) {
            return JsonResult.failed("提交参数不合法");
        } else if (status == notFoundUser) {
            return JsonResult.failed("找不到该用户");
        } else if (status == oldPasswordError) {
            return JsonResult.failed("旧密码错误");
        } else {
            return JsonResult.failed();
        }
    }

    @ApiOperation("删除指定用户信息")
    @GetMapping(value = "/delete/")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户ID", dataType = "long", dataTypeClass = Long.class, paramType="query", required = true)
    })
    public JsonResult delete(@NotNull(message = "用户ID不能为空")Long id) {
        int count = adminService.delete(id);
        if (count > 0) {
            return JsonResult.success(count);
        }
        return JsonResult.failed();
    }

    @ApiOperation("修改帐号状态")
    @GetMapping(value = "/updateStatus/")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户ID", dataType = "long", dataTypeClass = Long.class, paramType="query", required = true),
            @ApiImplicitParam(name = "status", value = "帐号启用状态：0->禁用；1->启用", dataType = "int", dataTypeClass = Integer.class, paramType="query", required = true)
    })
    public JsonResult updateStatus( @NotNull(message = "用户ID不能为空")Long id,
                                    @NotNull(message = "状态不能为空")Integer status) {
        UpdateParam lotsUser = new UpdateParam();
        lotsUser.setStatus(status);
        lotsUser.setId(id);
        int count = adminService.update(lotsUser);
        if (count > 0) {
            return JsonResult.success(count);
        }
        return JsonResult.failed();
    }

    @ApiOperation("给用户分配角色")
    @PostMapping(value = "/role/update")
    public JsonResult updateRole(@RequestBody @Validated(Update.class) UpdateRoleParam updateRoleParam) {
        int count = adminService.updateRole(updateRoleParam.getUserId(), updateRoleParam.getRoleIds());
        if (count >= 0) {
            return JsonResult.success(count);
        }
        return JsonResult.failed();
    }

    @ApiOperation("获取指定用户的角色")
    @GetMapping(value = "/role/get/")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", dataType = "long", dataTypeClass = Long.class, paramType="query", required = true)
    })
    public JsonResult<List<LotsRoleVo>> getRoleList(@NotNull(message = "用户ID不能为空") Long userId) {
        List<LotsRoleVo> roleList = adminService.getRoleList(userId);
        return JsonResult.success(roleList);
    }

    @ApiOperation("获取验证码图片")
    @GetMapping(value = "/getCaptcha")
    public JsonResult getCaptcha() {
        CaptchaVo captchaVo = CaptchaUtils.createCaptchaImage(CaptChaiPo.builder().build());
        String captcha = captchaVo.getCaptcha();
        BufferedImage captchaImage = captchaVo.getCaptchaImage();

        String captchaKey = cn.hutool.core.lang.UUID.randomUUID().toString(true);
        String captchaRedisKey = String.format(CaptchaUtils.CAPTCHA_REDIS_PREFIX, captchaKey);
        redisTemplate.opsForValue().set(captchaRedisKey, captcha, 300, TimeUnit.SECONDS);

        HttpServletResponse response = ServletUtils.getResponse();
        response.setContentType("image/png");
        try (OutputStream output = response.getOutputStream()) {
            ImageIO.write(captchaImage, "png", output);
        } catch (IOException e) {
            LOGGER.error("生成验证码图片失败", e);
        }

        Map<String, String> result = new HashMap<>(2);
        result.put("captchaKey", captchaKey);
        return JsonResult.success(result);
    }


}
