FROM blazemeter/taurus
RUN apt-get -y install xmlstarlet bc && \
  apt-get -y clean && \
  rm -rf /var/lib/apt/lists/*
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
  unzip awscliv2.zip && \
  ./aws/install

# Taurus working directory  = /bzt-configs
ADD ./load-test.sh /bzt-configs/
RUN chmod 755 /bzt-configs/load-test.sh
COPY scripts/ /bzt-configs/scripts/
ENTRYPOINT ["bash","./load-test.sh"]
CMD ["1", "scenario1"]