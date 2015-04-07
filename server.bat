setlocal
set java=C:\Program Files\Java\jre1.8.0_25\bin\java.exe

"%java%" -cp libs/stm-1.0.jar;libs/* net.darkslave.stm.test.TestServer server.cfg

pause