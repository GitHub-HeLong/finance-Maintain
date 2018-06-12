package com.es.imp;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.es.ESUtils;
import com.es.EsDao;

@Repository
public class EsDaoImpl implements EsDao {
	private final static Logger LOGGER = LoggerFactory
			.getLogger(EsDaoImpl.class);

	public void insertAlertProcessings(String index, String type, String _id,
			JSONObject json) throws Exception {
		try {
			IndexResponse response = ESUtils.client
					.prepareIndex(index, type, _id).setSource(json).execute()
					.actionGet();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	// 根据月份,报警原因类型,去EX表中查询处警单、核警单的试机，真警，误报事件
	public SearchResponse queryTryAlarm(String index, String eventTimeStart,
			String[] actualSituations) throws Exception {

		try {
			BoolQueryBuilder boolQuery = new BoolQueryBuilder();

			// boolQuery.must(QueryBuilders.rangeQuery("eventTime").gte(
			// eventTimeStart));

			// boolQuery.must(QueryBuilders.existsQuery("zoneNum"));

			boolQuery.must(QueryBuilders
					.termsQuery("zoneNum", actualSituations));
			// boolQuery.must(QueryBuilders.termsQuery("actualSituation",
			// actualSituations));

			SearchResponse searchResponse = ESUtils.client.prepareSearch(index)
					.setQuery(boolQuery).execute().actionGet();

			return searchResponse;
		} catch (Exception e) {
			throw e;
		}
	}

	// 根据月份，系统码，去EX的真警中查询有声劫盗，无声劫盗，出入防区，周边防区，盗取
	public SearchResponse queryAlarmType(String eventTimeStart, String sysCode)
			throws Exception {
		try {
			BoolQueryBuilder boolQuery = new BoolQueryBuilder();

			boolQuery.must(QueryBuilders.rangeQuery("eventTime").gte(
					eventTimeStart));
			boolQuery.must(QueryBuilders.termsQuery("sysCode", sysCode));

			SearchResponse searchResponse = ESUtils.client
					.prepareSearch("alert_processing").setQuery(boolQuery)
					.execute().actionGet();

			return searchResponse;
		} catch (Exception e) {
			throw e;
		}
	}

}
