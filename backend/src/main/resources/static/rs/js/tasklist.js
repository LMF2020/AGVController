layui.define([ 'jquery', 'table' ], function(exports) {

	var $ = layui.$;
	var layer = layui.layer;
	var table = layui.table;

	var me = {
		init : function() {
			table.render({
				elem : '#tasklist',
				url : COMJS.CTX_PATH + "/task/list",
				cols : [ [{
					field : 'uname',
					title : '任务'
				}, {
					field : 'status',
					width : 100,
					title : '状态',
					templet : function(d) {
						var text = '待执行';
						if (d.status == 1) {
							text = '进行中';
						} else if (d.status == 2) {
							text = '<span style="color: #c00;">已完成</span>';
						}
						return text;
					}
				}, {
					title : '操作',
					width : 65,
					align : 'center',
					toolbar : '#mybar'
				}, {
					field : 'name',
					width : 100,
					title : '代码'
				} ] ]
			});

			// 监听工具条
			table.on('tool(test)', function(obj) {
				var data = obj.data; // 获得当前行数据
				var layEvent = obj.event; // 获得 lay-event 对应的值（也可以是表头的 event
				// 参数对应的值）

				if (layEvent === 'del') { // 删除任务
					layer.confirm('确定删除任务(' + data.name + ')？后面关联的待办任务将会被级联删除',
							function(index) {
								layer.close(index);
								me.sendTaskAction("delete", data.id);
							});
				}
			});

			// 8s 刷新一次任务列表
			setInterval(function() {
				try {
					table.reload('tasklist')
				} catch (e) {
					console.error('refresh failed.');
				}
			}, 8000);

		},

		sendTaskAction : function(action, taskId) {
			$.ajax({
				method : "GET",
				url : COMJS.CTX_PATH + "/task/" + action + "/" + taskId
			}).done(function(resp) {
				if (resp.code == 1) {
					console.error(resp.msg);
				} else {
					table.reload('tasklist');
				}
			});
		}

	}

	exports('tasklist', me)

})