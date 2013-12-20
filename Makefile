all:
	javac -cp lib/pircbotx-1.9.jar:lib/twitter4j-core-3.0.5.jar:lib/twitter4j-async-3.0.5.jar:lib/twitter4j-stream-3.0.5.jar:. *.java

clean:
	rm -rf *.class
