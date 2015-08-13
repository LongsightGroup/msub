set JAVA_OPTS=-server



: -----------------------------------------------
: heap memory settings
: -----------------------------------------------
set JAVA_OPTS=%JAVA_OPTS% -Xmx1500m
: set JAVA_OPTS=%JAVA_OPTS% -XX:SurvivorRatio=6
: set JAVA_OPTS=%JAVA_OPTS% -XX:TargetSurvivorRatio=90


: -----------------------------------------------
: young generation memory settings
: -----------------------------------------------
: set JAVA_OPTS=%JAVA_OPTS% -XX:NewSize=256m
: set JAVA_OPTS=%JAVA_OPTS% -XX:MaxTenuringThreshold=0


: -----------------------------------------------
: perm size memory settings
: -----------------------------------------------
set JAVA_OPTS=%JAVA_OPTS% -XX:PermSize=128m
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxPermSize=500m


: -----------------------------------------------
: garbage collection settings
: -----------------------------------------------
: set JAVA_OPTS=%JAVA_OPTS% -verbose:gc
: set JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGCDetails
: set JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGCTimeStamps
: set JAVA_OPTS=%JAVA_OPTS% -XX:+PrintTenuringDistribution
: set JAVA_OPTS=%JAVA_OPTS% -XX:+UseCMSCompactAtFullCollection


: -----------------------------------------------
: debugging settings
: -----------------------------------------------
: set JAVA_OPTS=%JAVA_OPTS% -Xdebug
: set JAVA_OPTS=%JAVA_OPTS% -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005


: -----------------------------------------------
: locale settings
: -----------------------------------------------
: set JAVA_OPTS=%JAVA_OPTS% -Duser.language=zh -Duser.region=CN


: -----------------------------------------------
: time zone settings
: -----------------------------------------------
: set JAVA_OPTS=%JAVA_OPTS% -Duser.timezone=MST


: -----------------------------------------------
: encoding settings
: -----------------------------------------------
: set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8


: -----------------------------------------------
: class loading tracking
: -----------------------------------------------
: set JAVA_OPTS=%JAVA_OPTS% -XX:+TraceClassloading
: set JAVA_OPTS=%JAVA_OPTS% -XX:+TraceClassUnloading


: -----------------------------------------------
: thread tracking
: -----------------------------------------------
: set JAVA_OPTS=%JAVA_OPTS% -XX:+PrintTLAB
: set JAVA_OPTS=%JAVA_OPTS% -XX:+ResizeTLAB

: -----------------------------------------------
: whether to populate tables with extra demo data
: -----------------------------------------------
: set JAVA_OPTS=%JAVA_OPTS% -Dsakai.demo=true

set JAVA_OPTS=%JAVA_OPTS% -Dhttp.agent=Sakai-News-Tool -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false -Dsun.lang.ClassLoader.allowArraySyntax=true
