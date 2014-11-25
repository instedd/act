#! /bin/bash

set -e

echo "Setting up SSH daemon"

# Use authorized_keys file in our persistent volume
touch /etc/ssh/authorized_keys
chmod 600 /etc/ssh/authorized_keys
ln -f -s /etc/ssh/authorized_keys /act/authorized_keys
sed -E -i 's/^#?AuthorizedKeysFile.*$/AuthorizedKeysFile \/etc\/ssh\/authorized_keys/' /etc/ssh/sshd_config

# Disable password authentication
sed -E -i 's/^#?PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config

# Fix fox ssh login
sed -i 's/session\s*required\s*pam_loginuid.so/session optional pam_loginuid.so/g' /etc/pam.d/sshd
