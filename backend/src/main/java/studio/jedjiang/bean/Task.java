package studio.jedjiang.bean;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Comment;
import org.nutz.dao.entity.annotation.EL;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Prev;
import org.nutz.dao.entity.annotation.Table;

import lombok.Data;

/**
 * 待办任务列表
 * 
 * @author Jed
 *
 */
@Data
@Table("task")
public class Task extends BaseModel {
	
	private static final long serialVersionUID = 1L;
	
	public static final int TASK_TODO = 0;
	public static final int TASK_IN_PROCESS = 1;
	public static final int TASK_FINISHED = 2;
	
	@Name
	@Comment("任务ID")
	@ColDefine(type = ColType.VARCHAR, width = 32)
	@Prev(els = { @EL("uuid()") })
	private String id;
	
	@Comment("任务名称")
	@Column("name")
	@ColDefine(type = ColType.VARCHAR)
	private String name;
	
	@Column
	@Comment("任务状态：待办(0)or进行中(1)or已完成(2)")
	@ColDefine(type = ColType.INT)
	private Integer status = TASK_TODO;
	
	@Column
	@Comment("任务顺序")
	@ColDefine(type = ColType.INT)
	private Integer ordering = 200000;
}
