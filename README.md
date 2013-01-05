Spectre
=======

A collection of useful and convenient Java utilities and interfaces

Aim
---

The aim of this project is to provide a collection of convenient Java utilities to support other projects (namely, a spectre) including

*  TimedShell: invoke external programs with time-out

*  File downloading (SpectreGet): a reincarnation of wget and jDownloader
   -   static downloading method: blocking until completion, with progress notification
       and callback on completion/error.
   -   asynchronous downloading method: run in background thread, with progress notification
       and callback on completion/error.
       
*  Archive file extraction (Digger):
   -   wrapper methods to extract a file archive or compressed archive