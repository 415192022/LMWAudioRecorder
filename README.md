
### 引入依赖

```
dependencies {
    implementation 'com.github.415192022:LMWAudioRecorder:0.0.1'
}
```

首先在application中进行初始化操作
```
AudioRecorder.init(context)
```
使用前，先通过构造出IRecorder
```
val recorder: IRecorder =
            MediaRecorder
                .Builder()
                .setFileDir(Environment.getExternalStorageDirectory().absolutePath + File.separator + "lmw" + File.separator)
                .setRecordStatusListener(object : IRecordStatusListener {
                    override fun recording() {
                        //正在录制回调
                    }

                    override fun stop(filePath: String?) {
                        //停止回调
                    }

                    override fun pause(filePath: String?) {
                        //暂停回调
                    }

                    override fun resume() {
                        //暂停后继续录制回调
                    }

                    override fun error(e: Exception?) {
                        //正在录制回调
                    }

                })
                .build()
```

然后通过start和stop方法，就可以开始录制和停止录制了

```
recorder.start()

recorder.stop()
```

假如需要自定义一个类似微信录音的功能，我们可以通过继承RecorderButton来实现
RecorderButton支持的属性如下：

| 属性         | 说明                 |
|:------------ |:-------------------- |
| max_time     | 最大录制时长         |
| min_time     | 最小录制时长         |
| max_distance | 滑动取消录制最大距离 |


RecorderButton在Xml文件中使用如下：
```
<com.lmw.audiorecorder.widget.RecordButton
            android:id="@+id/recordButton"
            app:max_time="50"
            app:min_time="3"
            android:layout_marginLeft="35dp"
            android:layout_marginRight="35dp"
            app:max_distance="30"
            android:layout_width="match_parent"
            android:background="@color/colorPrimary"
            android:layout_height="40dp"/>
```


