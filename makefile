JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
		ServerHandler.java \
		Client.java \
		Server.java \
		ClientHandler.java \
		BridgeServer.java
		

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
