FROM blazemeter/taurus
RUN apt-get -y install xmlstarlet bc && \
  apt-get -y clean && \
  rm -rf /var/lib/apt/lists/*

# Taurus working directory  = /bzt-configs
ADD ./load-test.sh /bzt-configs/
RUN chmod 755 /bzt-configs/load-test.sh
COPY scripts/ /bzt-configs/scripts/
ENTRYPOINT ["bash","./load-test.sh"]
CMD ["1", "scenario1"]