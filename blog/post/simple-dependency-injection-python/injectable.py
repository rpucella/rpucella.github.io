'''
Low-impact dependency injection class decorator
'''

def injectable(*services):
    '''
    Class decorator for injecting services (as classes)

    When given class names used in the body of the decorated class, it makes those
    class names optional parameters to the decorated constructor so that
    the decorated class can use alternate class implementations for those services.
    By default, the named classes will be used as services.

    To use a class injected by the decorator, you need to use the self.ClassName
    form (since injectable classes are stored in a local variable by the decorator)
    '''
    def inject(cls):
        original_init = cls.__init__
        
        names = [ service.__name__ for service in services ]

        def new_init (self, *args, **kwargs):
            kwargs_ = { k: kwargs[k] for k in kwargs if k not in names }
            self._services =  { service.__name__: service for service in services}
            for n in names:
                if n in kwargs:
                    self._services[n] = kwargs[n]
            original_init(self, *args, **kwargs_)

        def get_attribute(self, attr):
            if attr in self._services:
                return self._services[attr]
            else:
                raise Exception('no such attribute')

        cls.__init__ = new_init
        cls.__getattr__ = get_attribute
        return cls
    
    return inject


#
# Example class using one of two services
#
#   >>> x = Example(10)
#   >>> x.run()
#   In default service = 11
#   >>> y = Example(10, Service=Service2)
#   >>> y.run()
#   In service 2 = 110
#

class Service:
    def __init__(self, arg):
        self._arg = arg + 1

    def print(self):
        print(f'In default service = {self._arg}')


class Service2:
    def __init__(self, arg):
        self._arg = arg + 100

    def print(self):
        print(f'In service 2 = {self._arg}')
        
    
@injectable(Service)
class Example:
    def __init__(self, arg):
        self._arg = arg

    def run(self):
        obj = self.Service(self._arg)
        obj.print()
