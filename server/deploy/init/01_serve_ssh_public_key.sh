#! /bin/bash

set -e

echo "Copying SSH host public key to public directory of webapp"
ln -f -s /etc/ssh/ssh_host_rsa_key.pub public