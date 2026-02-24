---
title: Remembering Joe Halpern
date: 2026-02-21
reading: England, England (by Julian Barnes)
prevTitle: Thoughts On MCP Servers
prevUrl: ../../2026/02-thoughts-on-mcp-servers
---

[Joe Halpern passed away](https://news.cornell.edu/stories/2026/02/joe-halpern-towering-computer-scientist-and-mentor-dies-72) in Ithaca on Friday February 13. Joe was my Ph.D. advisor at Cornell twenty-five years ago, and I'm still coming to grips with his death.

Joe was a well-known researcher, staking his home at the intersection of a number of fields: computer science, mathematics, philosophy, and game theory. His eclecticism (my word) was one of the reasons I enjoyed working with him so much. 

The work that cemented his fame was the application of epistemic concepts (knowledge and belief) to the study of distributed computation, in particular the definition and formalization of [common knowledge](https://arxiv.org/abs/cs/0006009) with Y. Moses ("everybody knows X and everybody knows that everybody knows X and everybody knows that everybody knows that everybody knows X and ...") as both a delightful fixed point compution and a central concept for the analysis of distributed programs. In short: coordinated action requires common knowledge, but common knowledge is not achievable in the presence of asynchronous communication, so we need to rely on weaker forms of knowledge in those environments, with an impact on the possible protocols that we can bring to bear on the problem.

He was a big proponent of crafting logics to formalize concepts of interest. I used to point to [_Why Bother With Syntax?_](https://arxiv.org/abs/1506.05282) as a reference for why one might want a logical syntax for analyses that can often be performed directly on the underlying mathematical structures. But he was not constrained by this point of view, and was perfectly happy to work in a fully semantic domain, to use the terminology of the field.

He was a dyed in the wool Tarskian. I came from a programming languages background, so logic for me was constructive proof theory. It was a bit of a shock to enter his world. But enter it I did, and eventually made sense of it. We contain multitudes.

Joe's breadth was phenomenal. I had not been aware until late in our collaboration that he created (with E. A. Emerson) the logic [CTL*](https://research.ibm.com/publications/sometimes-and-not-never-revisited-on-branching-versus-linear-time-temporal-logic), a temporal logic that embodies both linear time and branching time operators under a unifying framework. He co-created the [TARK](http://www.tark.org/) conference series, _Theoretical Aspects of Rationality and Knowledge_, a showcase of a small subset of his interests. His work with Judea Pearl on [actual](https://arxiv.org/abs/cs/0011012) [causality](https://arxiv.org/abs/cs/0208034) was an important addition to a complex and admittedly contentious field. He was [deeply involved](https://arxiv.org/abs/cs/0005003) in the creation of the [Computing Research Repository](https://info.arxiv.org/help/cs/index.html) (CoRR) in arXiv.

Joe made me think of Erdös. Not that I ever met Erdös, but you always hear about Erdös's peripatetic life filled with collaborations, and that has always been my image of Joe at conferences. Your attention would wander for a minute, and when you'd go back to look for him, he'd be in a corner of the room, sitting and scribbling mathematics with a colleague on a pad of paper. He was one of the most collaborative researchers I've ever met, and anybody that has had the pleasure of working with him will attest to the insight, taste, and passion he would bring to the work.

I cannot to this day prepare a presentation at work without picturing Joe behind his desk in his old Upson Hall office at Cornell, leaning back in his chair and asking me "so what's the story?"

I started to work with Joe by accident. In my first year of graduate school, I took his class on knowledge, which was well established. The following year, I took his class on uncertainty, which was still being developed. During a lecture on representations of uncertainty (probabilities, Dempster-Shafer belief functions, possibility measures, that sort of thing), he presented finite axiomatizations for all of these representations. Most people are familiar with the standard [Kolmogorov axioms for probability measures](https://en.wikipedia.org/wiki/Probability_axioms). Similar axiomatizations exist for most other representations. The one representation whose finite axiomatization was not known—including whether such a finite axiomatization even existed—was upper and lower probability measures. An upper (lower) probability measure is the pointwise supremum (infimum) of a set of probability measures. It felt weird to me that axiomatizations for those would not have been investigated, so I started noodling with the problem of finding one. I talked to Joe about it over several weeks, dug into the literature, made some progress. After the end of the course, we cracked the problem and showed that not only was there a finite axiomatization, but we could exhibit it and prove it complete: all properties of upper and lower probability measures were consequences of these axioms. The [resulting paper](https://arxiv.org/abs/cs/0307069) was my first with Joe, and inaugurated my apprenticeship at the feet of the master. And yes, he would have balked at that characterization.

It is the one paper I wrote of which I'm proud. It is not an important paper, and there must be only a handful of people in the world that would care enough to understand the details. But it contains the hardest piece of research mathematics that I ever grappled with, Theorem 2.4. I came across a comment by mathematician J. E. Littlewood about Bertrand Russell a long time ago:

> He had a secret craving to have proved _some_ straight mathematical theorem. As a matter of fact
> there _is_ one: 2<sup>2<sup>a</sup></sup> > ℵ<sub>0</sub> if a is infinite. Perfectly good
> mathematics. (This weakness is very common with people who take the Mathematical Tripos and then
> switch [...]).
> 
> _(Littlewood, A Mathematician's Miscellany, 1953)_

That's me, right there, secret craving and all. Theorem 2.4 is my straight mathematical theorem, my one mathematical contribution to the world. I'm unabashedly proud of it. And Joe is and always will be inextricably linked to that feeling.

Safe travels, Joe.
