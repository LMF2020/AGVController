package studio.jedjiang.bean;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Comment;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;

import lombok.Data;

/**
 * 站点
 * 
 * @author Jed
 *
 */
@Data
@Table("task_site")
public class TaskSite extends BaseModel {

	private static final long serialVersionUID = 1L;

	public TaskSite(String name, Integer status) {
		this.name = name;
		this.status = status;
	}

	public TaskSite() {}

	@Name
	@Comment("站点")
	@Column("name")
	@ColDefine(type = ColType.VARCHAR)
	private String name;

	@Column
	@Comment("仓位状态：满仓or空仓")
	@ColDefine(type = ColType.INT)
	private Integer status = 0; // 默认空仓

}
