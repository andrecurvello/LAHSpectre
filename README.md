Spectre
=======

A collection of useful and convenient Java utilities

Aim
---

The aim of this project is to provide a collection of convenient Java utilities including

*  File downloading (SpectreGet): a reincarnation of wget and jDownloader
   -   static downloading method: blocking until completion, with progress notification
       and callback on completion/error.
   -   asynchronous downloading method: run in background thread, with progress notification
       and callback on completion/error.
       
*  Archive file extraction (Digger):
   -   wrapper methods to extract a file archive or compressed archive
   
*  Execution of process with time out constraint (TimedProcess):
   -   Add time out to Runtime#exec(...) to make sure that the process terminates in finite time.
   -   Simply use static methods like create() and execute()
   
*  And more to come.

Building and inporting to your Java project
-------------------------------------------

Clone this project from github.

1.	Import the project to Eclipse.

2.	Build the project.

3.	Export to jar.
	Note: For Android, currently need to make sure that we compile with JRE 1.6 compatibility.
	
4.	Copy the exported jar to your project's location and reference it.
  
TODO
----

* Incorporate or modify jDownloader's source

* Think of a more reasonable design to address security issue