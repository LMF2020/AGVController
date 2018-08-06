package studio.jedjiang.bean;

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
	private String cmd;
	private String taskName;
	private String battery;
	private boolean isFinished;
	private String error;

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
			String[] pair = item.split("=");
			String pair_k = pair[0].trim();
			String pair_v = pair[1].trim();

			if (pair_k.equals("cmd")) {
				me.setCmd(pair_v);
			} else if (pair_k.equals("task")) {
				me.setTaskName(pair_v);
			} else if (pair_k.equals("battery")) {
				me.setBattery(pair_v);
			} else if (pair_k.equals("task_isfinished")) {
				me.setFinished(Boolean.parseBoolean(pair_v));
			} else if (pair_k.equals("task_error")) {
				me.setError(pair_v);
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
		this.setCmd(me.getCmd());
		this.setError(me.getError());
		this.setFinished(me.isFinished());
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getBattery() {
		return battery;
	}

	public void setBattery(String battery) {
		this.battery = battery;
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

}
