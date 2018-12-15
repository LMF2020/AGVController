# AGV叉车自动化调度
## 简要概述：
客户端通过socket协议与车载进行通讯，发送和解析指令，实时上报车子状态，同步指令的发送。
## 代码核心：
* Websocket推送服务，
* Socket协议解析，
* GIS实时定位，
* 异步消息的同步控制，
* 复杂业务简单化设计（移步→[需求文档](https://github.com/LMF2020/AGVController/tree/master/doc)）
## 运行效果图：
![image 程序运行效果图](https://github.com/LMF2020/AGVController/raw/master/doc/img/agv.png)