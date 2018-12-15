package studio.jedjiang;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class MainLauncherTest {
	public static void main(String[] args) {
//		String respCode = "sition;pause_stat=0;battery=144;error=0;x=-40172;y=-6866;a=269.216003;z=-9;gAlarm=1;speed=0;task=S6070.xml;veer_angle=-15.828571;task_step=0;task_isfinished=1;task_error=0;walk_path_id=-1�";
//	
//		AGVStatus.ofme(respCode);
		
		
//		String r = "SS100.xml";
//		boolean re = AGVClient.isTaskValid(r);
//		System.out.println(re);
		
		
		//System.out.println(String.valueOf("F1145".charAt(0)));
//		String fromSite = "T231567";
//		int len = fromSite.length();
//		String target = fromSite.substring(len -2);
//		System.out.println(target);
		Map<String, String> GetTask = ImmutableMap.<String, String>builder().put("T", "叫料任务,").put("F", "返仓任务,")
				.put("S", "待命任务,").put("O", "充电任务,").build();

		Map<String, String> GetGeo = ImmutableMap.<String, String>builder().put("00", "(00)待命").put("11", "E(1号位)")
				.put("12", "E(2号位)").put("21", "D(1号位)").put("22", "D(2号位)").put("31", "C(1号位)").put("32", "C(2号位)")
				.put("41", "B(1号位)").put("42", "B(2号位)").put("51", "A(1号位)").put("52", "A(2号位)").put("61", "机加(1号位)")
				.put("62", "机加(2号位)").put("70", "仓库(0号位)").put("71", "仓库(1号位)").put("72", "仓库(2号位)").put("80", "(80)待命")
				.put("81", "充电").build();
		
//		String name = "F801172";
//		String name = "T427061";
//		String name = "O110081";
		String name = "S710080";
		StringBuilder sb = new StringBuilder();
		String n = name.substring(0, 1);
		String from = name.substring(1, 3);
		String to = name.substring(5);
		String uname = sb.append(GetTask.get(n)).append("从").append(GetGeo.get(from)).append("到").append(GetGeo.get(to)).toString();
		System.out.println(uname);
	}
}
