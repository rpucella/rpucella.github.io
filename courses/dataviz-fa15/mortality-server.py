# A simple server for behavioral data

from bottle import get, post, run, request, static_file, redirect
# from bottle import PasteServer
import os
import sys
#import psycopg2
import sqlite3

import traceback

from bottle import response
from json import dumps


MORTALITYDB = "mortality.db"


def group_by (rows,field):
    values = set([r[field] for r in rows])
    grouped_rows = {}
    for (value,rows) in [(value,[r for r in rows if r[field]==value]) for value in values]:
        grouped_rows[value] = rows
    return grouped_rows


def pullData (gender):
    # ignore age & gender for now
    conn = sqlite3.connect(MORTALITYDB)
    cur = conn.cursor()

    try: 
        if gender:
            cur.execute("""SELECT year, Cause_Recode_39, SUM(1) as total 
                           FROM mortality
                           WHERE sex = '%s'
                           GROUP BY year, Cause_Recode_39""" % (gender))
        else:
            cur.execute("""SELECT year, Cause_Recode_39, SUM(1) as total 
                           FROM mortality
                           GROUP BY year, Cause_Recode_39""")
        data = [{"year":int(year),
                 "cause":int(cause),
                 "total":total} for (year, cause, total,) in  cur.fetchall()]
#        causes = set([d["cause"] for d in data])
#        grouped_data = {}
#        for  (cause,rows) in [(cause,[d for d in data if d["cause"]==cause]) for  cause in causes]:
#            grouped_data[cause] = rows
        grouped_data = group_by(data,"cause")
        for cause in grouped_data:
            grouped_data[cause]= group_by(grouped_data[cause],"year")
        conn.close()
        return grouped_data

    except: 
        print "ERROR!!!"
        conn.close()
        raise
        
    
@get("/data")
def data ():
    print list(request.query)
    gender = request.query.gender

    print "gender =", gender
    return pullData(gender)

    
@get('/<name>')
def static (name="index.html"):
    return static_file(name, root='.')  # os.getcwd())

def main (p):
    run(host='0.0.0.0', port=p)

if __name__ == "__main__":
    if len(sys.argv) > 1:
        main(int(sys.argv[1]))
    else:
        print "Usage: server <port>"
