 namespace java com.seveniu.thriftServer
 struct Result {
    1:required string url;
    2:required string storageName;
    3:required string orginName;
    4:required string extension;
 }
 service DownloaderThrift{
  map<string,Result> download(1:list<string> urls)
 }