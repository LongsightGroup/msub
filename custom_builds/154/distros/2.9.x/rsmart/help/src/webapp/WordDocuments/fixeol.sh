GFILE=dos2unix.out

cat /dev/null $LOGFILE;

for foo in `find .`;
do

   if  [[ -n `file -b $foo | grep text` ]];  then
     tr '\r' '\n' $foo;
   fi

done

