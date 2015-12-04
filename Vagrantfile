# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.

# https://github.com/Parallels/boot2docker-vagrant-box

Vagrant.configure(2) do |config|

  config.vm.network "private_network", ip: "192.168.33.20"

  config.vm.box = "parallels/boot2docker"

  config.vm.synced_folder "/Users", "/Users", type: "nfs", mount_options: ["nolock", "vers=3", "udp"], id: "nfs-sync"

end
