package com.lots.lots.common;

import cn.hutool.json.JSONObject;
import com.lots.lots.util.XssSqlHttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于其他Controller继承的基础Controller
 *
 * @author lots
 * @version 1.0.0 2022-03-24
 */
@Validated
public abstract class BaseController {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);
    @Resource
    protected HttpServletRequest request;

    @Resource
    protected HttpServletResponse response;

    private static final String STR_BODY = "body";

    /**
     * description 取request中的已经被防止XSS，SQL注入过滤过的key value数据封装到map 返回
     *
     * @param request 1
     * @return java.util.Map<java.lang.String, java.lang.String>
     */
    private static Map<String, String> getRequestParameters(ServletRequest request) {
        Map<String, String> dataMap = new HashMap<>(16);
        Enumeration enums = request.getParameterNames();
        while (enums.hasMoreElements()) {
            String paraName = (String) enums.nextElement();
            String paraValue = getRequest(request).getParameter(paraName);
            if (null != paraValue && !"".equals(paraValue)) {
                dataMap.put(paraName, paraValue);
            }
        }
        return dataMap;
    }

    /**
     * description 获取request中的body json 数据转化为map
     *
     * @param request 1
     * @return java.util.Map<java.lang.String, java.lang.String>
     */
    @SuppressWarnings("unchecked")
    private static Map<String, String> getRequestBodyMap(ServletRequest request) {
        Map<String, String> dataMap = new HashMap<>(16);
        // 判断是否已经将 inputStream 流中的 body 数据读出放入 attribute
        if (request.getAttribute(STR_BODY) != null) {
            // 已经读出则返回attribute中的body
            return (Map<String, String>) request.getAttribute(STR_BODY);
        } else {
            try {
                Map<String, String> maps = new JSONObject(request.getInputStream()).toBean(Map.class);
                dataMap.putAll(maps);
                request.setAttribute(STR_BODY, dataMap);
            } catch (IOException e) {
                LOGGER.error("读取request body失败", e);
            }
            return dataMap;
        }
    }

    /**
     * description 读取request 已经被防止XSS，SQL注入过滤过的 请求参数key 对应的value
     *
     * @param request 1
     * @param key     2
     * @return java.lang.String
     */
    private static String getParameter(ServletRequest request, String key) {
        return getRequest(request).getParameter(key);
    }

    /**
     * description 读取request 已经被防止XSS，SQL注入过滤过的 请求头key 对应的value
     *
     * @param request 1
     * @param key     2
     * @return java.lang.String
     */
    private static String getHeader(ServletRequest request, String key) {
        return getRequest(request).getHeader(key);
    }

    /**
     * description 取request头中的已经被防止XSS，SQL注入过滤过的 key value数据封装到map 返回
     *
     * @param request 1
     * @return java.util.Map<java.lang.String, java.lang.String>
     */
    private static Map<String, String> getRequestHeaders(ServletRequest request) {
        Map<String, String> headerMap = new HashMap<>(16);
        Enumeration enums = getRequest(request).getHeaderNames();
        while (enums.hasMoreElements()) {
            String name = (String) enums.nextElement();
            String value = getRequest(request).getHeader(name);
            if (null != value && !"".equals(value)) {
                headerMap.put(name, value);
            }
        }
        return headerMap;
    }

    protected Map<String, String> getRequestParameter(HttpServletRequest request) {

        return getRequestParameters(request);
    }

    protected Map<String, String> getRequestBody(HttpServletRequest request) {
        return getRequestBodyMap(request);
    }

    protected Map<String, String> getRequestHeader(HttpServletRequest request) {
        return getRequestHeaders(request);
    }

    /**
     * description 封装response  统一json返回
     *
     * @param outStr   1
     * @param response 2
     */
    protected void responseWrite(String outStr, ServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        try {
            response.getWriter().write(outStr);
        } catch (Exception e) {
            LOGGER.error("写入response失败", e);
        }
    }

    private static HttpServletRequest getRequest(ServletRequest request) {
        return new XssSqlHttpServletRequestWrapper((HttpServletRequest) request);
    }
}
