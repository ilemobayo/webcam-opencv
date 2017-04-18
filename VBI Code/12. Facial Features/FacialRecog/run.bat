@echo off
echo Executing %* with FaceSDK, JavaCV, and OpenCV...

java -cp "C:\Program Files\Luxand\FaceSDK\include\Java\FaceSDK.jar;C:\Program Files\Luxand\FaceSDK\include\Java\jna.jar;d:\javacv-bin\javacv.jar;d:\javacv-bin\javacpp.jar;d:\javacv-bin\javacv-windows-x86.jar;." -Djava.library.path="C:\opencv\build\x86\mingw\bin;C:\Program Files\Luxand\FaceSDK\bin\win32;." %*

rem C:\Program Files\Luxand\FaceSDK\bin\win32
echo Finished.


