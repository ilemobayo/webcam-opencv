@echo off
echo Compiling %* with TableLayout, JavaCV, and OpenCV...

javac -cp "d:\javacv-bin\javacv.jar;d:\javacv-bin\javacpp.jar;d:\javacv-bin\javacv-windows-x86.jar;TableLayout-bin-jdk1.5-2009-08-26.jar;." %*

echo Finished.
