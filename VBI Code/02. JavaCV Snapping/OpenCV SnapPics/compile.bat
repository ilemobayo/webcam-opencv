@echo off
echo Compiling %* with Java binding of OpenCV...

javac -cp "C:\opencv\build\java\opencv-245.jar;." %*

echo Finished.
