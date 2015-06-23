#!/usr/bin/expect -f
spawn ssh-add ~/.ssh/id_46.101.159.127.pub
expect "Enter passphrase for /home/ubuntu/.ssh/id_46.101.159.127.pub:"
send "";
interact