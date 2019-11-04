REM 打包脚本
REM 2019-11-4


REM 不加 call 会让 mvn 后面的语句无法执行
call mvn clean package "-DskipTests"
move target\mailtophone-0.0.1-SNAPSHOT.jar  mailtophone.jar
pause