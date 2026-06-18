#!/bin/bash

#开始时间 时间戳
startTime=`date +'%Y-%m-%d %H:%M:%S'`

#接口项目站点路径（目录按照各自配置）
APP_PATH=/www/wwwroot/java.qianxutec.cn

#jar包文件名称
APP_NAME=$APP_PATH/Crmeb-admin.jar

#日志文件名称
LOG_FILE=$APP_PATH/crmeb_out.log

#启动环境   # 如果需要配置数据和redis，请在 application-prod.yml中修改, 用jar命令修改即可
APP_YML=--spring.profiles.active=prod


#数据库配置

rm -rf $LOG_FILE

echo "开始停止 Crmeb 项目进程"
#查询进程，并杀掉当前jar/java程序

pid=`ps -ef|grep $APP_NAME | grep -v grep | awk '{print $2}'`
if [ $pid ];then
  echo "pid: $pid"
  kill -9 $pid
  echo "Crmeb 项目进程进程终止成功"
fi

sleep 2

#判断jar包文件是否存在，如果存在启动jar包，并时时查看启动日志

if test -e $APP_NAME;then
  echo '文件存在,开始启动此程序...'

# 启动jar包，指向日志文件，2>&1 & 表示打开或指向同一个日志文件  --spring.profiles.active=prod 启动 prod环境
  cd "$APP_PATH" || exit 1
  nohup java -jar "$APP_NAME" $APP_YML > "$LOG_FILE" 2>&1 &
  echo "正在发布中，请稍后......"
  sleep 5

  # 通过检测日志判断是否启动成功（最多等待 120 秒）
  waitSeconds=0
  while [ $waitSeconds -lt 120 ]; do
      if [ -f "$LOG_FILE" ]; then
          success=`grep "Started CrmebAdminApplication in " "$LOG_FILE"`
          if [[ "$success" != "" ]]; then
              break
          fi
          fail=`grep -E "APPLICATION FAILED TO START|Fail" "$LOG_FILE"`
          if [[ "$fail" != "" ]]; then
              echo "项目启动失败"
              tail -n 100 "$LOG_FILE"
              exit 1
          fi
      fi
      sleep 1
      waitSeconds=$((waitSeconds + 1))
  done

  if [ $waitSeconds -ge 120 ]; then
      echo "启动超时，请查看日志: $LOG_FILE"
      tail -n 100 "$LOG_FILE"
      exit 1
  fi
  echo "Crmeb Started Success"

endTime=`date +'%Y-%m-%d %H:%M:%S'`
startSecond=$(date --date="$startTime" +%s);
endSecond=$(date --date="$endTime" +%s);

total=$((endSecond-startSecond))
# shellcheck disable=SC2027
echo "本次运行时间： "$total"s"
# shellcheck disable=SC2086
echo "当前时间："$endTime


 
else
  echo '$APP_NAME 文件不存在,请检查。'
fi

#在 $APP_NAME 同级目录下运行  ./startAdmin.sh 命令即可启动项目。
