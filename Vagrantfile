# -*- mode: ruby -*-
# vi: set ft=ruby :

# Nice Resources: https://vagrantcloud.com/parallels/boxes/boot2docker
# https://vagrantcloud.com/parallels/boxes/boot2docker
Vagrant.configure(2) do |config|
  config.vm.box = "parallels/boot2docker"

  config.vm.network "private_network", ip: "192.168.33.11"

  # Make Volumes available
  config.vm.synced_folder "/Users", "/Users", type: "nfs", create: true

  # Fix busybox/udhcpc issue
  config.vm.provision :shell do |s|
    s.inline = <<-EOT
      if ! grep -qs ^nameserver /etc/resolv.conf; then
        sudo /sbin/udhcpc
      fi
      cat /etc/resolv.conf
    EOT
  end

  # Adjust datetime after suspend and resume
  config.vm.provision :shell do |s|
    s.inline = <<-EOT
      sudo /usr/local/bin/ntpclient -s -h pool.ntp.org
      date
    EOT
  end

end
