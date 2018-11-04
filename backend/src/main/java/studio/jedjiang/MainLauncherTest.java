package studio.jedjiang;

import studio.jedjiang.client.AGVClient;

public class MainLauncherTest {
	public static void main(String[] args) {
//		String respCode = "sition;pause_stat=0;battery=144;error=0;x=-40172;y=-6866;a=269.216003;z=-9;gAlarm=1;speed=0;task=S6070.xml;veer_angle=-15.828571;task_step=0;task_isfinished=1;task_error=0;walk_path_id=-1�";
//	
//		AGVStatus.ofme(respCode);
		
		
//		String r = "SS100.xml";
//		boolean re = AGVClient.isTaskValid(r);
//		System.out.println(re);
		
		
		//System.out.println(String.valueOf("F1145".charAt(0)));
		String fromSite = "T231567";
		int len = fromSite.length();
		String target = fromSite.substring(len -2);
		System.out.println(target);
	}
}
