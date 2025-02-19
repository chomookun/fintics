# FINTICS (Financial System Trading Application)

Don't do it. If you mess up, you'll be in big trouble.

![](docs/assets/image/gambling-raccon.gif)

![](docs/assets/image/gambling-dog.gif)

---

## Redis installation
```shell
# install
sudo apt install redis

# status
sudo systemctl status redis

# check status
nc -zv 127.0.0.1 6379

# config (optional)
sudo vim /etc/redis/redis.conf
...
bind 0.0.0.0
protected-mode no
...
```


