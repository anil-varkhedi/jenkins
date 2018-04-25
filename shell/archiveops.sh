#
#
#
# archiveops : Create Archive and/or extract archive
# Needs commit
#       branch
# Files created in /z/V3DJenkinsRelease/[master|release|branch]
#
#
#
BASEPATH=/z/V3DJenkinsRelease
export PATH="c:\Program Files\git\usr\bin":$PATH
usage()
{
    echo "${0} -c COMMMIT -b BRANCH -d DESTINATIONDIR -x -o"
    exit -1
}
#
# create a file list
#
#
create_archive()
{
  #Get list of all the files in the directory
  OUTPUTFILENAME=${1}
  rm -f out out1
  echo "Making a file list"
  find . -type f -print >out
  #Excluding files we do not need
  echo "Excluding Files"
  grep -v "\.cpp$" out | grep -v "*[!d][!a][!t][!a]\.obj" | grep -v "\.pch$" | grep -v "\.pdb$" | grep -v "\.\/ThirdParty" | grep -v "\/\.git" | grep -v "filters$" | grep -v '\.tlog' | grep -v '\.vcxproj' | grep -v '\.lib' | grep -v '\.obj' | grep -v "\.h$" >out1
  grep '\.obj' out >out2
  grep 'data' out2 >out3
  cat out3 >>out1
  DNAME=`dirname ${OUTPUTFILENAME}`
  echo "Creating output directory ${DNAME}"
  mkdir -p ${DNAME}
  tar cvzf  ${OUTPUTFILENAME} -T ./out1
  echo "Created ${OUTPUTFILENAME}"
  DNAME=`dirname ${DNAME}`
  echo "Creating link latest"
  echo -n "${OUTPUTFILENAME}"  >${DNAME}/latest
}
XTRACTFLAG=0
OUTPUTFLAG=0
while getopts ":hb:d:c:n:xo" opt; do
  case ${opt} in
    h ) usage
      ;;
    c ) COMMIT=${OPTARG};;
    n ) NUMBER=${OPTARG};;
    b ) BRANCH=${OPTARG};;
    d ) DESTINATION=${OPTARG};;
    x ) XTRACTFLAG=1;;
    o ) OUTPUTFLAG=1;;
    \? ) usage
      ;;
    : ) usage
      ;;
  esac
done
               
if [ -z ${COMMIT} ]
then
                usage
fi
if [ -z ${BRANCH} ]
then
                usage
fi
if [ -z ${DESTINATION} ]
then
                usage
fi
if [ ${BRANCH} = "master" ]
then
                    ZIPPATH=${BASEPATH}/master/${DESTINATION}/${NUMBER}/master.tar.gz   
else
   echo ${BRANCH} | grep 'release' >/dev/null 2>&1
   if [ "${?}" = "0" ]
   then
                BRANCH=`echo $BRANCH | sed 's/\//_/g'`
        ZIPPATH=${BASEPATH}/release/${DESTINATION}/${NUMBER}/${BRANCH}.tar.gz      
   else
                ZIPPATH=${BASEPATH}/branch/${DESTINATION}/${BRANCH}/${NUMBER}/${BRANCH}.tar.gz       
   fi
fi
echo "Running Operations on ${ZIPPATH}"
if [ "${XTRACTFLAG}" = "1" -a "${OUTPUTFLAG}" = "1" ]
then
       echo "Cannot use for both output and extract "
       exit 1
fi
if [ "${XTRACTFLAG}" = "1" ]
then
   echo "Extracting the GZipped tar acrhive ${ZIPPATH}"
   if [ ! -f ${ZIPPATH} ]
   then
      DNAME=`dirname ${ZIPPATH}`
      BNAME=`basename ${ZIPPATH}`
      DNAME=`dirname ${DNAME}`
      FNAME="${DNAME}/latest"
      ZIPPATH=`cat ${FNAME}`
   fi
   tar xvzf ${ZIPPATH}
fi
if [ "${OUTPUTFLAG}" = "1" ]
then
   echo "Creating tar archive ${ZIPPATH}"
   create_archive ${ZIPPATH}
fi