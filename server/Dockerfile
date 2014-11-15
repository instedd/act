FROM phusion/passenger-ruby21:0.9.12
MAINTAINER Juan Edi <jedi@manas.com.ar>

# Set correct environment variables.
ENV HOME /root
ENV RAILS_ENV production

CMD "/act/init"

RUN rm -f /etc/service/nginx/down
RUN rm /etc/nginx/sites-enabled/default

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

# Clean up APT when done.
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

EXPOSE 80