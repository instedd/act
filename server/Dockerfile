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
RUN bundle exec rake assets:precompile

# Copy configuration and init files
ADD deploy/nginx-site.conf /etc/nginx/sites-enabled/webapp.conf
ADD deploy/nginx-env.conf  /etc/nginx/main.d/nginx-env.conf
ADD deploy/init /act/init
ADD deploy/rrsync /act/rrsync

# Add background daemons
RUN mkdir /etc/service/act-file-monitor /etc/service/act-keys-import
ADD deploy/act-file-monitor.sh /etc/service/act-file-monitor/run
ADD deploy/act-keys-import.sh  /etc/service/act-keys-import/run

# Clean up APT when done.
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

VOLUME /var/lib/postgresql/data
VOLUME /etc/ssh
VOLUME /act/sync

EXPOSE 22
EXPOSE 80