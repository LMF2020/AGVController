var COMJS = (function() {

	var _confirm = function(msg, myfunc) {
		msg = '<h5>' + msg + '</h5>'
		layer.confirm(msg, {
			title: '提示',
			btn: ['确定', '取消'],
			area: ['500px', '200px'],
			skin: 'demo-class',
			icon: 3,
			anim: 0
		}, function(index) {
			//$(".layui-layer-btn0").hide();
			//$(".layui-layer-btn1").hide();
			
			if(myfunc && $.isFunction(myfunc)) {
				myfunc(index)
			}
		}, function() {
			layer.close()
		})
	}

	var _alert = function(msg) {
		msg = '<h5>' + msg + '</h5>'
		layer.alert(msg, {
			skin: 'demo-class',
			area: ['500px', '200px'],
			icon: 6
		})
	}

	var _error = function(msg) {
		msg = '<h5>' + msg + '</h5>'
		layer.alert(msg, {
			icon: 2,
			area: ['500px', '250px'],
		})
	}

	var _success = function(msg) {
		msg = msg || '命令执行成功'
		var msg = '<h5>' + msg + '</h5>'
		//		layer.confirm(msg, {
		//			time: 20000, //20s后自动关闭
		//			btn: ['关闭', '继续'] //按钮
		//		}, function(index) {
		//			layer.closeAll()
		//		}, function() {
		//
		//		})
		layer.alert(msg, {
			icon: 1
			//area: ['500px', '250px'],
		})
	}

	// 读取表单模板
	var _readform = function(conf) {
		if(!conf || !conf.htmUrl) {
			layer.alert('配置为空或缺少模板路径', {
				icon: 2
			});
			return
		}
		$.get(conf.htmUrl, function(tplHtm) {
			var newConf = $.extend(true, {
				type: 1,
				area: ['550px', '400px'], // 默认窗口尺寸
				content: tplHtm //注意，如果str是object，那么需要字符拼接。
			}, conf);
			layer.open(newConf);
		});
	}

	return {
		CTX_PATH: '',
		confirm: _confirm,
		alert: _alert,
		success: _success,
		error: _error,
		readform: _readform
	}
})()