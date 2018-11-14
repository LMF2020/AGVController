package studio.jedjiang.service;

import java.util.List;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import com.google.common.collect.Lists;

import studio.jedjiang.bean.Task;


@IocBean
public class TaskServiceImpl implements TaskService {

	private static final Log LG = Logs.get();

	@Inject
	protected Dao dao;

	@Override
	public void update(Task task) throws Exception {
		if (Strings.isBlank(task.getId())) {
			throw new Exception("更新任务Id不存在");
		}
		dao.updateIgnoreNull(task);
		LG.info("任务:(" + task.getName() + ")更新成功, 状态为:" + task.getStatus());
	}

	@Override
	public void delete(List<String> taskIds) throws Exception {
		for (String id : taskIds) {
			dao.delete(Task.class, id);
		}
		LG.info("任务:(" + String.join(",", taskIds) + ")已被删除.");
	}

	@Override
	public void deleteList(List<Task> tasks) throws Exception {
		for (Task task : tasks) {
			dao.delete(task);
		}
	}

	/**
	 * 查询任务列表, 排序：已完成任务 > 进行中任务 > 待办任务
	 */
	@Override
	public List<Task> listAll() throws Exception {
		List<Task> taskList = dao.query(Task.class, Cnd.orderBy().asc("ordering"));
		if (taskList.isEmpty()) {
			return Lists.newArrayList();
		}
		List<Task> todoTaskList = Lists.newArrayList();
		List<Task> finishedTaskList = Lists.newArrayList();
		List<Task> inProgressTaskList = Lists.newArrayList();
		// 把任务分类
		for (Task task : taskList) {
			switch (task.getStatus()) {
			case Task.TASK_FINISHED:
				finishedTaskList.add(task);
				break;
			case Task.TASK_IN_PROCESS:
				inProgressTaskList.add(task);
				break;
			case Task.TASK_TODO:
				todoTaskList.add(task);
				break;
			default:
				break;
			}
		}

		inProgressTaskList.addAll(todoTaskList);
		finishedTaskList.addAll(inProgressTaskList);
		return finishedTaskList;
	}

	@Override
	public Task get(String id) throws Exception {
		return dao.fetch(Task.class, id);
	}

	@Override
	public void clearAll() throws Exception {
		dao.clear(Task.class);
	}

	@Override
	public void clearTodo() throws Exception {
		dao.clear(Task.class, Cnd.where("status", "=", Task.TASK_TODO));
	}
	
	/**
	 * 清空已完成的任务
	 */
	@Override
	public void clearFinished() throws Exception {
		dao.clear(Task.class, Cnd.where("status", "=", Task.TASK_FINISHED));
	}

	/**
	 * 新增任务
	 * 
	 */
	@Override
	public void add(String taskName) throws Exception {
		List<Task> taskList = dao.query(Task.class, Cnd.orderBy().desc("ordering"));
		Task task = new Task();
		task.setStatus(Task.TASK_TODO);
		task.setName(taskName);
		if (taskList.size() != 0) {
			Task lastTask = taskList.get(0);
			// 如果任务列表有任务, 新增的任务序号要加一
			task.setOrdering(lastTask.getOrdering() + 1);
		}
		// 如果任务列表没有任务, 新增的任务序号为默认值200000
		dao.insert(task);
		LG.infof("新增任务:(%s).", taskName);
	}

	
	@Override
	public Task findLastTodo() throws Exception {
		List<Task> todoTaskList = dao.query(Task.class, Cnd.where("status", "=", Task.TASK_TODO).orderBy("ordering", "desc"));
		if (todoTaskList.size() != 0) {
			return todoTaskList.get(0);
		}
		return null;
	}
	
	/**
	 * 选择任务
	 * 
	 */
	@Override
	public Task findNext() throws Exception {
		List<Task> todoTaskList = dao.query(Task.class, Cnd.where("status", "=", Task.TASK_TODO).orderBy("ordering", "asc"));
		if (todoTaskList.size() != 0) {
			return todoTaskList.get(0);
		}
		return null;
	}

	/**
	 * 任务置顶
	 * 
	 */
	@Override
	public void top(String id) throws Exception {
		List<Task> todoTaskList = dao.query(Task.class, Cnd.where("status", "=", Task.TASK_TODO).orderBy("ordering", "asc"));
		if (todoTaskList.size() != 0) {
			// 拿出当前顺序最靠前的任务
			Task topTask = todoTaskList.get(0);
			Task curTask = dao.fetch(Task.class, id);
			// 如果当前顺序最靠前的任务比我选择的任务靠前才会把我选择的任务置顶
			if (topTask.getOrdering() < curTask.getOrdering()) {
				// 置顶的方案就是把最靠前的顺序减一赋值给我选择的任务
				curTask.setOrdering(topTask.getOrdering() - 1);
				dao.update(curTask);
			}
		}
	}

	/**
	 * 获取进行中的任务
	 */
	@Override
	public Task getOngoingTask() throws Exception {
		return dao.fetch(Task.class, Cnd.where("status", "=", Task.TASK_IN_PROCESS));
	}

	/**
	 * 更新进行中的任务为完成
	 */
	@Override
	public void updateOngoingAsFinished() throws Exception{
		dao.update(Task.class, Chain.make("status",Task.TASK_FINISHED), Cnd.where("status", "=", Task.TASK_IN_PROCESS));
	}

	/**
	 * 获取最新完成的任务
	 */
	@Override
	public Task getLatestFinished() throws Exception {
		List<Task> doneTaskList = dao.query(Task.class, Cnd.where("status", "=", Task.TASK_FINISHED).orderBy("ordering", "asc"));
		if (doneTaskList.size() != 0) {
			return doneTaskList.get(0);
		}
		return null;
	}

	/**
	 * 根据指定状态，添加任务
	 */
	@Override
	public void addByStatus(String taskName, int status) throws Exception {
		List<Task> taskList = dao.query(Task.class, Cnd.orderBy().desc("ordering"));
		Task task = new Task();
		task.setStatus(status);
		task.setName(taskName);
		if (taskList.size() != 0) {
			Task lastTask = taskList.get(0);
			// 如果任务列表有任务, 新增的任务序号要加一
			task.setOrdering(lastTask.getOrdering() + 1);
		}
		// 如果任务列表没有任务, 新增的任务序号为默认值200000
		dao.insert(task);
		LG.infof("新增任务:(%s),状态:(%d)", taskName, status);
		
	}

	/**
	 * 删除指定的待办任务，同时会级联删除后续关联的待办任务
	 */
	@Override
	public void deleteTodo(String id) throws Exception {
		Task task = dao.fetch(Task.class, id);
		if(task == null) {
			throw new Exception("任务不存在，无法删除");
		}
		Trans.exec(new Atom(){
		    public void run() {
				dao.clear(Task.class,Cnd.where("ordering", ">", task.getOrdering()).and("status", "=", Task.TASK_TODO));
				dao.delete(Task.class, id);
		    }
		});
	}

}
