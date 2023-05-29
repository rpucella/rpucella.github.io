#! /bin/bash

# untested, so please copy before using

rm -rf .git
git init -b master
git config --add user.name 'Riccardo Pucella'
git config --add user.email 'riccardo.pucella@gmail.com'
git add .
git commit -m 'Reset repo'
git remote add origin git@github.com:rpucella/rpucella.github.io.git
git push --set-upstream origin master --force
