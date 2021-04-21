#!/bin/bash

# uses https://www.docverter.com
# https://github.com/docverter/docverter

CURRENT='/Users/riccardo/git/rpucella.github.io/bin'

if [ "$#" -lt 1 ]
then
    echo 'Usage: convert <file1> ...'
    exit -1
fi

acc=""
for var in "$@"
do
    acc="$acc -F input_files[]=@$var"
done

##echo http://c.docverter.com/convert -F from=markdown -F to=pdf $acc
curl http://c.docverter.com/convert -F from=markdown -F to=html -F template=template.html -F other_files[]=@$CURRENT/template.html $acc
