all:
	javac -cp lib/pircbotx-1.9.jar:. *.java

clean:
	rm -rf *.class
