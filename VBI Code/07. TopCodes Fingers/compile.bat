@echo off
echo Compiling %* with TopCodes, JavaCV, and OpenCV...

javac -cp "d:\topcodes\lib\topcodes.jar;d:\javacv-bin\javacv.jar;d:\javacv-bin\javacpp.jar;d:\javacv-bin\javacv-windows-x86.jar;." %*

echo Finished.
