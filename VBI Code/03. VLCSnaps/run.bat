@echo off
echo Executing %1 with vlcj...

java -cp "d:\vlcj-2.3.1\*;." -Djna.library.path="C:\Program Files\VideoLAN\VLC" %*
rem                           ^^^^ the above is jna not java
echo Finished.