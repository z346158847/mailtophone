REM ����ű�
REM 2019-11-4


REM ���� call ���� mvn ���������޷�ִ��
call mvn clean package "-DskipTests"
move target\mailtophone-0.0.1-SNAPSHOT.jar  mailtophone.jar
pause