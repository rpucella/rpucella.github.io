
#!/bin/bash

# uses https://www.docverter.com
# https://github.com/docverter/docverter

if [ "$#" -lt 1 ]
then
    echo 'Usage: md-to-html <file1> ...'
    exit -1
fi

acc=""
for var in "$@"
do
    acc="$acc -F input_files[]=@$var"
done

##echo http://c.docverter.com/convert -F from=markdown -F to=pdf $acc
curl http://c.docverter.com/convert -F from=markdown -F to=html -F template=template.html -F other_files[]=@template.html $acc
