/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.restkeeper.service.impl;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.ymatou.restkeeper.dao.jpa.FunctionRepository;
import com.ymatou.restkeeper.model.pojo.Function;
import com.ymatou.restkeeper.model.pojo.OperationLog;
import com.ymatou.restkeeper.model.vo.FunctionParamVo;
import com.ymatou.restkeeper.model.vo.FunctionVo;
import com.ymatou.restkeeper.service.FunctionService;
import com.ymatou.restkeeper.service.OperationLogService;
import com.ymatou.restkeeper.util.Constants;
import com.ymatou.restkeeper.util.HttpClientUtil;

/**
 * 
 * @author qianmin 2016年8月2日 下午2:34:44
 *
 */
@Service
public class FunctionServiceImpl extends BaseServiceImpl<Function> implements FunctionService {
    
    private static final Logger logger = LoggerFactory.getLogger(FunctionServiceImpl.class);

    private FunctionRepository functionRepository;

    @Autowired
    private OperationLogService OperationLogService;

    @Autowired
    public FunctionServiceImpl(FunctionRepository functionRepository) {
        super(functionRepository);
        this.functionRepository = functionRepository;
    }

    @Override
    public String submit(FunctionVo function) {
        String request = null;
        String response = null;
        try {
            if (Constants.HTTP_METHOD_POST.equals(function.getHttpMethod())) {
                Map<String, String> requestMap = getRequestMap(function);
                request = JSON.toJSONString(requestMap);
                response = HttpClientUtil.sendGet(function.getUrl(), requestMap);
            } else {
                request = getRequestBody(function);
                response = HttpClientUtil.sendPost(function.getUrl(), request, function.getContentType());
            }
        } catch (Exception e) {
            logger.error("submit request failed. ", e);
            response = e.toString();
        }

        OperationLog operationLog = generateOperationLog(function, request, response);
        OperationLogService.save(operationLog);

        return response;
    }

    private OperationLog generateOperationLog(FunctionVo function, String request, String response) {
        OperationLog operationLog = new OperationLog();
        operationLog.setCreateTime(new Date());
        operationLog.setUpdateTime(new Date());
        operationLog.setOperateTime(new Date());
        operationLog.setFunctionId(function.getId());
        operationLog.setRequest(request);
        operationLog.setResponse(response);
        operationLog.setUserId(1222L); // TODO
        operationLog.setUserName(""); // TODO
        return operationLog;
    }

    private Map<String, String> getRequestMap(FunctionVo function) {
        Map<String, String> paramMap = new HashMap<>();
        for (FunctionParamVo functionParam : function.getFunctionParams()) {
            String name = functionParam.getName();
            String format = functionParam.getType();
            Object value = functionParam.getValue();
            if (format.equals("Date")) {
                value = new SimpleDateFormat(functionParam.getFormat()).format(value);
            }
            paramMap.put(name, String.valueOf(value));
        }
        return paramMap;
    }

    private String getRequestBody(FunctionVo function) {
        String paramJson = function.getFunctionParam();
        HashMap<String, Object> paramMap = new HashMap<>();
        for (FunctionParamVo functionParam : function.getFunctionParams()) {
            String name = functionParam.getName();
            String format = functionParam.getFormat();
            Object value = functionParam.getValue();
            if (functionParam.isArray()) {
                value = Arrays.asList(value.toString().split(","));
            }else{
                switch (functionParam.getType()) {
                    case Constants.FORMAT_STRING:
                        paramMap.put(name, String.valueOf(value));
                        break;
                    case Constants.FORMAT_NUMBER:
                        paramMap.put(name, value);
                        break;
                    case Constants.FORMAT_DATE:
                        paramMap.put(name, new SimpleDateFormat(format).format(value));
                        break;
                    default:
                        //TODO
                        break;
                }
            }
        }

        return StringUtils.isBlank(paramJson) ? JSON.toJSONString(paramMap) : paramJson;
    }

}