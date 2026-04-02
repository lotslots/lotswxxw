package com.lots.lots.config;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONObject;
import com.lots.lots.common.ApiException;
import com.lots.lots.dao.mapper.LotsServerExceptionLogMapper;
import com.lots.lots.common.JsonResult;
import com.lots.lots.common.ResultCode;
import com.lots.lots.entity.vo.LotsServerExceptionLogVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lots.lots.util.IpUtil.getIpAddress;

/**
 * @name: 全局异常处理
 * @author: lots
 * @date: 2021/4/28 11:38
 */

@RestControllerAdvice
public class GlobalExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private LotsServerExceptionLogMapper lotsServerExceptionLogMapper;

    @ExceptionHandler(value = ApiException.class)
    public JsonResult handle(ApiException e) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        LotsServerExceptionLogVo logPo = new LotsServerExceptionLogVo();
        logPo.setLogid(UUID.randomUUID().toString().replace("-", ""));
        logPo.setIpaddress(getIpAddress(request));
        logPo.setExceptionname(e.getMessage());
        logPo.setContent(getExceptionInfo(e).getBytes());
        logPo.setCreatetime(new Date());
        logPo.setUrl(request.getRequestURL().toString());
        logPo.setClientip(request.getRemoteAddr());
        logPo.setUseragent(request.getHeader("user-agent"));
        logPo.setRequestparameter(new JSONObject(request.getParameterMap()).toStringPretty().getBytes());
        lotsServerExceptionLogMapper.insert(logPo);
        logger.error(e.getMessage(), e);
        if (e.getErrorCode() != null) {
            return JsonResult.failed(e.getMessage());
        }
        return JsonResult.failed(e.getMessage());
    }
    /**
     * 处理 Exception 异常
     *
     * @param e 异常
     * @return 处理结果
     */
    @ExceptionHandler(Exception.class)
    public JsonResult handlerException(HttpServletRequest req, Exception e) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        LotsServerExceptionLogVo logPo = new LotsServerExceptionLogVo();
        logPo.setLogid(UUID.randomUUID().toString().replace("-", ""));
        logPo.setIpaddress(getIpAddress(request));
        logPo.setExceptionname(e.getMessage());
        logPo.setContent(getExceptionInfo(e).getBytes());
        logPo.setCreatetime(new Date());
        logPo.setUrl(request.getRequestURL().toString());
        logPo.setClientip(request.getRemoteAddr());
        logPo.setUseragent(request.getHeader("user-agent"));
        logPo.setRequestparameter(new JSONObject(request.getParameterMap()).toStringPretty().getBytes());
        lotsServerExceptionLogMapper.insert(logPo);
        logger.error(e.getMessage(), e);
        return new JsonResult(ResultCode.FAILED.getCode(), "系统开小差了~");
    }

    /**
     * 获取Exception详情
     *
     * @param ex
     * @return
     */
    public static String getExceptionInfo(Exception ex) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintStream pout = new PrintStream(out)) {
            ex.printStackTrace(pout);
            return out.toString();
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
            logger.warn("获取异常信息失败", e);
            return ex.getMessage();
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JsonResult handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        }
        return JsonResult.validateFailed(message);
    }

    @ExceptionHandler(value = BindException.class)
    public JsonResult handleValidException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        }
        return JsonResult.validateFailed(message);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public JsonResult handleValidException(ConstraintViolationException e) {
        String message = null;
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        message = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(";"));
        return JsonResult.validateFailed(message);
    }
}
