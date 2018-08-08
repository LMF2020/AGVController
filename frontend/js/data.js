
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
// 号位
var hwStyle={
  symbol:'rect',
  symbolSize:[20,60],
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
    offset:[10,16]
  }
}

var chartData = [{
  name: '充电桩',
  value: [59000, -6350],
  symbol:'rect',
  symbolSize:[20,40],
  itemStyle:{
    color:'rgba(0,0,0,0)',
    borderColor:'#000',
    borderWidth:3
  },
  label:{
    show:true,
    color:'#000',
    position:'right',
  }
},
{
  name: 'node',
  value: [59000, -180],
  ...nodeStyle
},
{
  name: 'node',
  value: [54290, -180],
  ...nodeStyle
},
{
  name: 'node',
  value: [49150, -180],
  ...nodeStyle
},
{
  name: 'node',
  value: [45540, -180],
  ...nodeStyle
},
{
  name: '仓库',
  value: [49150, -4820],
  symbol:'rect',
  symbolSize:[160,40],
  itemStyle:{
    color:'rgba(0,0,0,0)',
    borderColor:'#000',
    borderWidth:3
  },
  label:{
    show:true,
    color:'#000',
    position:'bottom',
  }
}, 
{
  name: '定子',
  value: [45540, -4820],
  ...dzStyle
},
{
  name: '转子',
  value: [49150, -4820],
  ...zzStyle
},
{
  name: '退仓',
  value: [54290, -4820],
  ...tcStyle
  
},

{
  name: 'node',
  value: [-33625, 380],
  ...nodeStyle
},
{
  name: 'node',
  value: [-40100, 380],
  ...nodeStyle
},
{
  name: '待命区',
  value: [-40100, -7330],
  symbol:'rect',
  symbolSize:[20,40],
  itemStyle:{
    color:'rgba(0,0,0,0)',
    borderColor:'#000',
    borderWidth:3
  },
  label:{
    show:true,
    color:'#000',
    position:'left',
  }
},

{
  name: 'node',
  value: [-33625, -920],
  ...nodeStyle
},
{
  name: 'node',
  value: [-33625, -2420],
  ...nodeStyle
},
{
  name: 'node',
  value: [-33625, -3920],
  ...nodeStyle
},
{
  name: '1号位',
  value: [-37900, -4290],
  ...hwStyle
}, 
{
  name: '定子',
  value: [-37900, -4290],
  ...dzStyle
},
{
  name: '转子',
  value: [-37900, -5790],
  ...zzStyle
},
{
  name: '退仓',
  value: [-37900, -2790],
  ...tcStyle
},




{
  name: 'node',
  value: [-33625, -8920],
  ...nodeStyle
},
{
  name: 'node',
  value: [-33625, -10420],
  ...nodeStyle
},
{
  name: 'node',
  value: [-33625, -11920],
  ...nodeStyle
},
{
  name: '2号位',
  value: [-37900, -12290],
  ...hwStyle
}, 
{
  name: '定子',
  value: [-37900, -12290],
  ...dzStyle
},
{
  name: '转子',
  value: [-37900, -13790],
  ...zzStyle
},
{
  name: '退仓',
  value: [-37900, -10790],
  ...tcStyle
},


{
  name: 'node',
  value: [-33625, -16920],
  ...nodeStyle
},
{
  name: 'node',
  value: [-33625, -18420],
  ...nodeStyle
},
{
  name: 'node',
  value: [-33625, -19920],
  ...nodeStyle
},
{
  name: '3号位',
  value: [-37900, -20290],
  ...hwStyle
}, 
{
  name: '定子',
  value: [-37900, -20290],
  ...dzStyle
},
{
  name: '转子',
  value: [-37900, -21790],
  ...zzStyle
},
{
  name: '退仓',
  value: [-37900, -18790],
  ...tcStyle
},


{
  name: 'node',
  value: [-33625, -24920],
  ...nodeStyle
},
{
  name: 'node',
  value: [-33625, -26420],
  ...nodeStyle
},
{
  name: 'node',
  value: [-33625, -27920],
  ...nodeStyle
},
{
  name: '4号位',
  value: [-37900, -28290],
  ...hwStyle
}, 
{
  name: '定子',
  value: [-37900, -28290],
  ...dzStyle
},
{
  name: '转子',
  value: [-37900, -29790],
  ...zzStyle
},
{
  name: '退仓',
  value: [-37900, -26790],
  ...tcStyle
},


{
  name: 'node',
  value: [-33625, -32920],
  ...nodeStyle
},
{
  name: 'node',
  value: [-33625, -34420],
  ...nodeStyle
},
{
  name: 'node',
  value: [-33625, -35920],
  ...nodeStyle
},
{
  name: '5号位',
  value: [-37900, -36290],
  ...hwStyle
}, 
{
  name: '定子',
  value: [-37900, -36290],
  ...dzStyle
},
{
  name: '转子',
  value: [-37900, -37790],
  ...zzStyle
},
{
  name: '退仓',
  value: [-37900, -34790],
  ...tcStyle
},
]