#! /bin/bash

set -e

if [ -n "$ACT_SUPPORT_CONTACT" ] && [ -n "$ACT_SMTP_SERVER" ]; then

CELLCOM_TASK_LOG="$ACT_APP_DIR/log/cellcom.log"

sed -E -i "s/^#?SENDMAILTO.*$/SENDMAILTO=\"$ACT_SUPPORT_CONTACT\"/" /etc/logcheck/logcheck.conf
sed -E -i "s/^#?INTRO.*$/INTRO=0/"                                  /etc/logcheck/logcheck.conf
sed -E -i "s/^#?EVENTSSUBJECT.*$/EVENTSSUBJECT=/"                   /etc/logcheck/logcheck.conf
sed -E -i "s/^#?MAILASATTACH.*$/MAILASATTACH=1/"                    /etc/logcheck/logcheck.conf

touch $CELLCOM_TASK_LOG
echo $CELLCOM_TASK_LOG > /etc/logcheck/logcheck.logfiles

echo \
"root=postmaster
mailhub=$ACT_SMTP_SERVER
#rewriteDomain=
hostname=act
AuthUser=$ACT_SMTP_USER
AuthPass=$ACT_SMTP_PASS
useSTARTTLS=YES
" > /etc/ssmtp/ssmtp.conf

fi;
