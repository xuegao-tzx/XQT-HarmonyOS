# 校企通鸿蒙版

**这是校企通鸿蒙版快捷打卡魔改版，仅仅用于学习技术交流，请勿用于商业目的以及日常打卡，日常打卡还请下载校企通原版进行登录后如实填写自己的信息来打卡。**

**本项目仅用于个人知识积累供大家学习交流分享，请勿将成果用于非法用途，建议您设计测试完成后进行正规的打卡操作。因使用本技术研究带来的一切后果，由使用者自行负责。出现任何问题均与作者无关。**


实现原理:

1. 首先我们通过HMS服务调用系统定位模块来获取当前设备的详细经纬度
2. 我们使用从[百度地图官网](https://lbsyun.baidu.com/index.php?title=webapi/guide/webservice-geocoding-abroad)申请的AK密钥，来使用全球逆地理编码服务
3. 通过分析校企通抓包信息，[参考这篇文章](https://blog.csdn.net/weixin_43416532/article/details/113866480)可知原版获取的地址为[省市区街道]的格式
4. 这里我们使用JianJia网络框架，可以轻松向校企通后端发送POST表单请求
5. 最后根据返回值即可得到打卡成功的返回值


使用方法:

1. 下载本项目到本地
2. 首先你要到华为开发者平台申请密钥，以便于真机调试，[参考官方方法](https://developer.harmonyos.com/cn/docs/documentation/doc-guides/ide_debug_device-0000001053822404)，并且在应用API处打开地理位置的开关，并且按照官网指引下载agconnect-services.json文件，并参考[这个](https://developer.huawei.com/consumer/cn/doc/development/HMSCore-Guides/harmonyos-java-config-app-signing-0000001199536987),并在:
`json
"client":{
		"cp_id":"xxxxxx",
		"product_id":"xxxxxxxxxxxxxxxxxx",
		"client_id":"xxxxxxxxxxxxxxxxxx",
		"client_secret":"[!XXXXXXXXXXXXXXXXXXXXXX]",
		"project_id":"xxxxxxxxxxxxxxxxxx",
		"app_id":"xxxxxx",
		"api_key":"[!XXXXXXXXXXXXXXXXXXXXXX]",
		"package_name":"你的打包名",
		"cert_fingerprint": "这里加一行填写你的证书指纹"
	},
`,完成这些之后记得把文件复制到entry目录下和entry/src/main/resources/rawfile/下，然后就可以进行下一步了
3. 编译前记得更改entry/src/main/config.json文件中的bundleName和jianjia/src/main/config.json文件中的bundleName为你自己申请的应用打包名，还有entry/src/main/config.json中的权限处:
`json
{
        "permissions": [
          "com.huawei.agconnect.core.DataAbilityShellProvider.PROVIDER"
        ],
        "name": "com.huawei.agconnect.core.provider.AGConnectInitializeAbility",
        "type": "data",
        "uri": "dataability://你的打包名.location.AGConnectInitializeAbility"
}
`，还有记得在MainAbilitySlice.java中的第184行左右TODO左侧处填写你自己在百度地图控制台申请的AK
4. 最后你就可以在自己的真机上运行了


免责声明

**本项目所有信息都仅用于个人知识积累供大家学习交流分享，请勿将成果用于非法用途，建议您设计测试完成后进行正规的打卡操作。因使用本技术研究带来的一切后果，由使用者自行负责。出现任何问题均与作者无关。**
