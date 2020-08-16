##
## APPEND THIS FILE TO THE END OF YOUR homework3.py FILE
##

# Docs at https://github.com/pyparsing/pyparsing

import pyparsing as pp


def parseQuery (input):

    # parse a string into an abstract query

    # <sql> ::= select <columns> from <tables> (where <conditions>)? 

    idChars = pp.alphas+"_*"

    pIDENTIFIER = pp.Word(idChars, idChars+"0123456789.")
    pIDENTIFIER.setParseAction(lambda result: result[0])

    pCOMMAIDENT = pp.Suppress(pp.Word(",")) + pIDENTIFIER

    pIDENTIFIER2 = pp.Group(pIDENTIFIER + pIDENTIFIER)

    pCOMMAIDENT2 = pp.Suppress(pp.Word(",")) + pIDENTIFIER2

    pINTEGER = pp.Word("-0123456789","0123456789")
    pINTEGER.setParseAction(lambda result: int(result[0]))

    pSTRING = pp.QuotedString("'")

    pKEYWORD = lambda w : pp.Suppress(pp.Keyword(w,caseless=True))

    pSELECT = pKEYWORD("select") + pp.Group(pIDENTIFIER + pp.ZeroOrMore( pCOMMAIDENT))

    pFROM = pKEYWORD("from") + pp.Group(pIDENTIFIER2 + pp.ZeroOrMore( pCOMMAIDENT2))

    pCONDITION_NEQN = pIDENTIFIER + pp.Word("=") + pIDENTIFIER
    pCONDITION_NEQN.setParseAction(lambda result: ("n=n",result[0],result[2]))
    
    pCONDITION_NEQV1 = pIDENTIFIER + pp.Word("=") + pINTEGER
    pCONDITION_NEQV1.setParseAction(lambda result: ("n=v",result[0],result[2]))
    
    pCONDITION_NEQV2 = pIDENTIFIER + pp.Word("=") + pSTRING
    pCONDITION_NEQV2.setParseAction(lambda result: ("n=v",result[0],result[2]))
    
    pCONDITION_NGEV = pIDENTIFIER + pp.Word(">") + pINTEGER
    pCONDITION_NGEV.setParseAction(lambda result: ("n>v",result[0],result[2]))
    
    pCONDITION = pCONDITION_NEQV1 | pCONDITION_NEQV2 | pCONDITION_NEQN | pCONDITION_NGEV

    pANDCONDITION = pKEYWORD("and") + pCONDITION
    
    pCONDITIONS = pp.Group(pCONDITION + pp.ZeroOrMore( pANDCONDITION))

    pWHERE = pp.Optional(pKEYWORD("where") + pCONDITIONS )

    pSQL = pSELECT + pFROM + pWHERE + pp.StringEnd()
    pSQL.setParseAction(lambda result: { "select": result[0].asList(),
                                         "from": result[1].asList(), 
                                         "where": result[2].asList() if len(result)>2 else []})


    result = pSQL.parseString(input)[0]
    return result    # the first element of the result is the expression




sample_db = {
    "Books": BOOKS,
    "Persons": PERSONS,
    "AuthoredBy": AUTHORED_BY
}



def convert_abstract_query (db,aq):

    pass


def shell (db):
    # Repeatedly read a line of input, parse it, and evaluate the result

    pass

