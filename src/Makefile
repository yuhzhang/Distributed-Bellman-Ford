JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
 
  
CLASSES = \
	Client.java \
	SendThread.java \
	ListenThread.java \
	TimerThread.java

default: classes
   
classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
