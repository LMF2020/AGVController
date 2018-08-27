package studio.jedjiang.bean;

import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import studio.jedjiang.client.AGVClient;

/**
 * agv任务状态
 * 
 * @author Jed
 *
 */
public class AGVStatus {
	
	private static final Log log = Logs.get();
	
	public static int IN_PROGRESS = 0;
	public static int FINISHED = 1;

	// 车子编号
	private String avgCode;
	private String taskName;
	private int battery;
	private boolean isFinished;
	private String error;
	
	private String x;
	private String y;
	/**
	 * 创建实时状态
	 * 
	 * @param respCode
	 * @return
	 */
	public static AGVStatus ofme(String agvResponse) {
		
		if (Strings.isBlank(agvResponse)) {
			log.error("报文为空");
			return null;
		}
		
		if (!agvResponse.contains("cmd=") || !agvResponse.contains("battery=") || !agvResponse.contains("task_isfinished=")
				|| !agvResponse.contains("task=")) {
			log.error("报文格式不对：format() error：" + agvResponse);
			return null;
		}

		AGVStatus me = new AGVStatus();
		String[] kv = agvResponse.trim().split(";");

		for (String item : kv) {
			String[] pair = item.split("=", 2);
			if(pair.length != 2) {
				// 忽略所有格式错误的报文
				continue;
			}
			String pair_k = pair[0].trim();
			String pair_v = pair[1].trim();
			
			if (pair_k.equals("task")) {
				// 有值了就直接跳过
				if(me.getTaskName() != null) {
					continue;
				}
				// 只设值一次
				if(Strings.isBlank(pair_v)){
					me.setTaskName(AGVClient.NO_TASK);
				}else {
					me.setTaskName(pair_v);
				}
			} else if (pair_k.equals("battery")) { // 电量
				int powerleft = 100;
				if(Strings.isNotBlank(pair_v)) {
					powerleft = Integer.parseInt(pair_v);
				}
				me.setBattery(powerleft);
			} else if (pair_k.equals("task_isfinished")) {
				me.setFinished("1".equals(pair_v));
			} else if (pair_k.equals("task_error")) {
				me.setError(pair_v);
			} else if(pair_k.equals("x")) {
				me.setX(pair_v);
			} else if(pair_k.equals("y")) {
				me.setY(pair_v);
			}
		}

		return me;
	}

	/**
	 * 更新实时状态
	 * 
	 * @param me
	 */
	public void update(AGVStatus me) {
		this.setBattery(me.getBattery());
		this.setError(me.getError());
		this.setFinished(me.isFinished());
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getAvgCode() {
		return avgCode;
	}

	public void setAvgCode(String avgCode) {
		this.avgCode = avgCode;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
	public int getBattery() {
		return battery;
	}

	public void setBattery(int battery) {
		this.battery = battery;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	
}
