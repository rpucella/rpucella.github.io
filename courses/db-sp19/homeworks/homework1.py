######################################################################
#
# HOMEWORK 0
#
# Due: Sun 2/17/19 23h59.
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


class ERModel (object):

    def __init__ (self):

        pass

    def entity_sets (self):

        pass

    def create_entity_set (self,name,attributes,attributes_key):

        pass

    def read_entity_set (self,name):

        pass

    def relationship_sets (self):

        pass

    def create_relationship_set (self,name,roles,attributes=[]):

        pass
    
    def read_relationship_set (self,name):

        pass


class EntitySet (object):

    def __init__ (self,attributes,attributes_key):

        pass

    def entity_keys (self):

        pass

    def create_entity (self,attributes):

        pass

    def read_entity (self,key):

        pass
            
    def delete_entity (self,key):

        pass
    

class Entity (object):

    def __init__ (self,attributes,attributes_key):
        
        self._attributes = attributes

    def __str__ (self):
        
        attrs = [ "{}={}".format(name,val) for (name,val) in self._attributes.items() ]
        return "[{}]".format("|".join(attrs))

    def attribute (self,name):
        
        pass

    def primary_key (self):

        pass
    

class RelationshipSet (object):

    def __init__ (self,roles,attributes):

        pass
    
    def relationship_keys (self):

        pass

    def create_relationship (self,role_keys,attributes={}):

        pass

    def read_relationship (self,pkey):

        pass
            
    def delete_relationship (self,pkey):

        pass


class Relationship (object):

    def __init__ (self,role_keys,attributes):

        self._role_keys = role_keys
        self._attributes = attributes

    def __str__ (self):

        rkey = lambda r : [ "{}={}".format(name,val) for (name,val) in self._role_keys[r].items() ]
        entities = " ".join([ "[{}]".format("|".join(rkey(r))) for r in self._role_keys ])
        attrs = [ "{}={}".format(name,val) for (name,val) in self._attributes.items() ]
        return "<{} {}>".format(entities,"|".join(attrs))

    def attribute (self,name):
        
        pass

    def role_key (self,role):

        pass
    
    def primary_key (self):

        pass
    
                     

def sample_entities_model ():

    collection = ERModel()

    collection.create_entity_set("Books", ["title", "numberPages", "year", "isbn"], ["isbn"])

    books = collection.read_entity_set("Books")

    books.create_entity({ "title": "A Distant Mirror",
                          "numberPages": 677,
                          "year": 1972,
                          "isbn": "0345349571" })
    
    books.create_entity({ "isbn": "034538623X",
                          "title": "The Guns of August",
                          "year": 1962,
                          "numberPages": 511 })
    
    books.create_entity({ "isbn": "0393356182",
                          "title": "Norse Mythology",
                          "year": 2017,
                          "numberPages": 299 })
     
    books.create_entity({ "isbn": "0060558121",
                          "title": "American Gods",
                          "year": 2003,
                          "numberPages": 591 })
    
    books.create_entity({ "isbn": "0062255655",
                          "title": "The Ocean at the End of the Lane",
                          "year": 2013,
                          "numberPages": 181 })

    books.create_entity({ "isbn": "0060853980",
                          "title": "Good Omens",
                          "year": 1990,
                          "numberPages": 432 })
    
    
    books.create_entity({ "isbn": "0307274939",
                          "title": "The American Civil War",
                          "year": 2009,
                          "numberPages": 396 })

    books.create_entity({ "isbn": "0712666451",
                          "title": "The First World War",
                          "year": 1999,
                          "numberPages": 500})
    
    books.create_entity({ "isbn": "0679768173",
                          "title": "The Kidnapping of Edgardo Mortara",
                          "year": 1997,
                          "numberPages": 350 })
    
    books.create_entity({ "isbn": "0375724886",
                          "title": "The Fortress of Solitude",
                          "year": 2003,
                          "numberPages": 509 })
    
    books.create_entity({ "isbn": "0571205992",
                          "title": "The Wall of the Sky, The Wall of the Eye",
                          "year": 1996,
                          "numberPages": 232 })

    books.create_entity({ "isbn": "1101972120",
                          "title": "Stories of Your Life and Others",
                          "year": 2002,
                          "numberPages": 281 })
    
    books.create_entity({ "isbn": "0812980660",
                          "title": "The War That Ended Peace",
                          "year": 2014,
                          "numberPages": 739 })

    books.create_entity({ "isbn": "0387977102",
                          "title": "Sheaves in Geometry and Logic",
                          "year": 1994,
                          "numberPages": 630})

    books.create_entity({ "isbn": "0387984032",
                          "title": "Categories for the Working Mathematician",
                          "year": 1978,
                          "numberPages": 317})

    books.create_entity({ "isbn": "0060175400",
                          "title": "The Poisonwood Bible",
                          "year": 1998,
                          "numberPages": 560})
    
    
    collection.create_entity_set("Persons", ["firstName", "lastName", "birthYear"], ["lastName"])

    persons = collection.read_entity_set("Persons")

    persons.create_entity({"firstName": "Barbara",
                           "lastName": "Tuchman",
                           "birthYear": 1912 })

    persons.create_entity({"firstName": "Neil",
                           "lastName": "Gaiman",
                           "birthYear": 1960 })

    persons.create_entity({"firstName": "Terry",
                           "lastName": "Pratchett",
                           "birthYear": 1948})

    persons.create_entity({"firstName": "John",
                           "lastName": "Keegan",
                           "birthYear": 1934})

    persons.create_entity({"firstName": "Jonathan",
                           "lastName": "Lethem",
                           "birthYear": 1964})

    persons.create_entity({"firstName": "Margaret",
                           "lastName": "MacMillan",
                           "birthYear": 1943})

    persons.create_entity({"firstName": "David",
                           "lastName": "Kertzer",
                           "birthYear": 1948})

    persons.create_entity({"firstName": "Ted",
                           "lastName": "Chiang",
                           "birthYear": 1967})

    persons.create_entity({"firstName": "Saunders",
                           "lastName": "Mac Lane",
                           "birthYear": 1909})

    persons.create_entity({"firstName": "Ieke",
                           "lastName": "Moerdijk",
                           "birthYear": 1958})

    persons.create_entity({"firstName": "Barbara",
                           "lastName": "Kingsolver",
                           "birthYear": 1955})

    collection.create_relationship_set("AuthoredBy", { "book": "Books",
                                                       "author": "Persons" })
    
    return collection


def sample_relationships_model ():

    collection = sample_entities_model()

    authored = collection.read_relationship_set("AuthoredBy")

    authored.create_relationship({ "book": { "isbn": "0345349571"},
                                   "author": { "lastName": "Tuchman" } })
    
    authored.create_relationship({ "book": { "isbn": "034538623X"},
                                   "author": { "lastName": "Tuchman" } })

    authored.create_relationship({ "book": { "isbn": "0393356182" },
                                   "author": { "lastName": "Gaiman" } })
    
    authored.create_relationship({ "book": { "isbn": "0060558121" },
                                   "author": { "lastName": "Gaiman" } })
    
    authored.create_relationship({ "book": { "isbn": "0062255655" },
                                   "author": { "lastName": "Gaiman" } })

    authored.create_relationship({ "book": { "isbn": "0060853980" },
                                   "author": { "lastName": "Gaiman" } })

    authored.create_relationship({ "book": { "isbn": "0060853980" },
                                   "author": { "lastName": "Pratchett" } })
    
    authored.create_relationship({ "book": { "isbn": "0307274939" },
                                   "author": { "lastName": "Keegan" } })
    
    authored.create_relationship({ "book": { "isbn": "0712666451" },
                                   "author": { "lastName": "Keegan" } })
    
    authored.create_relationship({ "book": { "isbn": "1101972120" },
                                   "author": { "lastName": "Chiang" } })
    
    authored.create_relationship({ "book": { "isbn": "0679768173" },
                                   "author": { "lastName": "Kertzer" } })
    
    authored.create_relationship({ "book": { "isbn": "0812980660" },
                                   "author": { "lastName": "MacMillan" } })
    
    authored.create_relationship({ "book": { "isbn": "0571205992" },
                                   "author": { "lastName": "Lethem" } })
    
    authored.create_relationship({ "book": { "isbn": "0375724886" },
                                   "author": { "lastName": "Lethem" } })
    
    authored.create_relationship({ "book": { "isbn": "0387977102" },
                                   "author": { "lastName": "Mac Lane" } })

    authored.create_relationship({ "book": { "isbn": "0387977102" },
                                   "author": { "lastName": "Moerdijk" } })

    authored.create_relationship({ "book": { "isbn": "0387984032" },
                                   "author": { "lastName": "Mac Lane" } })

    authored.create_relationship({ "book": { "isbn": "0060175400" },
                                   "author": { "lastName": "Kingsolver"} })
    
    return collection


def show_title_books_more_500_pages ():

    pass


def show_title_books_by_barbara ():

    pass


def show_name_authors_more_one_book ():

    pass

                
def show_title_books_more_one_author ():

    pass

     
    
def tests (): 
    
    print("----------------------------------------")
    show_title_books_more_500_pages()
    
    print("----------------------------------------")
    show_title_books_by_barbara()
    
    print("----------------------------------------")
    show_name_authors_more_one_book()
    
    print("----------------------------------------")
    show_title_books_more_one_author()

