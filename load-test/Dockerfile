FROM blazemeter/taurus
RUN apt-get -y install xmlstarlet bc

# Taurus working directory  = /bzt-configs
ADD ./load-test.sh /bzt-configs/
RUN chmod 755 /bzt-configs/load-test.sh
COPY scripts/ /bzt-configs/scripts/
ENTRYPOINT ["sh", "-c","./load-test.sh", "1", "scenario1"]