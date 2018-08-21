layui.config({
	base: '/rs/js/'
}).extend({
	// 加载自定义模块
	tasklist: 'tasklist'
})

layui.use(['layer', 'tasklist'], function() {
	var layer = layui.layer;
	var tasklist = layui.tasklist;
	
	// 创建任务
	var CREATE_TASK = 0;
	// 结束任务
	var END_TASK = 1;
	// 恢复任务
	var RECOVER_TASK = 2;
	// 重置任务
	var CLEAR_TASK = 3;
	
	// 任务列表
	try{
		tasklist.init();
	}catch(e){
		console.error('task list init error.')
	}
	
	var myChart = null;
	
	initPage()

		// 添加任务
		$("#add_task").click(function() {
			COMJS.confirm('您确认要添加任务吗?', function() {
				var site = $('#position').val()
				var catalog = $('#catalog').val()
				var params = site + "/" + catalog;
				sendCommand(CREATE_TASK, params)
			})
		})
		
		// 清理任务
		$("#clear_task").click(function() {
			COMJS.confirm('清理任务？清理后请确保车子在待命区才能继续添加任务', function() {
				sendCommand(CLEAR_TASK)
			})
		})

//		// 结束任务
//		$("#end_task").click(function() {
//			COMJS.confirm('您确认要结束任务吗? 结束前请确保有任务在执行', function() {
//				sendCommand(END_TASK)
//			})
//		})
//		// 恢复任务
//		$("#rec_task").click(function() {
//			COMJS.confirm('您确认要恢复任务吗?', function() {
//				sendCommand(RECOVER_TASK)
//			})
//		})

	var sendCommand = function(flag, params) {
		var cmd = '';
		if(flag === CREATE_TASK) {
			cmd = "/task/add/" + params
		} else if(flag === CLEAR_TASK) {
			cmd = "/task/clear"
		} else if(flag === END_TASK) {
			cmd = "/cmd/task/end"
		} else if(flag === RECOVER_TASK){
			cmd = "/cmd/task/recover"
		}
		
		if(!cmd){
			COMJS.error("没有执行任务");
			return;
		}
		$.ajax({
				method: "GET",
				url: COMJS.CTX_PATH + cmd
			})
			.done(function(resp) {
				// 处理授权到期
				if(resp.code == 1002){
					console.log("授权到期");
					location.replace(location.protocol + "/rs/expired.html");
				}
				if(resp.code == 1) {
					COMJS.error(resp.msg);
				} else {
					COMJS.success(resp.msg);
				}
			});
	}

	/**
	 * 实时数据流
	 */
	function createWebsocketUrl() {
		var loc = window.location;
		var uri = "ws://" + loc.host;
		return uri.slice(0, uri.lastIndexOf(":")) + ":9321"
	}

	function connect() {
		var ws_uri = createWebsocketUrl()
		var ws = new WebSocket(ws_uri);
		ws.onopen = function() {
			console.log('Socket is connected')
		};
		
		// 实时更新车子的任务状态
		ws.onmessage = function(e) {
			// console.log('Message:', e.data);
			//
			var agvStatus = JSON.parse(e.data)
			// 当前正在执行任务的名称
			// var taskName = agvStatus['taskName']
			// 任务是否结束
			// var isFinished = agvStatus['isFinished']
			// 车子剩余电量
			// var battery = agvStatus['battery']
			// x 坐标
			var x = agvStatus['x']
			// y 坐标
			var y = agvStatus['y']
			// 如果有错误, 车子的报错日志
			// var error = agvStatus['error']
			console.log("x="+x + ",y=" + y)
			myChart.setOption(chartOptions(x, y, 0));
		};
		ws.onclose = function(e) {
			console.log('Socket is closed. Reconnect will be attempted in 1 second.', e.reason);
			setTimeout(function() {
				connect();
			}, 30000);
		};
		ws.onerror = function(err) {
			console.error('Socket encountered error: ', err.message, 'Closing socket');
			ws.close();
		};
	}
	
	// 初始化 WebSocket 连接
	connect()

	// 页面初始化
	function initPage() {

		// 初始化echarts实例
		myChart = echarts.init(document.getElementById('mainChart'));
		// 使用刚指定的配置项和数据显示图表。
		myChart.setOption(chartOptions(-40100, -7330, 0));

		function getAngle(x1, y1, x2, y2) {
			var x = Math.abs(x1 - x2);
			var y = Math.abs(y1 - y2);
			var z = Math.sqrt(x * x + y * y);
			return Math.round((Math.asin(y / z) / Math.PI * 180));
		}

		var target_start = [20, 40]
		var target_end = [10, 40]

//		function mockAnimate() {
//			var currentLocation = mockAnimate.location || [0, 0]
//			var x = currentLocation[0] + 1
//			var y = currentLocation[1] + 1
//			if(x > target_start[0]) {
//				x = target_start[0]
//			}
//			if(y > target_start[1]) {
//				y = target_start[1]
//			}
//
//			if(x < target_start[0] || y < target_start[1]) {
//				myChart.setOption(chartOptions(x, y, 0));
//				mockAnimate.location = [x, y]
//				setTimeout(mockAnimate, 800)
//			}
//			myChart.setOption(chartOptions(x, y, 0));
//
//		}
//		mockAnimate()
	}



});
