package com.aiimage.aspect;

import com.aiimage.util.BaseContextUtil;
import com.aiimage.util.ExtraParamUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;


/**
 * @author zzl
 * @date 2026-05-01 13:57
 */

@Component
@Aspect
@Order(1)
@Slf4j
public class HttpAspect {

    public static final String TRACE_ID = "trace-id";

    @Value("${spring.application.name}")
    private String appName;

    /**
     * 是否打印返回日志
     */
    @Value("${backend.common.HttpAspect.showAfterReturn: true}")
    private boolean showAfterReturn;

    /**
     * 返回内容截取长度，默认不截取（前提条件：启用打印返回日志）
     */
    @Value("${backend.common.HttpAspect.afterReturnCutLength: }")
    private Integer afterReturnCutLength;

    /**
     * 是否打印请求耗时区间，便于统计慢请求
     */
    @Value("${backend.common.HttpAspect.showRequestUsage: true}")
    private boolean showRequestUsage;

    @Pointcut("execution(public * com.aiimage..*Controller.*(..))")
    public void webLog() {
        // do noting
    }

    @Before("webLog()")
    public void doBefore() {
        // do noting
    }

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        //获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        String traceId = (String) BaseContextUtil.get(TRACE_ID);
        MDC.put(TRACE_ID, (traceId != null && traceId.length() >= 6 ? traceId.substring(0, 6) : traceId) + " ");
        if (request != null) {
            log.info("请求IP:{} 方式:{} URL:{} 参数:{}", request.getRemoteAddr(), request.getMethod(), request.getRequestURL(), JSON.toJSONString(getParameter(method, joinPoint.getArgs())));
        }
        log.info("请求类名:{},方法名:{},账号:{},traceId:{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), getCurrentUserId(), traceId);

        Object retVal;
        Object[] args = joinPoint.getArgs();
        try {
            retVal = joinPoint.proceed(args);
        } catch (Exception e) {
            log.info("请求异常:{} {}", e.getMessage(), e.getClass().getSimpleName());
            long endTime = System.currentTimeMillis();

            logUsageTime(endTime - startTime, request);
            MDC.remove(TRACE_ID);
            throw e;
        }
        long endTime = System.currentTimeMillis();
        logUsageTime(endTime - startTime, request);
        MDC.remove(TRACE_ID);
        return retVal;
    }

    private void logUsageTime(long usageTime, HttpServletRequest request) {
        if (usageTime > 20 && usageTime < 50) {
            log.info("请求耗时20_50 {}ms URL:{}", usageTime, request.getRequestURL());
        } else if (usageTime >= 50 && usageTime < 100) {
            log.info("请求耗时50_100 {}ms URL:{}", usageTime, request.getRequestURL());
        } else if (usageTime >= 100 && usageTime < 200) {
            log.info("请求耗时100_200 {}ms URL:{}", usageTime, request.getRequestURL());
        } else if (usageTime >= 200 && usageTime < 500) {
            log.info("请求耗时200_500 {}ms URL:{}", usageTime, request.getRequestURL());
        } else if (usageTime >= 500) {
            log.info("请求耗时500_ {}ms URL:{}", usageTime, request.getRequestURL());
        } else {
            log.info("请求耗时:{}ms", usageTime);
        }
    }

    /**
     * 根据方法和传入的参数获取请求参数
     */
    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                String key = parameters[i].getName();
                if (StringUtils.isNotBlank(requestParam.value())) {
                    key = requestParam.value();
                }
                if (args[i] instanceof MultipartFile) {
                    map.put(key, "MultipartFile Object");
                } else {
                    map.put(key, args[i]);
                }
                argList.add(map);
            }
        }
        if (argList.isEmpty()) {
            return null;
        } else if (argList.size() == 1) {
            return argList.get(0);
        } else {
            return argList;
        }
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) {
        // 给返回体设置 traceId
        if (ret instanceof com.aiimage.dto.ApiResponse) {
            // ApiResponse doesn't have traceId, skip setting
        }

        if (showAfterReturn) {
            // 处理完请求，返回内容
            String content = JSON.toJSONString(ret);
            if (Objects.nonNull(afterReturnCutLength) && afterReturnCutLength > 0 && content.length() > afterReturnCutLength) {
                content = content.substring(0, afterReturnCutLength);
            }
            log.info("[返回内容]:{}|{}|{}", content, ExtraParamUtil.getExtraParam().getTraceId(), appName);
        }
        //清除当前线程
        ExtraParamUtil.destroy();
    }

    @Resource
    protected HttpServletRequest request;

    protected Long getCurrentUserId() {
        return BaseContextUtil.getCurrentUserId();
    }
}
