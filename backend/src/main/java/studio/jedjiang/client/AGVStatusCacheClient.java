package studio.jedjiang.client;

import java.util.HashMap;
import java.util.Map;

import studio.jedjiang.bean.AGVStatus;

public class AGVStatusCacheClient {

	public static AGVStatusCacheClient instance = null;

	// 缓存任务列表
	private Map<String, AGVStatus> _self = new HashMap<>();

	public static AGVStatusCacheClient getInstance() {
		if (instance == null) {
			instance = new AGVStatusCacheClient();
		}
		return instance;
	}

	public void put(String key, AGVStatus val) {
		_self.put(key, val);
	}

	public AGVStatus get(String key) {
		return _self.get(key);
	}

	public boolean has(String key) {
		return _self.containsKey(key);

	}

}
