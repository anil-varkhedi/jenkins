#
# Get the server connection
#
def get_server_instance():
                jenkins_url = 'http://jenkins.velo3d.com'
                server = Jenkins(jenkins_url, username='jenkins',
                        password='JN88@campbell')
                logging.info('Getting Server Instance')
                return server
"""Get job details of each job that is running on the Jenkins instance"""
#
# Usage
#
def usage(*args):
    for arg in args:
        progname = arg
    print "python " + progname + " -b <branchname> -j <jobname> -n <buildnumber>"
 
def stopbuild(build, killtime, triggername):
    #
    #
    #
    strdateformat = "%Y-%m-%d %H:%M:%S"
    jbuildtime = build.get_timestamp()
    print jbuildtime
    jbuildtime_s = str(jbuildtime)
    formatted_jbuildtime = jbuildtime_s.split('+')
    builddt = datetime.datetime.strptime(formatted_jbuildtime[0],
                    strdateformat)
    currenttime = datetime.datetime.now()
    currenttime_s = str(currenttime)
    formatted_currenttime = currenttime_s.split('.')
    currentdt = datetime.datetime.strptime(formatted_currenttime[0],
                  strdateformat)
    dif = currentdt - builddt
    print dif.total_seconds()
    if ( dif.total_seconds() < 1800 ):
       print "Stopping " + str(build.buildno)
       logging.info( "Stopping the build "  + str(build.buildno) )
       build.stop()
#
# Find the jobs that are running before this one and
#
#def find_jobs_for_branch(bname, jname, currentbuild, killtime, triggername):
def find_jobs_for_branch(**kwargs):
#
# Refer Example #1 for definition of function 'get_server_instance'
#
    bname = kwargs['branch']
    jname = kwargs['jobname']
    currentbuild = kwargs['currentbuild']
    killtime = kwargs['killtime']
    triggername = kwargs['triggername']
    if _debug == 1:
       if triggername == "MANUAL":
          triggername = "PR_OPENED"
    if triggername != "PR_OPENED":
       print "Skipping test since the trigger is not a PR_OPENED"
       print "triggername " + str(triggername)
       return
    try:
                server = get_server_instance()
        job = server.get_job(jname)
        build = job.get_last_build()
                print "Build Number is " + str(build.buildno)
        for i in range(int(currentbuild)-1, int(currentbuild) - 50, -1):
          if i > 0:
           build = job.get_build(i)
           if _debug == 1:
              print ("Status is ", build.get_status())
           status = build.get_status()
           if status not in allowed_status:
              if build.is_running():
                print "Build is Running"
              else:
                print "Build is not running"
              parms = build.get_actions()['parameters']
              for p in parms:
                 parmname = p['name']
                 if parmname == 'branch':
                    branchname = p['value']
                    print branchname
                    if branchname == bname:
                     #
                     # if current time is within the expected time
                     #
                       if _debug == 1:
                          print "Found BUILD "  + str(build.buildno)
                          print build.get_timestamp()
                       print "Stopping " + str(build.buildno)
                       logging.info( "Calling Stopbuild " + str(build.buildno) )
                       stopbuild(build, killtime, triggername)
                    
    except Exception as e:
                print "Exception "  + str(e)
#
#get job details
#
def get_job_details():
# Refer Example #1 for definition of function 'get_server_instance'
    try:
                server = get_server_instance()
                for job_name, job_instance in server.get_jobs():
                                print 'Job Name:%s' % (job_instance.name)
                                print 'Job Description:%s' % (job_instance.get_description())
                                print 'Get Params :%s' % (job_instance.get_params())
                        bld = job_instance.get_last_build()
                                logging.info(bld.buildno)
    except Exception as e:
                print "Exception" + str(e)
if __name__ == '__main__':
         try:                               
            opts, args = getopt.getopt(sys.argv[1:], "hdb:j:n:t:r:",
                        ["help", "branch=", "job=", "jobnumber="])
            logging.basicConfig(filename='cancelprevjobs.log',
                                level=logging.INFO)
         except getopt.GetoptError:         
            usage(sys.argv[0])                        
            sys.exit(2)                    
         for opt, arg in opts:               
           if opt in ("-h", "--help"):     
            usage(sys.argv[0])                        
            sys.exit(-1)                  
           elif opt == '-d':               
            _debug = 1                 
           elif opt in ("-b", "--branch"):
            branch = arg              
           elif opt in ("-j", "--job"):
            jobname = arg               
           elif opt in ("-n", "--jobnumber"):
            jobnumber = arg              
           elif opt in ("-t", "--timespent"):
            killtime = arg              
           elif opt in ("-r", "--trigger"):
            triggername = arg              
                 find_jobs_for_branch(branch=branch,jobname=jobname,
                   currentbuild=jobnumber,killtime=killtime,
                                     triggername=triggername)
