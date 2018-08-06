$(function() {

	layui.use(['layer', 'table'], function() {
		var layer = layui.layer;
		
		// 创建任务
		var CREATE_TASK = 0;
		// 结束任务
		var END_TASK = 1;
		// 恢复任务
		var RECOVER_TASK = 2;
		
		// 根据locationURL 初始化页面
		initPage()

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

		// flag : 0-create; 1-recover; 2-end;
		var sendCommand = function(flag, msg) {
			var cmd = '';
			if(flag === CREATE_TASK) {
				cmd = "/cmd/task/create/" + msg
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
				console.log('WebSocket is connected')
			};
			
			// 实时更新车子的任务状态
			ws.onmessage = function(e) {
				// console.log('Message:', e.data);
				//
				var agvStatus = JSON.parse(e.data)
				// 当前正在执行任务的名称
				var taskName = agvStatus['taskName']
				// 任务是否结束
				var isFinished = agvStatus['isFinished']
				// 车子剩余电量
				var battery = agvStatus['battery']
				// x 坐标
				var x = agvStatus['x']
				// y 坐标
				var y = agvStatus['y']
				// 如果有错误, 车子的报错日志
				var error = agvStatus['error']
				
				
//				$("#current_task").text(taskName)
//				$("#current_status").text(isFinished ? '任务已结束' : '任务进行中')
//				$("#current_battery").text(battery + '%')


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
		// connect()

		// 页面初始化
		function initPage() {

        // 初始化echarts实例
        var myChart = echarts.init(document.getElementById('mainChart'));

        // 指定图表的配置项和数据
        var option = {
            title: {
                text: 'ECharts test'
            },
            tooltip: {},
            legend: {
                data:['销量']
            },
            xAxis: {
                data: ["衬衫","羊毛衫","雪纺衫","裤子","高跟鞋","袜子"]
            },
            yAxis: {},
            series: [{
                name: '销量',
                type: 'bar',
                data: [5, 20, 36, 10, 10, 20]
            }]
        };

        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);

		}

	});

})