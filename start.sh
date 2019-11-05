#!/bin/bash

#用户配置区开始#
#
#增加可执行权限
#chmod +x ./test.sh
#执行脚本 参数端口号 文件名
#./start.sh  mail 8081  
#
#用户配置区结束#
name=$1
port=$2
file="$name.jar"
#根据端口号查询对应的pid
pid=$(netstat -nlp | grep :$port | awk '{print $7}' | awk -F"/" '{ print $1 }');
echo $file;
#杀掉对应的进程，如果pid不存在，则不执行
if [  -n  "$pid"  ];  then
    echo "杀死进程成功";
    kill  -9  $pid;
fi
#判断文件是否存在
if [  -f "$file" ]; then
  #守护进程启动springboot
  nohup java -jar $file & 
  echo -e \\n;
fi
if [ -f "nohup.out" ]; then
  tail -f "nohup.out";
fi






