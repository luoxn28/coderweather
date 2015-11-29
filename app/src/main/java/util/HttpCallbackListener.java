package util;

/**
 * Created by luoxn28 on 2015/11/29.
 * HttpCallbackListener用于回调服务返回的结果
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception ex);
}
