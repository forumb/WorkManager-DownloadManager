# WorkManager-DownloadManager

I have shown two ways to download any file using Android API:
1. WorkManager
2. DownloadManager

Sample http url is used to download a zip file.

Both workmanager and download manager will perform the downloading task in the background.
Workmanger has a advantage of scheduling task periodically. It will retry the work when the constratints are met or device is restarted.
Downloadmanger help to pause/resume the download activity and it is solely used for downloading big files through url.

DownloadManger will download the files on external storage only. 
