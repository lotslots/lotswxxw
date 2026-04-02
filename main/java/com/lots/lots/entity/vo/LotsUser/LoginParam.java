package com.lots.lots.entity.vo.LotsUser;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import com.lots.lots.common.validation.Select;
/**
 * LoginParam
 *
 * @author lots
 * @date 2022/3/29 17:39
 */
@Data
@ApiModel("登录接口请求数据")
public class LoginParam implements Serializable {

    @ApiModelProperty(value = "用户名")
    @NotNull(message = "用户名不能为空" , groups = {Select.class})
    private String username;

    @ApiModelProperty(value = "密码")
    @NotNull(message = "密码不能为空", groups = {Select.class})
    private String password;
}
