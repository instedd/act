FROM phusion/passenger-ruby21:0.9.12
MAINTAINER Juan Edi <jedi@manas.com.ar>

# Set correct environment variables.
ENV HOME /root
ENV RAILS_ENV production

CMD "/act/init"

RUN rm -f /etc/service/nginx/down
RUN rm /etc/nginx/sites-enabled/default

# Install rsync
RUN apt-get update && apt-get install -y rsync

# Prepare folders
RUN mkdir /home/app/webapp
WORKDIR /home/app/webapp

# Install bundle and copy app files
ADD Gemfile /home/app/webapp/
ADD Gemfile.lock /home/app/webapp/
RUN bundle install --without development test
ADD . /home/app/webapp

# Copy configuration and init files
ADD deploy/nginx-site.conf /etc/nginx/sites-enabled/webapp.conf
ADD deploy/nginx-env.conf  /etc/nginx/main.d/nginx-env.conf
ADD deploy/init /act/init

# Add file monitor daemon
ADD deploy/act-sync.pub /root/.ssh/authorized_keys
RUN mkdir /etc/service/act-filemonitor
ADD deploy/act-filemonitor.sh /etc/service/act-filemonitor/run

# Clean up APT when done.
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

EXPOSE 22
EXPOSE 80