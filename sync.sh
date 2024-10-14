#!/bin/bash
echo Syncing changes from AY2425S1-CS2113-W14-4/tp...

gh repo sync yijiano/tp -b master
git checkout master
git pull origin master

echo Sync done!
