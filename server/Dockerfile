FROM phusion/passenger-ruby21:0.9.12
MAINTAINER Juan Edi <jedi@manas.com.ar>

# Set correct environment variables.
ENV HOME /root
ENV RAILS_ENV production

CMD ["sh","/act/init"]

RUN rm -f /etc/service/nginx/down
RUN rm /etc/nginx/sites-enabled/default


# Prepare folders
RUN mkdir /home/app/webapp

# Run Bundle in a cache efficient way
WORKDIR /tmp
ADD Gemfile /tmp/
ADD Gemfile.lock /tmp/
RUN bundle install --without development test

# Add the rails app
ADD . /home/app/webapp
WORKDIR /home/app/webapp
RUN bundle install --without development test

# Clean up APT when done.
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ADD deploy/nginx.conf /etc/nginx/sites-enabled/webapp.conf
ADD deploy/init /act/init
ADD deploy/act-env.conf /etc/nginx/main.d/act-env.conf
ADD deploy/secrets.yml /home/app/webapp/config/secrets.yml