#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#define BASEDIR "/var/lib/jenkins/jobs"
char *paKeywords[] = { "Branch - ", "Commit - ", "Repository - ",
                       "Project - ", "Trigger - ",
                       "PR Destination - ", "PR Id - ",
                       "PR URL - ", "PR Author - ",
                       "PR Title - ", "PR Description - " };
void getValue( char *pKeyword, char *pJobname, char *pJobnumber)
{
   char s[8192];
   char *p;
   char *p1;
   FILE *fpin;
   char szFileName[2048];
 
   memset(szFileName,0,sizeof(szFileName));
   sprintf(szFileName, "%s/%s/builds/%s/log", BASEDIR, pJobname, pJobnumber);
   fpin = fopen(szFileName, "r");
 
   if ( fpin == ( FILE * ) NULL )
   {
      printf("Error %s\n", szFileName);
      exit(-1);
   }
  
   while ( fgets(s,sizeof(s),fpin) != ( char * ) NULL )
   {
      p1 = &s[strlen(s) - 1];
      if ( *p1 == ( char ) 0x0a )
      {
           *p1 = 0x00;
      }
      if ( ( p = strstr(s,pKeyword) ) != ( char * ) NULL )
      {
         
          p+= strlen(pKeyword);
          printf("%s", p);
          break;
      }
   }
   exit(0);
}
 
void Usage(char *progname)
{
   printf("%s -[bcrptdiualn] -z <jobname> -y <jobnumber>", progname);
   exit(-1);
}
void main(int argc, char *argv[])
{
 
int c = 0;
int index = 0;
char szJobName[256];
char szJobNumber[256];
 
while ((c = getopt (argc, argv, "bcrptdiualnz:y:")) != -1)
    switch (c)
      {
      case 'y':
        strcpy(szJobNumber,optarg);
        break;
      case 'z':
        strcpy(szJobName,optarg);
        break;
      case 'b':
        index = 0;
        break;
      case 'c':
        index = 1;
        break;
      case 'r':
        index = 2;
        break;
      case 'p':
        index = 3;
        break;
      case 't':
        index = 4;
        break;
      case 'd':
        index = 5;
        break;
      case 'i':
        index = 6;
        break;
      case 'u':
        index = 7;
        break;
      case 'a':
        index = 8;
        break;
      case 'l':
        index = 9;
        break;
      case 'n':
        index = 10;
        break;
      default:
        Usage(argv[0]);
      }
      getValue(paKeywords[index], szJobName, szJobNumber);
}