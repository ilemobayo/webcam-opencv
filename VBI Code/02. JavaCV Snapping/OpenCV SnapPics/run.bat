@echo off
echo Executing %* with Java binding of OpenCV...

java -cp "C:\opencv\build\java\opencv-245.jar;." -Djava.library.path="C:\opencv\build\java\x86\;." %*

echo Finished.
