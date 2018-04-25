#
# parseforticket.sh
# Parse for JIRA tickets
# Match patterns are SW-, SP-, VEL-, sw-, sp-, vel-
#
#
#
INPUTFILE=${1}
echo -n "" >out
awk 'match($0, /[sS][wW]-[0-9]+/) { print substr( $0, RSTART, RLENGTH )}' ${INPUTFILE} >>out
awk 'match($0, /[sS][pP]-[0-9]+/) { print substr( $0, RSTART, RLENGTH )}' ${INPUTFILE} >>out
awk 'match($0, /[vV][eE][lL]-[0-9]+/) { print substr( $0, RSTART, RLENGTH )}' ${INPUTFILE} >>out
sort out | uniq >./out1
dos2unix out1 >/dev/null 2>&1
ALIST=""
while read ALINE
do
   ALIST="$ALIST $ALINE"
done <./out1
echo ${ALIST}
rm out out1
