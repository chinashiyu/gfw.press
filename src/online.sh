netstat -nt  | grep "`hostname -i`:[12][0-9]\{4\}"  | grep ESTABLISHED |awk '{print $4}' | awk -F ":" '{print $NF}' |sort -u |wc |awk '{print $1}'
