package com.mysqlDao;

import java.util.List;
import java.util.Map;

public interface FinanceMysql {

	public void insertFinace(final Map<String, Object> map,
			final List<String> type, final String month);

	public List<Map<String, Object>> queryUserOrderByMonth(String month);

	public void insertDeviceZone(final List<Map<String, Object>> list);

	public void insertDeviceTyrZone(final List<Map<String, Object>> list,
			final List<Map<String, Object>> results);

	public void updateDeviceTyrZone(Map<String, Object> map, String month);

	public void updateEvent(String userId, String month, String D,
			String eventType);

	public void updateBCF(String month, String D, String userId, String devId);

}
