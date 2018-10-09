
all:
#	@sh ./bin/kill.sh
	mvn clean package -Dhttps.protocols=TLSv1.2 -Dproject.build.os=.linux

clean:
	mvn clean -Dos=.linux
