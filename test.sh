cd /root/react-terminal-style/
git pull
npm run build
scp -r build/* /var/www/html/
echo FINISHED