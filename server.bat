@ECHO OFF

CD /D %~dp0

setLocal EnableDelayedExpansion

for /R .\lib %%A in (*.jar) do (
   set JARPATH=%%A;!JARPATH!
)

%JAVA_HOME%\bin\java -Dfile.encoding=utf-8 -Dsun.jnu.encoding=utf-8 -Duser.timezone=Asia/Shanghai -Xms512M -Xmx512M  -classpath !JARPATH!.\bin\ press.gfw.Server
