
# Notes on Datalog

SQL is declarative layer on top of relational algebra. But it is not
the only way to query relational data.

Datalog comes from deductive databases. It's an alternative to SQL
that lets you write recursive queries. It is especially useful for
recursive (hierarchical) data.

Consider the following Sample employee hierarchy, as a table `Employee`. 

    
    | name    | manager | daysWorked |
    +---------+---------+------------+
    | bob     | alice   | 100        |
    | xena    | bob     | 150        |
    | yuri    | bob     | 200        |
    | charlie | alice   | 250        |
    | dana    | charlie | 300        |
    | ulric   | dana    | 350        |
    | val     | dana    | 400        |
    
Note that datalog relies on accessing attributes of a query by
position, and not by name.

A datalog query is expressed as asking for all tuples of a relation
that match a certain template. For example, asking for all the tuples
in `Employee` where `bob` is the manager:

    Employee(X, bob, D).
    
Capital letters are placeholder variables. This asks for all tuples
where `b` is in the second (manager) position. The result is usually
expressed as:

    X = xena , D = 150
    X = yuri , D = 200

To write more elaborate queries, datalog lets us express intermediate
relations that you can query. These intermediate relations are
expressed using _deductive rules_. For instance, you can express
a relation capturing an employee's manager's manager's as:

    EmployeeManagerManager(X, Y) :- Employee(X, Z, _), Employee(Z, Y, _)

Read this from right to left: if `X` is an employee with manager `Z`, and
`Z` is an employee with manager `Y`, then `Y` is `X`'s manager's
manager. (Another reading is from left to right: `Y` is `X`'s manager's
manager when `X` is an employee with manager `Z` (for some `Z`) and `Z` is an
employee with manager `Y`. Pick whichever works best for you.) An `_`
means that you don't care about the value being matched.

You can then query for a manager's direct reports' direct reports
with:

    EmployeeManagerManager(X, charlie)

which should return:

    X = ulric
    X = val

We require rules to be _range restricted_: every variable appearing
in the head of a rule (before the `:-`) must also appear in the
body of the rule (after the `:-`).

Rules can be recursive, which lets you express a relation capturing
whether an employee is a report of another, meaning that there is a
chain of manager linking both employees:

    Reports(X, Y) :- Employee(X, Y, _)
    Reports(X, Y) :- Employee(X, Z, _), Reports(Z, Y)

You can read this from left to right as `X` reports to `Y` if `Y` is
`X`'s manager, or when `Z` is `X`'s manager (for some `Z`) and
`Z` reports to `Y`.

If you were to explicitly construct relation `Reports`, it would
look like:

    +---------+---------+
    | bob     | alice   |
    | xena    | alice   |
    | yuri    | alice   |
    | charlie | alice   |
    | dana    | alice   |
    | ulric   | alice   |
    | val     | alice   |
    | xena    | bob     |
    | yuri    | bob     |
    | dana    | charlie |
    | ulric   | charlie |
    | val     | charlie |
    | ulric   | dana    |
    | val     | dana    |
    +---------+---------+

Another example: determining the difference in levels between two employees:


    Difference(X, Y, 1) :- Employee(X, Y, _)
    Difference(X, Y, N) :- Employee(X, Z, _), Difference(Z, Y, M), N = M + 1

Querying for `Difference(u, b, D)` should yield the difference in
levels between `u` and `b`, which is `D = 21`.

Example: two employees are teammates if they have the same manager:

    Teammates(X, Y) :- Employee(X, Z), Employee(Y, Z)

How do we think about the relations constructed by datalog rules? One way to think about it is in terms of something like the tuple relational calculus. If you consider the rules of relation `Reports` above:

    Reports(X, Y) :- Employee(X, Y, _)
    Reports(X, Y) :- Employee(X, Z, _), Reports(Z, Y)

and you reflect about the "meaning" that we gave them, you can think
of them as defining a relation `Reports` defined by the following
property (*): 

    Reports = { t : ∃s ∈ Employee with t[0] = s[0] and t[1] = s[1], 
                    or ∃s ∈ Employee, u ∈ Reports with t[0] = s[0], t[1] = u[1], and s[1] = t[0] }

Note that this is not a proper definition -- you cannot define a set
in terms of itself. The above is an equation, and a solution to the
equation is a set `Reports` that satisfies the equation.

How do you solve such an equation? It's usually tricky business. But
in this case, the properties corresponding to the datalog rules we've
seen all have a monotonicity property: adding more tuples to base
relations only lead to more tuples added to any potential solution to
the equation, and never fewer. This means that we can use a simple
least-fixed-point computation to get a solution:

- Let _S_ be the empty set -- this is our target solution
- Compute _S'_ to be the result obtained from property (*) above, replacing `Reports` in the property by _S_
- Take the new _S_ to be _S_ unioned with _S'_
- Repeat until nothing gets added to _S_
- Take `Reports` to be the final set of tuples _S_. 

Negation is a feature that is useful in a query language, but is also
tricky. For instance, you could write a query that says that there is
no reporting relation between two employees:

    NotReports(X, Y) :- Employee(X, _, _), Employee(Y, _, _), not Reports(X, Y).

One restriction on negation is that a variable can only appear in a
negated atom (a component of the body of a rule) when it also appears
in a non-negated atom. Such a rule is said to be safe.

Another restriction on negation is that the rules must be
_stratified_. This is to ensure that the process we described earlier to
find a solution to equations obtained from datalog rules still
applies. Negation destroys monotonicity, and stratification basically
ensure enough monotonicity for the least-fixed-point computation to
proceed.

Intuitively, statification means partitioning the relations defined by
a set of rules into different strata, where the rules defining
relations in one strata can only use negated atoms involving rules in
a lower stratum.

More formally, say that a relation `R` depends positively on a
relation `S` if every rule for `R` that refers to `S` only uses `S`
without negating it. Say that a relation `R` depends negatively on a
relation `S` if some rule for `R` refers to `S` under a negation.

A stratification for a set of relations defined by rules is a
partition of the relations into strata 1, ..., N such that whenever
`R` depends positively on `S`, then the stratum of `R` is greater than
or equal to the stratum of `S`, and whenever `R` depends negatively on
`S`, then the stratum of `R` is strictly greater than the stratum of
`S`.

This means that we can determine the tuples in all the relations in
statum 1 first. Then use that to determine the tuples in the relations
in stratum 2, and so on. If we ever need to determine that a tuple is
_not_ in a given relation `S` when defining relation `R`, then
since `S` is required to live in a previous stratum, we already know
the content of `S`, and therefore can safely determine whether the
tuple is not in `S` without worrying that the construction will later
add the tuple to `S` and contradict our results. 

Stratification is also useful when considering aggregated Datalog,
which is an extension of Datalog that allows aggregation. It can be
written as follows:

    AvgDaysWorkedDirectReports(X, AVG(<D>)) :- Employee(Y, X, D)

This is roughly equivalent to the SQL query

    SELECT manager, AVG(daysWorked) AS avgDaysWorked
    FROM Employee
    GROUP BY manager

minus the naming of the attributes in the resulting relation.

Again, this can only be made sense of if the relation being aggregated
over is fully known when computing the aggregation. Therefore, the
rules must again be stratified, with a variant definition of
stratification with the additional clause: whenever `R` depends
aggregatively on `S`, then the stratum of `R` is strictly greater than
the stratum of `S`. A relation `R` depends aggregatively on relation
`S` if some rule defining `R` with an aggregation operator over some
variable `X` refers to `S` in the body of the rule using variable
`X`.
