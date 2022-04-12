package com.lots.lots.entity.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import cn.hutool.core.date.DatePattern;


/**
 * 后台用户登录日志表(lots_user_login_log)
 *
 * @author lots
 * @version 1.0.0 2022-03-29
 */
@ApiModel(description = "后台用户登录日志表")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("lots_user_login_log")
public class LotsUserLoginLog implements java.io.Serializable {
   /**
    * 版本号
    */
    private static final long serialVersionUID = -1011255114687987872L;


    @ApiModelProperty(value = "主键ID")
    @TableId
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = DatePattern.NORM_DATETIME_PATTERN)
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "IP")
    private String ip;

    @ApiModelProperty(value = "地址")
    private String address;

    @ApiModelProperty(value = "浏览器登录类型")
    private String userAgent;
}