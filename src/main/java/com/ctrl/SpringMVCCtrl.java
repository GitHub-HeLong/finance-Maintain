package com.ctrl;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.es.EsDao;
import com.mq.MQSender;
import com.mq.MqTopicSendServer;
import com.server.DeviceBCFService;
import com.server.EventService;
import com.server.SpringMVCService;
import com.tool.HttpTool;

@Controller
@RequestMapping("springMVCCtrl")
public class SpringMVCCtrl {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SpringMVCCtrl.class);

	@Resource
	SpringMVCService springMVCService;

	@Resource
	EventService eventService;

	@Resource
	MQSender mqSender;

	@Resource
	EsDao esDao;

	@Resource
	DeviceBCFService deviceBCFService;

	@Resource
	MqTopicSendServer mqTopicSendServer;

	@RequestMapping("requestService")
	@ResponseBody
	public JSONObject requestService(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// JSONObject json = new JSONObject();

		// 获取真警信息更新到数据事件表
		// String[] actualSituations = { "3", "6", "7" };
		// JSONObject json = eventService.updateEvents(actualSituations,
		// "processing", "isAlarm");

		// 获取误报信息更新到数据事件表
		// String[] actualSituations = { "4", "5", "8", "9", "10", "12" };
		String[] actualSituations = { "0003" };
		JSONObject json = eventService.updateEvents(actualSituations, "verify",
				"noAlarm");

		// 获取环境误报信息更新到数据事件表
		// String[] actualSituations = { "10" };
		// JSONObject json = eventService.updateEvents(actualSituations,
		// "verify",
		// "hj_error");

		// 获取人工误报信息更新到数据事件表
		// String[] actualSituations = { "9" };
		// JSONObject json = eventService.updateEvents(actualSituations,
		// "verify",
		// "rg_error");

		// 获取设备误报信息更新到数据事件表
		// String[] actualSituations = { "12" };
		// JSONObject json = eventService.updateEvents(actualSituations,
		// "verify",
		// "sb_error");

		// 有声劫盗
		// JSONObject json = eventService.updateAlarmEventService("E123",
		// "E123");

		// 无声劫盗
		// JSONObject json = eventService.updateAlarmEventService("E122",
		// "E122");

		// 出入防区
		// JSONObject json = eventService.updateAlarmEventService("E134",
		// "E134");

		// 周边防区
		// JSONObject json = eventService.updateAlarmEventService("E131",
		// "E131");

		// 盗窃
		// JSONObject json = eventService.updateAlarmEventService("E130",
		// "E130");

		// deviceBCFService.updateBCFService(); // 更新布撤防信息

		return json;
	}

	@RequestMapping("init")
	@ResponseBody
	public JSONObject init(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		JSONObject json = new JSONObject();

		LOGGER.info(" --- 初始化用户信息 --- ");

		springMVCService.queryFinanceService();// 获取用户信息表

		eventService.updateTryZoneAndAlarm(); // 更新试机表信息

		// 获取真警信息更新到数据事件表
		String[] actualSituations = { "3", "6", "7" };
		eventService.updateEvents(actualSituations, "processing", "isAlarm");

		// 获取误报信息更新到数据事件表
		String[] noAlarmActualSituations = { "4", "5", "8", "9", "10", "12" };
		eventService.updateEvents(noAlarmActualSituations, "verify", "noAlarm");

		// 获取环境误报信息更新到数据事件表 ，不需要，误报信息已经更新了这里
		// String[] hj_errorActualSituations = { "10" };
		// eventService.updateEvents(hj_errorActualSituations, "verify",
		// "hj_error");

		// 获取人工误报信息更新到数据事件表，不需要，误报信息已经更新了这里
		// String[] rg_errorActualSituations = { "9" };
		// eventService.updateEvents(rg_errorActualSituations, "verify",
		// "rg_error");

		// 获取设备误报信息更新到数据事件表，不需要，误报信息已经更新了这里
		// String[] sb_errorActualSituations = { "12" };
		// eventService.updateEvents(sb_errorActualSituations, "verify",
		// "sb_error");

		// 有声劫盗
		eventService.updateAlarmEventService("E123", "E123");

		// 无声劫盗
		eventService.updateAlarmEventService("E122", "E122");

		// 出入防区
		eventService.updateAlarmEventService("E134", "E134");

		// 周边防区
		eventService.updateAlarmEventService("E131", "E131");

		// 盗窃
		eventService.updateAlarmEventService("E130", "E130");

		// 更新布撤防信息
		deviceBCFService.updateBCFService();

		json.put("code", 200);
		json.put("msg", "success");
		return json;
	}

	@RequestMapping("sendMQToQueryService")
	@ResponseBody
	public void sendMQToQueryService(HttpServletRequest request,
			HttpServletResponse response) {
		mqSender.send("集成mq和mysql！");
	}

	@RequestMapping("sendMQToTopicService")
	@ResponseBody
	public void sendMQToTopicService(HttpServletRequest request,
			HttpServletResponse response) {
		mqTopicSendServer.sendMessage("发送广播 ！");
	}

	@RequestMapping("sendElasticsearch")
	@ResponseBody
	public JSONObject sendElasticsearch(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();

		try {
			String[] actualSituations = { "1" };

			SearchResponse result = esDao.queryTryAlarm("verify",
					"2018-06-01T00:00:00", actualSituations);
			SearchHits hits = result.getHits();
			SearchHit[] searchHits = hits.getHits();

			for (SearchHit hit : searchHits) {
				Map<String, Object> map = hit.sourceAsMap();
				System.out.println("accountNum :" + map.get("accountNum")
						+ "  zoneNum :" + map.get("zoneNum"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}

	@RequestMapping("sendHttpTool")
	@ResponseBody
	public void sendHttpTool(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			String str = HttpTool.post3(
					"http://10.0.17.19:8080/data-sync-up/check.do", "");
			LOGGER.info("str : {}", str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
