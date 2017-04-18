@echo off
echo Compiling %* with ZXing, JavaCV, and OpenCV...

javac -cp "javase/javase.jar;core/core.jar;d:\javacv-bin\javacv.jar;d:\javacv-bin\javacpp.jar;d:\javacv-bin\javacv-windows-x86.jar;." %*

echo Finished.
