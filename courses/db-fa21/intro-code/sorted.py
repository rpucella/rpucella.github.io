


book_list = [
    ('0345349571', 'Tuchman', 'A Distant Mirror', 1972, 677, 'Balantine Books', 'US'),
    ('034538623X', 'Tuchman', 'The Guns of August', 1962, 511, 'Balantine Books', 'US'),
    ('0393356182', 'Gaiman', 'Norse Mythology', 2017, 299, 'W. W. Norton', 'US'),
    ('0307274939', 'Keegan', 'The American Civil War', 2009, 396, 'Random House', 'US'),
    ('0679768173', 'Kertzer', 'The Kidnapping of Edgardo Mortara', 1997, 350, 'Random House', 'US'),
    ('0387972722', 'Edgar', 'Measure,  Topology,  and Fractal Geometry', 1990, 230, 'Springer-Verlag', 'US'),
    ('0375724886', 'Lethem', 'The Fortress of Solitude', 2003, 509, 'Random House', 'US'),
    ('1101972120', 'Chiang', 'Stories of Your Life and Others', 2002, 281, 'Random House', 'US'),
    ('0812980660', 'MacMillan', 'The War That Ended Peace', 2014, 739, 'Random House', 'US'),
    ('0060558121', 'Gaiman', 'American Gods', 2003, 591, 'Harper Collins', 'US'),
    ('0062255655', 'Gaiman', 'The Ocean at the End of the Lane', 2013, 181, 'William Morrow', 'US'),
    ('0571205992', 'Lethem', 'The Wall of the Sky,  the Wall of the Eye', 1996, 232, 'Faber and Faber', 'UK')
]

def make_book(b):
    (isbn, author, title, year, pages, publisher, country) = b
    return {
        'isbn': isbn,
        'author': author,
        'title': title,
        'year': year,
        'pages': pages,
        'publisher': publisher,
        'country': country
    }        

def create_sorted(book, key, arr):
    mn = 0
    mx = len(arr)
    while True:
        ##print(mn, mx)
        if mn == mx:
            arr.insert(mn, book)
            return
        else:
            mid = (mx + mn) // 2
            if arr[mid][key] < book[key]:
                mn = mid + 1
            elif arr[mid][key] > book[key]:
                mx = mid
            else:
                arr.insert(mid, book)
                return

def read_sorted(key, value, arr):
    mn = 0
    mx = len(arr)
    while True:
        ##print(mn, mx)
        if mn == mx:
            return None
        else:
            mid = (mx + mn) // 2
            if arr[mid][key] < value:
                mn = mid + 1
            elif arr[mid][key] > value:
                mx = mid
            else:
                return arr[mid]

def read(field, value, arr):
    # O(n) irrespectively of the field
    result = []
    for b in arr:
        if b[field] == value:
            result.append(b)
    return result

def make_test_sorted():
    books = []
    for b in book_list:
        book = make_book(b)
        create_sorted(book, 'isbn', books)
    return books
