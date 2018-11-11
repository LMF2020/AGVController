var nodeStyle = {
  symbolSize:5,
  itemStyle:{
    color:'#000'
  }
}
// 定子
var dzStyle = {
  symbol:'rect',
  symbolSize:[8,8],
  itemStyle:{
    color:'#f00'
  }
}
// 转子
var zzStyle = {
  symbol:'rect',
  symbolSize:[8,8],
  itemStyle:{
    color:'blue'
  }
}
// 退仓
var tcStyle = {
  symbol:'rect',
  symbolSize:[8,8],
  itemStyle:{
    color:'gray'
  }
}
// 号仓
var hwStyle={
  symbol:'rect',
  symbolSize:[20,90],
  itemStyle:{
    color:'rgba(0,0,0,0)',
    borderColor:'#000',
    borderWidth:1
  },
  label:{
    show:true,
    color:'#000',
    position:'right',
    rotate:90,
    offset:[60,10]
  }
}

/**
 * Chart 绘制
 */
var chartOptions = function(x, y, bt, deg){
  return {
    backgroundColor: '#fff',
    toolTip:{
      show:true
    },
    xAxis: {
      show: false
    },
    yAxis: {
      show: false
    },
    legend:{},
    series: {
      label: {
        show: false,
        formatter: '{b}'
      },
      // itemStyle: {
      //   borderWidth: 10
      // },
      // symbol: 'rect',
      // symbolSize: [80, 100],
      data: [
				{
				  name: '上部(left)',
				  value: [-40500,2400],
				  ...nodeStyle
				},
				{
				  name: '上部(right)',
				  value: [-34000,2400],
				  ...nodeStyle
				},
				{
				  name: '东(1)',
				  value: [18150,2400],
				  ...nodeStyle
				},
				{
				  name: '东(2)',
				  value: [18150,43545],
				  ...nodeStyle
				},
				{
				  name: '东(3)',
				  value: [20300,47545],
				  ...nodeStyle
				},
				{
				  name: '东(4)',
				  value: [20300,72500],
				  ...nodeStyle
				},
				{
				  name: '待命区',
				  value: [-40500,-7285],
				  ...tcStyle,
//				  symbol:'rect',
//				  symbolSize:[20,40],
//				  itemStyle:{
//				    color:'rgba(0,0,0,0)',
//				    borderColor:'#000',
//				    borderWidth:3
//				  },
				  label:{
				    show:true,
				    color:'#000',
				    position:'left',
				    offset:[-20,10]
				  }
				},
				{
				  name: 'node',
				  value: [-40500,-7285],
				  ...nodeStyle
				},
				{
				  name: '1号仓(上)',
				  value: [-34100,-7285],
				  ...dzStyle
				},
				{
				  name: 'node',
				  value: [-40500,-8745],
				  ...nodeStyle
				},
				{
				  name: '1号仓',
				  value: [-34100,-8745],
				  ...dzStyle,
				  label:{
					    show:true,
					    color:'#000',
					    position:'right',
					    offset:[20,-5]
					  }
				},
				{
				  name: 'node',
				  value: [-40500,-15135],
				  ...nodeStyle
				},
				{
				  name: '2号仓(上)',
				  value: [-34100,-15135],
				  ...dzStyle
				},
				{
				  name: 'node',
				  value: [-40500,-16535],
				  ...nodeStyle
				},
				{
				  name: '2号仓',
				  value: [-34100,-16535],
				  ...dzStyle,
				  label:{
					    show:true,
					    color:'#000',
					    position:'right',
					    offset:[20,-5]
					  }
				},
				{
				  name: 'node',
				  value: [-40500,-23390],
				  ...nodeStyle
				},
				{
				  name: '3号仓(上)',
				  value: [-34100,-23390],
				  ...dzStyle
				},
				{
				  name: 'node',
				  value: [-40500,-24790],
				  ...nodeStyle
				},
				{
				  name: '3号仓',
				  value: [-34100,-24790],
				  ...dzStyle,
				  label:{
					    show:true,
					    color:'#000',
					    position:'right',
					    offset:[20,-5]
					  }
				},
				{
				  name: 'node',
				  value: [-40500,-31475],
				  ...nodeStyle
				},
				{
				  name: '4号仓(上)',
				  value: [-34100,-31475],
				  ...dzStyle
				},
				{
				  name: 'node',
				  value: [-40500,-32875],
				  ...nodeStyle
				},
				{
				  name: '4号仓',
				  value: [-34100,-32875],
				  ...dzStyle,
				  label:{
					    show:true,
					    color:'#000',
					    position:'right',
					    offset:[20,-5]
					  }
				},
				{
				  name: 'node',
				  value: [-40500,-39190],
				  ...nodeStyle
				},
				{
				  name: '5号仓(上)',
				  value: [-34100,-39190],
				  ...dzStyle
				},
				{
				  name: 'node',
				  value: [-40500,-40590],
				  ...nodeStyle
				},
				{
				  name: '5号仓',
				  value: [-34100,-40590],
				  ...dzStyle,
				  label:{
					    show:true,
					    color:'#000',
					    position:'right',
					    offset:[20,-5]
					  }
				},
				{
				  name: 'node',
				  value: [-40500,-35945],
				  ...nodeStyle
				},
				{
				  name: '6号仓(上)',
				  value: [-46495,-35945],
				  ...zzStyle
				},
				{
				  name: 'node',
				  value: [-40500,-34545],
				  ...nodeStyle
				},
				{
				  name: '6号仓',
				  value: [-46495,-34545],
				  ...zzStyle,
				  label:{
					    show:true,
					    color:'#000',
					    position:'right',
					    offset:[-25,40]
					  }
				},
				{
				  name: '底部',
				  value: [-40500,-42000],
				  ...nodeStyle
				},
				{
				  name: 'node',
				  value: [20300,71305],
				  ...nodeStyle
				},
				{
				  name: '7号仓(1)',
				  value: [16900,71305],
				  ...zzStyle
				},
				{
				  name: 'node',
				  value: [20300,70020],
				  ...nodeStyle
				},
				{
				  name: '7号仓(2)',
				  value: [16900,70020],
				  ...dzStyle
				},
				{
				  name: 'node',
				  value: [20300,68610],
				  ...nodeStyle
				},
				{
				  name: '7号仓',
				  value: [16900,68610],
				  ...dzStyle,
				  label:{
					    show:true,
					    color:'#000',
					    position:'left',
					    offset:[-20,-5]
					  }
				},
				{
				  name: 'node',
				  value: [20300,66560],
				  ...nodeStyle
				},
				{
				  name: '充电桩',
				  value: [16900,66560],
				  ...tcStyle,
				  label:{
				    show:true,
				    color:'#000',
				    position:'right',
				    offset:[-60,10]
				  }
				},
				// 小车
				{
		          name: '电量：' + bt + '%',
		          value: [x, y],
		          label: {
		          show: true,
		            position:'left',
		            offset:[-10,-15]
		          },
		          symbolSize: [40, 40],
		          symbolRotate: deg,
		          symbol: "image://data:image/jpeg;base64,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"
		        }],
      
        // 连线
        markLine: {
        	data: [
        		// 底部连线(left)
        		[{
        			coord: [-40500,2400],
        			symbol: 'none'
        		}, {
        			coord: [-40500,-42000],
        			symbol: 'none'
        		}],
        		// 1号仓(up)
        		[{
        			coord: [-34100,-7285],
        			symbol: 'none'
        		}, {
        			coord: [-40500,-7285],
        			symbol: 'none'
        		}],
        		// 1号仓(down)
        		[{
        			coord: [-34100,-8745],
        			symbol: 'none'
        		}, {
        			coord: [-40500,-8745],
        			symbol: 'none'
        		}],
        		// 2号仓(up)
        		[{
        			coord: [-34100,-15135],
        			symbol: 'none'
        		}, {
        			coord: [-40500,-15135],
        			symbol: 'none'
        		}],
        		// 2号仓(down)
        		[{
        			coord: [-34100,-16535],
        			symbol: 'none'
        		}, {
        			coord: [-40500,-16535],
        			symbol: 'none'
        		}],
        		// 3号仓(up)
        		[{
        			coord: [-34100,-23390],
        			symbol: 'none'
        		}, {
        			coord: [-40500,-23390],
        			symbol: 'none'
        		}],
        		// 3号仓(down)
        		[{
        			coord: [-34100,-24790],
        			symbol: 'none'
        		}, {
        			coord: [-40500,-24790],
        			symbol: 'none'
        		}],
        		// 4号仓(up)
        		[{
        			coord: [-34100,-31475],
        			symbol: 'none'
        		}, {
        			coord: [-40500,-31475],
        			symbol: 'none'
        		}],
        		// 4号仓(down)
        		[{
        			coord: [-34100,-32875],
        			symbol: 'none'
        		}, {
        			coord: [-40500,-32875],
        			symbol: 'none'
        		}],
        		// 5号仓(up)
        		[{
        			coord: [-34100,-39190],
        			symbol: 'none'
        		}, {
        			coord: [-40500,-39190],
        			symbol: 'none'
        		}],
        		// 5号仓(down)
        		[{
        			coord: [-34100,-40590],
        			symbol: 'none'
        		}, {
        			coord: [-40500,-40590],
        			symbol: 'none'
        		}],
        		// 6号仓(up)
        		[{
        			coord: [-46495,-35945],
        			symbol: 'none'
        		}, {
        			coord: [-40500,-35945],
        			symbol: 'none'
        		}],
        		// 6号仓(down)
        		[{
        			coord: [-40500,-34545],
        			symbol: 'none'
        		}, {
        			coord: [-46495,-34545],
        			symbol: 'none'
        		}],
        		// 7号仓(1)
        		[{
        			coord: [16900,71305],
        			symbol: 'none'
        		}, {
        			coord: [20300,71305],
        			symbol: 'none'
        		}],
           		// 7号仓(2)
        		[{
        			coord: [16900,70020],
        			symbol: 'none'
        		}, {
        			coord: [20300,70020],
        			symbol: 'none'
        		}],
           		// 7号仓(3)
        		[{
        			coord: [16900,68610],
        			symbol: 'none'
        		}, {
        			coord: [20300,68610],
        			symbol: 'none'
        		}],
           		// 7号仓(4)
        		[{
        			coord: [16900,66560],
        			symbol: 'none'
        		}, {
        			coord: [20300,66560],
        			symbol: 'none'
        		}],
        		// 底部连线(right)
        		[{
        			coord: [-34000,2400],
        			symbol: 'none'
        		}, {
        			coord: [-34000,-42000],
        			symbol: 'none'
        		}],
        		// 上部连线
        		[{
        			coord: [-40500,2400],
        			symbol: 'none'
        		}, {
        			coord: [18150,2400],
        			symbol: 'none'
        		}],
        		// 东(1-2)
        		[{
        			coord: [18150,43545],
        			symbol: 'none'
        		}, {
        			coord: [18150,2400],
        			symbol: 'none'
        		}],
        		// 东(2-3)
        		[{
        			coord: [20300,47545],
        			symbol: 'none'
        		}, {
        			coord: [18150,43545],
        			symbol: 'none'
        		}],
           		// 东(3-4)
        		[{
        			coord: [20300,72500],
        			symbol: 'none'
        		}, {
        			coord: [20300,47545],
        			symbol: 'none'
        		}]
        	]
        },
      type: 'scatter'
    }
  }
}
