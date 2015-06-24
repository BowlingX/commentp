#!/usr/bin/expect -f
 
#Check to ensure one argument is passed:
if {[llength $argv] != 1} {
# Useage statement 
puts "usage: ssh_add_key.exp ssh_keyname" 
#We exit with return code = 1
exit 1
}
# Set the first argument
set ssh_keyname [lrange $argv 0 0]
# Set Timeout
set timeout 15
# Spawn ssh-add utility to add ssh private keys to the ssh_agent 
spawn ssh-add $ssh_keyname
# We expect to prompted for ssh_key pass phrase
expect "Enter passphrase for*"
# Send the ssh_key pass phrase and return 
send ""
# End of File 
expect eof