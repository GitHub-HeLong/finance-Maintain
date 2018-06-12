package com.es;

import org.elasticsearch.action.search.SearchResponse;

import com.alibaba.fastjson.JSONObject;

public interface EsDao {

	public void insertAlertProcessings(String index, String type, String _id,
			JSONObject json) throws Exception;

	public SearchResponse queryTryAlarm(String index, String eventTimeStart,
			String[] actualSituations) throws Exception;

	public SearchResponse queryAlarmType(String eventTimeStart, String sysCode)
			throws Exception;

}
