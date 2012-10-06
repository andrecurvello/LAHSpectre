SpectreGet
==========

A reincarnation of wget and jDownloader

Aim
---

The aim of this project is to provide a file downloading library. SpectreGet allows
Java applications to send file download requests and get notified on events such as
* the size of the file requested is known
* the number of byte received that we already received (periodically updated after 
  each KB is fetched, granularity will/can be added as a feature)
* the download result is ready to process
* there is exception in handling the request such as the file already exists on the
  disk, the file cannot be written (permission error), the path is actually a directory,
  the URL is malformed, etc.
  
  
Usage
-----

A file download requesting object should get an instance of SpectreGet using the
static method getInstance. Then use method requestResource to submit a request.

TODO
----

* incorporate or modify jDownloader's source
* think of a more reasonable design to address security issue