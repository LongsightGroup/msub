set PATH="C:\Program Files\Java\jdk1.5.0_06\bin";%PATH%
@echo off
for /F "usebackq delims==" %%i IN (`dir /B target\customizer-task-*-app.jar`) DO @java -jar -Xmx512m -Xms512m -XX:PermSize=128m -XX:MaxPermSize=128m target\%%i docs\cle-configuration-template.xml ..\..\ docs\ docs\toolIcons