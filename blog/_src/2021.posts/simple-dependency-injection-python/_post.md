---
title: Simple Dependency Injection in Python
date: 2021-05-08
---

[Dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) is an software design pattern that basically amounts to parameterizing every
object by the services that it uses, and forcing the instantiator of an object to supply objects
representing the services needed by the created object.

I'm somewhat torn on dependency injection. On the positive side, decoupling an object from the services
it uses is almost a requirement for any form of sane unit testing, since then you can test an object
by supplying proxy services that can be controlled by the testing environment. On the negative side, it
adds an extra layer of indirection that makes code difficult to read, difficult to maintain, and often 
adds visual noise that distracts from what an object does. (Dependency injection often comes with an
attendant architectural infrastructure, of the kind best examplified by the Java [Spring Framework](https://spring.io/projects/spring-framework).)

Is it possible to get some of the advantages of dependency injection for unit testing without straying too much from how you would normally code?

Here's a positive answer to the question in the context of Python. It is far from an ideal solution,
but it does provide a lightweight form of dependency injection that should suffice for unit testing.

Consider the following simple `Example` class that depends on a simple `Service` class:

    class Example:
        def __init__(self, arg):
            self._arg = arg
    
        def run(self):
            obj = Service(self._arg)
            obj.print()


    class Service:
        def __init__(self, arg):
            self._arg = arg + 1
    
        def print(self):
            print(f'In default service = {self._arg}')


We want the ability to instantiate `Example` with an alternate service `Service2` in place of `Service` in
some situations (such as testing):

    class Service2:
    
        def __init__(self, arg):
            self._arg = arg + 100
    
        def print(self):
            print(f'In service 2 = {self._arg}')

To do that, we introduce a class decorator `injectable` that takes as arguments all the services used
in the class that we want to be "injectable" at instantiation time. That decorator intuitively adds
an instance variable for every service listed and initialized with the given service. The
initialization can be overridden using a keyword argument on the class constructor with the same
name as the service being injected.. To take advantage of this injection, we simply need to adjust
every reference to an "injectable" service within the class so that it uses the instance variable
named after the service. For example:

    @injectable(Service)
    class Example:
        def __init__(self, arg):
            self._arg = arg
    
        def run(self):
            obj = self.Service(self._arg)
            obj.print()

Instantiating the class with the default service does not require any extra work:

    >>> e1 = Example(10)
    >>> e1.run()
    In default service = 11
    
And instantiating the class with an alternative service simply requires passing the alternative
service during instantiation as a keyword argument for the keyword corresponding to the service to
be injected:

    >>> e2 = Example(10, Service=Service2)
    >>> e2.run()
    In service 2 = 110

The main downside of this approach is the need to qualify every "injectable" service reference with
the `self` argument within the class. adding visual noise and a potential failure point if one such
reference is not qualified while others are.

For completeness, here's a reference implementation of the `injectable` decorator:

    def injectable(*services):
    
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

You can download the [full code](./injectable.py).
