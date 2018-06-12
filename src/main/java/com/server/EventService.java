package com.server;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.es.EsDao;
import com.mysqlDao.FinanceMysql;
import com.mysqlDao.OperationMysql;

/**
 * 用于每月自动获取金融行业用户的事件信息更新到信息表。 基本上查询ex
 * 
 * @author ywhl
 *
 */
@Service
public class EventService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SpringMVCService.class);

	@Resource
	OperationMysql operationMysql;

	@Resource
	FinanceMysql financeMysql;

	@Resource
	EsDao esDao;

	String[] sysCody = { "E123", "E122", "E134", "E131", "E130" };
	String[] errorActualSituation = { "9", "10", "12" };

	/**
	 * 此方法用于当服务部署的某天,更新月初到当天的试机信息，理论上只会调用一次
	 * 
	 * @return
	 * @throws Exception
	 */
	public JSONObject updateTryZoneAndAlarm() throws Exception {
		JSONObject json = new JSONObject();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
		Date date = new Date();
		String month = simpleDateFormat.format(date);

		String[] actualSituations = { "1" };

		// 查询用户本月试机设备
		SearchResponse result = null;
		try {
			result = esDao.queryTryAlarm("verify", month + "-01T00:00:00",
					actualSituations);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

		SearchHits hits = result.getHits();
		SearchHit[] searchHits = hits.getHits();
		LOGGER.info("试机用户数   size:{}  ", searchHits.length);

		for (SearchHit hit : searchHits) {
			Map<String, Object> map = hit.sourceAsMap();
			try {
				financeMysql.updateDeviceTyrZone(map, month); // 更新试机信息
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		json.put("code", 200);
		json.put("msg", "success");
		return json;
	}

	/**
	 * 此方法用于当服务部署的某天，更新月初到当天的事件信息，理论上只会调用一次
	 * actualSituations:单据中的报警原因类型，index：索引，eventType：事件表中的事件类型
	 * 
	 * @throws Exception
	 */
	public JSONObject updateEvents(String[] actualSituations, String index,
			String eventType) throws Exception {
		JSONObject json = new JSONObject();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
		Date date = new Date();
		String month = simpleDateFormat.format(date);

		// 查询处警单processing中用户本月真警事件
		LOGGER.info("获取表单信息  index：{}  ,报警原因 actualSituations：{}", index,
				Arrays.toString(actualSituations));
		SearchResponse result = null;
		try {
			result = esDao.queryTryAlarm(index, month + "-01T00:00:00",
					actualSituations);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

		SearchHits hits = result.getHits();
		SearchHit[] searchHits = hits.getHits();

		for (SearchHit hit : searchHits) {

			Map<String, Object> map = hit.sourceAsMap();

			LOGGER.info(" map :" + map.toString());

			String eventTime = map.get("eventTime").toString().substring(0, 10);

			String D = eventTime.substring(8, 9).equals("0") ? eventTime
					.substring(9, 10) : eventTime.substring(8, 10);

			String actualSituation = (String) map.get("actualSituation");

			try {
				financeMysql.updateEvent(map.get("accountNum").toString(),
						eventTime.substring(0, 7), D, eventType);
				if (Arrays.asList(errorActualSituation).contains(
						actualSituation)) { // 如果报警属于环境、人工、设备误报（9.10.12）则这3类响应加1
					financeMysql.updateEvent(map.get("accountNum").toString(),
							eventTime.substring(0, 7), D, actualSituation);
				}

			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		json.put("code", 200);
		json.put("msg", "success");
		return json;
	}

	/**
	 * 此方法用于当服务部署的某天,更新月初到当天的报警事件信息，理论上只会调用一次
	 * 
	 * @return
	 * @throws Exception
	 */
	public JSONObject updateAlarmEventService(String sysCode, String eventType)
			throws Exception {
		JSONObject json = new JSONObject();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
		Date date = new Date();
		String month = simpleDateFormat.format(date);

		// 查询处警单processing中用户本月真警事件
		LOGGER.info("获真警类型信息  sysCode：{},报警原因 eventType：{}", sysCode, eventType);
		SearchResponse result = null;
		try {
			result = esDao.queryAlarmType(month + "-01T00:00:00", sysCode);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

		SearchHits hits = result.getHits();
		SearchHit[] searchHits = hits.getHits();

		for (SearchHit hit : searchHits) {
			Map<String, Object> map = hit.sourceAsMap();

			String eventTime = map.get("eventTime").toString().substring(0, 10);

			String D = eventTime.substring(8, 9).equals("0") ? eventTime
					.substring(9, 10) : eventTime.substring(8, 10);

			try {
				financeMysql.updateEvent(map.get("accountNum").toString(),// 事件类型的报警不是真警，所以总的报警不需要加1
						eventTime.substring(0, 7), D, eventType);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		json.put("code", 200);
		json.put("msg", "success");
		return json;
	}

}
