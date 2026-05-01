JFLAGS = -d out -cp "lib/*:src"
JC = javac
JVM = java
RUN_FLAGS = -cp "lib/*:out"

# Find all java files
SOURCES := $(shell find src -name "*.java")

all: compile

compile:
	@mkdir -p out
	$(JC) $(JFLAGS) $(SOURCES)

run: compile
	$(JVM) $(RUN_FLAGS) Main

clean:
	rm -rf out

# To run without make:
# Windows:
# javac -d out -cp "lib/*;src" src/**/*.java src/*.java
# java -cp "lib/*;out" Main
