######################################################################
#
# HOMEWORK 4
#
# Due: Sun 4/28/19 23h59.
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

class RelationUnsorted:
    
    def __init__ (self, columns, primary_key, tuples=[]):
        self._columns = columns
        self._primary_key = primary_key
        self._tuples = File(3)
        self._tuples.allocate_block()
        for tup in tuples:
            self.create_tuple(tup)

    def columns (self):
        
        return self._columns

    def primary_key (self):
        
        return self._primary_key

    def primary_key_of (self,t):
        
        indices = [ self._columns.index(c) for c in self._primary_key ]
        return tuple([ t[i] for i in indices])
    
    def create_tuple (self,tup):

        raise Exception("Not implemented")

    def read_tuple (self,pkey):
        
        raise Exception("Not implemented")

    def delete_tuple (self,pkey):
        
        raise Exception("Not implemented")
    
    def statistics (self):
        
        return self._tuples.statistics()

    
class RelationSorted:
    
    def __init__ (self, columns, primary_key, tuples=[]):
        
        self._columns = columns
        self._primary_key = primary_key
        self._tuples = File(3)
        self._tuples.allocate_block()
        for tup in tuples:
            self.create_tuple(tup)

    def columns (self):
        
        return self._columns

    def primary_key (self):
        
        return self._primary_key
    
    def primary_key_of (self,t):
        
        indices = [ self._columns.index(c) for c in self._primary_key ]
        return tuple([ t[i] for i in indices])

    def create_tuple (self,tup):

        raise Exception("Not implemented")

    def read_tuple (self,pkey):
        
        raise Exception("Not implemented")

    def delete_tuple (self,pkey):
        
        raise Exception("Not implemented")

    def statistics (self):
        
        return self._tuples.statistics()
    

class RelationIndexed:
    
    def __init__ (self, columns, primary_key, tuples=[]):
        
        self._columns = columns
        self._primary_key = primary_key
        self._tuples = File(3)
        self._tuples.allocate_block()   # for the index
        self._tuples.allocate_block()   # content starts here
        for tup in tuples:
            self.create_tuple(tup)

    def columns (self):
        
        return self._columns

    def primary_key (self):
        
        return self._primary_key
    
    def primary_key_of (self,t):
        
        indices = [ self._columns.index(c) for c in self._primary_key ]
        return tuple([ t[i] for i in indices])

    def create_tuple (self,tup):

        raise Exception("Not implemented")

    def read_tuple (self,pkey):
        
        raise Exception("Not implemented")

    def delete_tuple (self,pkey):
        
        raise Exception("Not implemented")
    
    def statistics (self):
        
        return self._tuples.statistics()
    
    

############################################################
# API to files and blocks
#
# A file is basically an array of blocks
# You have to pull and push blocks explicitly to work on them
# and commit your changes
#
# A block is basically an array of tuples
#
# I know this code looks weird, really weird. That's because
# I really wanted to hide the underlying implementation as an
# array so that you're forced to use the API I provide. Python
# makes it ridiculously difficult to enforce this kind of thing
# (something that would be _private_ in a different language).
#
# The code below relies on an underlying implementation in terms of
# FileImplementation and BlockImplementation, where the implementation
# objects are hidden in the local environment of a closure. If this
# is mysterious, find a copy of "Structure and Interpretation of
# Computer Programs" and read chapter 3.
#
    
class File:
    
    def __init__ (self,block_size):

        self._impl = (lambda obj : { "__repr__": (lambda : FileImplementation.__repr__(obj)),
                                     "size": (lambda : FileImplementation.size(obj)),
                                     "allocate_block": (lambda : FileImplementation.allocate_block(obj)),
                                     "pull": (lambda i : FileImplementation.pull(obj,i)),
                                     "push": (lambda i,block : FileImplementation.push(obj,i,block)),
                                     "statistics": (lambda : FileImplementation.statistics(obj)) })(FileImplementation(block_size))

    def __repr__ (self):
        
        return self._impl["__repr__"]()

    def size (self):
        
        return self._impl["size"]()

    def allocate_block (self):
        
        return self._impl["allocate_block"]()

    def pull (self,i):
        
        return self._impl["pull"](i)

    def push (self,i,block):
        
        return self._impl["push"](i,block)

    def statistics (self):
        
        return self._impl["statistics"]()

    
class Block:

    def __init__ (self,size):

        self._impl = (lambda obj : { "__repr__": (lambda : BlockImplementation.__repr__(obj)),
                                     "has_space": (lambda : BlockImplementation.has_space(obj)),
                                     "size": (lambda : BlockImplementation.size(obj)),
                                     "last": (lambda : BlockImplementation.last(obj)),
                                     "append": (lambda tup : BlockImplementation.append(obj,tup)),
                                     "get": (lambda i : BlockImplementation.get(obj,i)),
                                     "put": (lambda i,tup : BlockImplementation.put(obj,i,tup)),
                                     "clone": (lambda : BlockImplementation.clone(obj)) })(BlockImplementation(size))

    def __repr__ (self):

        return self._impl["__repr__"]()
        
    def has_space (self):
        
        return self._impl["has_space"]()

    def size (self):

        return self._impl["size"]()

    def last (self):
        
        return self._impl["last"]()
        
    def append (self,tup):
        
        return self._impl["append"](tup)

    def get (self,i):
        
        return self._impl["get"](i)

    def put (self,i,tup):
        
        return self._impl["put"](i,tup)
    
    def clone (self):
        
        return self._impl["clone"]()
                                             



############################################################
# Implementation of blocks and files
#

class FileImplementation:
    
    def __init__ (self,block_size):

        self._content = []
        self._size = 0
        self._read = 0
        self._write = 0
        self._block_size = block_size

    def __repr__ (self):

        fmt = "------------------------------------------------------------\n{}\n"
        return "".join([ fmt.format(x) if x else "-" for x in self._content ])
    
    def size (self):
        
        return self._size

    def allocate_block (self):
        
        self._size += 1
        self._content.append(Block(self._block_size))

    def pull (self,i):
        
        if i in range(self._size):
            self._read += 1
            print("[ reading block @ {}: {} ]".format(i,self._content[i]))
            return self._content[i].clone()

    def push (self,i,block):
        
        if i in range(self._size):
            self._write += 1
            print("[ writing block @ {}: {} ]".format(i,block))
            self._content[i] = block

    def statistics (self):
        
        return (self._read,self._write)

    
class BlockImplementation:
    
    def __init__ (self, size):
        
        self._size = size;
        self._available = 0;
        self._content = [ None for _ in range(size) ]

    def __repr__ (self):

        return " ".join([ str(x) if x else "-" for x in self._content ])
        
    def has_space (self):
        
        return self._available < self._size

    def size (self):

        return self._size

    def last (self):
        
        return self._available-1
    
    def append (self,tup):
        
        if self.has_space():
            self.put(self._available,tup)
            self._available += 1
        else:
            raise Exception("Problem!")

    def get (self,i):
        
        if i in range(self._size):
            return self._content[i]

    def put (self,i,tup):
        
        if i in range(self._size):
            self._content[i] = tup

    def clone (self):
        
        copy = Block(self._size)
        for i in range(0,self._available):
            copy.append(self.get(i))
        for i in range(self._available,self._size):
            copy.put(i,self.get(i))
        return copy


# Uncomment as appropriate to have some testing relations

# BOOKS = RelationUnsorted(["title","year","numberPages","isbn"],
#                          ["isbn"],
#                          [              
#                              ( "A Distant Mirror", 1972, 677, "0345349571"),
#                              ( "The Guns of August", 1962, 511, "034538623X"),
#                              ( "Norse Mythology", 2017, 299, "0393356182"),
#                              ( "American Gods", 2003, 591, "0060558121"),
#                              ( "The Ocean at the End of the Lane", 2013, 181, "0062255655"),
#                              ( "Good Omens", 1990, 432, "0060853980"),
#                              ( "The American Civil War", 2009, 396, "0307274939"),
#                              ( "The First World War", 1999, 500, "0712666451"),
#                              ( "The Kidnapping of Edgardo Mortara", 1997, 350, "0679768173"),
#                              ( "The Fortress of Solitude", 2003, 509, "0375724886"),
#                              ( "The Wall of the Sky, The Wall of the Eye", 1996, 232, "0571205992"),
#                              ( "Stories of Your Life and Others", 2002, 281, "1101972120"),
#                              ( "The War That Ended Peace", 2014, 739, "0812980660"),
#                              ( "Sheaves in Geometry and Logic", 1994, 630, "0387977102"),
#                              ( "Categories for the Working Mathematician", 1978, 317, "0387984032"),
#                              ( "The Poisonwood Bible", 1998, 560, "0060175400")
#                          ])

# BOOKS = RelationSorted(["title","year","numberPages","isbn"],
#                          ["isbn"],
#                          [              
#                              ( "A Distant Mirror", 1972, 677, "0345349571"),
#                              ( "The Guns of August", 1962, 511, "034538623X"),
#                              ( "Norse Mythology", 2017, 299, "0393356182"),
#                              ( "American Gods", 2003, 591, "0060558121"),
#                              ( "The Ocean at the End of the Lane", 2013, 181, "0062255655"),
#                              ( "Good Omens", 1990, 432, "0060853980"),
#                              ( "The American Civil War", 2009, 396, "0307274939"),
#                              ( "The First World War", 1999, 500, "0712666451"),
#                              ( "The Kidnapping of Edgardo Mortara", 1997, 350, "0679768173"),
#                              ( "The Fortress of Solitude", 2003, 509, "0375724886"),
#                              ( "The Wall of the Sky, The Wall of the Eye", 1996, 232, "0571205992"),
#                              ( "Stories of Your Life and Others", 2002, 281, "1101972120"),
#                              ( "The War That Ended Peace", 2014, 739, "0812980660"),
#                              ( "Sheaves in Geometry and Logic", 1994, 630, "0387977102"),
#                              ( "Categories for the Working Mathematician", 1978, 317, "0387984032"),
#                              ( "The Poisonwood Bible", 1998, 560, "0060175400")
#                          ])

# BOOKS = RelationIndexed(["title","year","numberPages","isbn"],
#                         ["isbn"],
#                         [              
#                             ( "A Distant Mirror", 1972, 677, "0345349571"),
#                             ( "The Guns of August", 1962, 511, "034538623X"),
#                             ( "Norse Mythology", 2017, 299, "0393356182"),
#                             ( "American Gods", 2003, 591, "0060558121"),
#                             ( "The Ocean at the End of the Lane", 2013, 181, "0062255655"),
#                             ( "Good Omens", 1990, 432, "0060853980"),
#                             ( "The American Civil War", 2009, 396, "0307274939"),
#                             ( "The First World War", 1999, 500, "0712666451"),
#                             ( "The Kidnapping of Edgardo Mortara", 1997, 350, "0679768173"),
#                             ( "The Fortress of Solitude", 2003, 509, "0375724886"),
#                             ( "The Wall of the Sky, The Wall of the Eye", 1996, 232, "0571205992"),
#                             ( "Stories of Your Life and Others", 2002, 281, "1101972120"),
#                             ( "The War That Ended Peace", 2014, 739, "0812980660"),
#                             ( "Sheaves in Geometry and Logic", 1994, 630, "0387977102"),
#                             ( "Categories for the Working Mathematician", 1978, 317, "0387984032"),
#                             ( "The Poisonwood Bible", 1998, 560, "0060175400")
#                         ])

    
    
