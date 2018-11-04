package studio.jedjiang.client;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import com.google.common.collect.Lists;

import studio.jedjiang.bean.AGVStatus;

public class AGVClient {

	private static final Log log = Logs.get();
	public static String ONE_AVG_ID = "ONLY_FOR_ONE_AVG";

	public static final String SEND_CMD_PATTERN = "cmd=set task by name;name={CODE}.xml";
	public static final String END_CMD_PATTERN = "cmd=pause;pauseStat=1";
	public static final String REC_CMD_PATTERN = "cmd=pause;pauseStat=0";
	public static final String DEFAULT_DB_TASK_ID = "AGVUniqueId_dde94b0e9e87f376efd80c30";

	// 缓存：保存最后一次任务对象
	public static AGVStatusCacheClient agvCacheClient = AGVStatusCacheClient.getInstance();
	public static boolean hasNextTask = false;

	public static final String NO_TASK = "no_task";
	
	// 高于该值表示已充满电
	public static int CHARGE_FULL_MAX_VAL = 99;
	// 低于该值表示需要充电
	public static int CHARGE_LOWER_MIN_VAL= 20;
	// 电量高于该值可以继续接任务
	public static int CHARGE_RECOVER_MIN_VAL = 40;
	
	// 定子，转子，返仓
	public static final String DZ_NUM = "1";
	public static final String ZZ_NUM = "2";
	public static final String FC_NUM = "0";
	public static final String CHARGE_NUM = "71";
	
	// 定子，转子，返仓
	public static final String DZ_FLAG = "D";
	public static final String ZZ_FLAG = "Z";
	public static final String FC_FLAG = "F";
	
	// 需要改变起始点的站点
	public static final List<String> NEED_CHANGED_FROM_SITE = Lists.newArrayList("10","20","30","40","50");
	
	/**
	 * 检测心跳超时时间
	 */
	public static final int TIMEOUT = 5000;

	/**
	 * 发送的报文
	 */
	public static String createSendCommad(String reqCode) {
		int pos = reqCode.indexOf(".xml");
		if(pos != -1) {
			reqCode = reqCode.substring(0, pos);
		}
		return SEND_CMD_PATTERN.replace("{CODE}", reqCode);
	}

	/**
	 * 创建结束命令
	 */
	public static String createEndCommad() {
		return END_CMD_PATTERN;
	}

	/**
	 * 创建恢复执行的命令
	 */
	public static String createRecCommad() {
		return REC_CMD_PATTERN;
	}
	
	/**
	 * 解析返回报文
	 * 
	 * @param agvResponse 服务器返回的报文
	 * @return
	 */
	public static AGVStatus updateCache(String agvResponse) {
		AGVStatus me = null;
		try {
			me = AGVStatus.ofme(agvResponse);
		} catch (Exception e) {
			log.error("报文解析出错：ofme() error：" + agvResponse);
			// 报文解析出错，则清空缓存
			agvCacheClient.put(ONE_AVG_ID, null);
			// e.printStackTrace();
			return null;
		}
		
		// 检测任务名是否合法
		if(me!=null && isTaskValid(me.getTaskName())) {
			return null;
		}
		
		agvCacheClient.put(ONE_AVG_ID, me);

		// log.infof("报文解析成功: 当前任务:%s",me.getTaskName());

		return me;
	}

	/**
	 * 获取回待命区的指令 
	 */
	// for example : Z3221
	public static String getBackSite(String fromSite){
		// 定子转子回待命区 SXX00, 返仓回待命区 S6070, 充电回待命区S7170
		fromSite = fromSite.replace(".xml", "").trim();
		// 截取末尾两位，判断是几号仓
		String target = fromSite.substring(fromSite.length() - 2);
		// 123456号仓回到00
		String route = target.substring(0, 1);
		if("123456".contains(route)){
			return "S" + target + "0000";
		}
		if("78".contains(route)){
			return "S" + target + "0080";
		}
		return route;
	}
	
	/**
	 * 获取当前站点到充电站的任务
	 */
	public static String getFromSiteToChargeSite(String fromSite){
		fromSite = fromSite.replace(".xml", "").trim().substring(fromSite.length() -2);
		// fix: 新增充电任务的时候有可能需要改变起始站点
		return "O" + fromSite + "0081";
	}
	
	// 判断发送的任务是否有效
	public static boolean isTaskValid(String taskName) {
		if(Strings.isBlank(taskName)) {
			return false;
		}
		if(taskName.equals(NO_TASK)) {
			return false;
		}
		if(taskName.replace(".xml", "").trim().length() != 7) {
			return false;
		}
		
		// 第一个字符必须非数字，后面四个字符必须都是数字
		boolean match = Strings.isNumber(taskName.substring(1)) && !Strings.isNumber(taskName.substring(0,1));
		if(!match) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 打印错误堆栈信息
	 */
	public static String getStackMsg(Throwable e) {

		StringBuffer sb = new StringBuffer();
		StackTraceElement[] stackArray = e.getStackTrace();
		for (int i = 0; i < stackArray.length; i++) {
			StackTraceElement element = stackArray[i];
			sb.append(element.toString() + "\n");
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		// String word = "Sty679";
		// System.out.println(word.substring(2));
				
		
		
	}

}
