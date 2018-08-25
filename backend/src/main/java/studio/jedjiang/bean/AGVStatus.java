package studio.jedjiang.bean;

import org.nutz.lang.Strings;

import studio.jedjiang.client.AGVClient;

/**
 * agv任务状态
 * 
 * @author Jed
 *
 */
public class AGVStatus {
	
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
	public static AGVStatus ofme(String respCode) {

		AGVStatus me = new AGVStatus();
		String[] kv = respCode.trim().split(";");

		for (String item : kv) {
			String[] pair = item.split("=", 2);
			String pair_k = pair[0].trim();

			if (pair_k.equals("task")) {
				String pair_v = null;
				try {
					pair_v = pair[1].trim();
				} catch (Exception e) {
					pair_v = null;
				}
				if(Strings.isBlank(pair_v)){
					me.setTaskName(AGVClient.NO_TASK);
				}else {
					me.setTaskName(pair_v);
				}
			} else if (pair_k.equals("battery")) { // 电量
				String pair_v = pair[1].trim();
				int powerleft = 60;
				if(Strings.isNotBlank(pair_v)) {
					powerleft = Integer.parseInt(pair_v);
				}
				me.setBattery(powerleft);
			} else if (pair_k.equals("task_isfinished")) {
				me.setFinished("1".equals(pair[1].trim()));
			} else if (pair_k.equals("task_error")) {
				me.setError(pair[1].trim());
			} else if(pair_k.equals("x")) {
				me.setX(pair[1].trim());
			} else if(pair_k.equals("y")) {
				me.setY(pair[1].trim());
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
