package com.easy.tx.remote;

import com.easy.tx.constant.GlobalConfigCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * http远程客户端
 *
 * @author hzh
 * @date 2023/08/14
 */
public class HttpRemotingClient extends AbstractRemotingClient {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    
    public HttpRemotingClient() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(10000);
        httpRequestFactory.setConnectTimeout(10000);
        httpRequestFactory.setReadTimeout(10000);
        restTemplate.setRequestFactory(httpRequestFactory);
    }
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Object sendSyncRequest(RemoteMessage rpcMessage) {
        return jsonPost(rpcMessage.getAddressInfo().getTargetAddress(), rpcMessage);
    }
    
    @Override
    public void sendAsyncRequest(RemoteMessage rpcMessage) {
        jsonPost(rpcMessage.getAddressInfo().getTargetAddress(), rpcMessage);
    }
    
    /**
     * post 请求
     *
     * @param url 请求路径
     * @return
     */
    private Map jsonPost(String url, Object obj) {
        boolean http = StringUtils.startsWith(url, "http");
        if (!http) {
            url = "http://" + url;
        }
        url = url + "/easy/tx";
        String json = null;
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        HttpHeaders headers = new HttpHeaders();
        
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Content-Encoding", "UTF-8");
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
        return restTemplate.postForObject(url, requestEntity, Map.class);
    }
    
}
