@echo off
echo Executing %* with TopCodes, JavaCV, and OpenCV...

java -cp "d:\topcodes\lib\topcodes.jar;d:\javacv-bin\javacv.jar;d:\javacv-bin\javacpp.jar;d:\javacv-bin\javacv-windows-x86.jar;." -Djava.library.path="d:\topcodes\lib;C:\opencv\build\x86\mingw\bin;." %*

echo Finished.
