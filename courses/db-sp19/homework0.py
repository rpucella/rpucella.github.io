######################################################################
#
# HOMEWORK 0
#
# Due: Sun 2/3/19 23h59.
# 
# Name: 
# 
# Email:
# 
# Remarks, if any:
#
#
######################################################################


######################################################################
#
# Python 3 code
#
# Please fill in this file with your solutions and submit it
#
# The functions below are stubs that you should replace with your
# own implementation.
#
######################################################################


import os




class BooksDictionary (object):

    def __init__ (self):

        self._content = {}


    def create (self, isbn, author, title, year, pages, publisher, country):

        # COMPLETE this method
        pass


    def read (self,isbn):

        # COMPLETE this method
        pass


    def update (self, isbn, author, title, year, pages, publisher, country):

        # COMPLETE this method
        pass


    def delete (self,isbn):

        # COMPLETE this method
        pass


    def isbns (self):

        # COMPLETE this method
        pass




    

class BooksTuples (object):


    def __init__ (self):

        self._content = []


    def create (self, isbn, author, title, year, pages, publisher, country):

        # COMPLETE this method
        pass


    def read (self,isbn):

        # COMPLETE this method
        pass

    
    def update (self, isbn, author, title, year, pages, publisher, country):

        # COMPLETE this method
        pass

                        
    def delete (self,isbn):

        # COMPLETE this method
        pass

    
    def isbns (self):

        # COMPLETE this method
        pass




    

class BooksFile (object):

    def __init__ (self,filename):

        self._filename = filename
        # clear out file if it already exists
        with open(filename,"wb") as f:
            f.write("")


    def create (self, isbn, author, title, year, pages, publisher, country):

        # COMPLETE this method
        pass


    def read (self,isbn):

        # COMPLETE this method
        pass

    
    def update (self, isbn, author, title, year, pages, publisher, country):

        # COMPLETE this method
        pass
            
                        
    def delete (self,isbn):

        # COMPLETE this method
        pass

    
    def isbns (self):

        # COMPLETE this method
        pass




    

def make_test (i):

    for book in [("0345349571","Tuchman","A Distant Mirror",1972,677,"Balantine Books","US"),
                 ("034538623X","Tuchman","The Guns of August",1962,511,"Balantine Books","US"),
                 ("0393356182","Gaiman","Norse Mythology",2017,299,"W. W. Norton","US"),
                 ("0307274939","Keegan","The American Civil War",2009,396,"Random House","US"),
                 ("0679768173","Kertzer","The Kidnapping of Edgardo Mortara",1997,350,"Random House","US"),
                 ("0387972722","Edgar","Measure, Topology, and Fractal Geometry",1990,230,"Springer-Verlag","US"),
                 ("0375724886","Lethem","The Fortress of Solitude",2003,509,"Random House","US"),
                 ("1101972120","Chiang","Stories of Your Life and Others",2002,281,"Random House","US"),
                 ("0812980660","MacMillan","The War That Ended Peace",2014,739,"Random House","US"),
                 ("0060558121","Gaiman","American Gods",2003,591,"Harper Collins","US"),
                 ("0062255655","Gaiman","The Ocean at the End of the Lane",2013,181,"William Morrow","US"),
                 ("0571205992","Lethem","The Wall of the Sky, the Wall of the Eye",1996,232,"Faber and Faber","UK") ]:
        i.create(*book)





def find_books_of_author (coll,author):

    # COMPLETE this function
    pass




def count_pages_of_author (coll,author):

    # COMPLETE this function
    pass




def group_books_by_author (coll):

    # COMPLETE this function
    pass




def count_pages_by_author (coll):

    # COMPLETE this function
    pass




def remove_old_books_of_author (books,author,year):

    # COMPLETE this function
    pass
        
