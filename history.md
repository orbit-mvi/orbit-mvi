# A bit of history

We originally set out to create Orbit with the following principles in mind:

- Simple
- Flexible
- Testable
- Designed for, but not limited to Android

Orbit 1 was our first attempt at this, and while it worked well in general, it
fell short of our expectations when it came to its flexibility and testability.
It did not support coroutines with support hard to incorporate, as it was
rigidly dependent on RxJava 2. The users were not shielded from this either. As
we were migrating to coroutines ourselves, this was increasing the complexity
of our code.

We thought we had taken Orbit 1 as far as we could. Having learned a great deal
about MVI in Orbit 1, we set out to take another shot at this. We resolved to
keep the good things of Orbit 1 and redesign it from the ground up to live up
to our standards as Orbit 2. We think - hopefully, finally - we hit the sweet
spot.

We stand on the shoulders of giants:

- [Managing State with RxJava by Jake Wharton](https://www.reddit.com/r/androiddev/comments/656ter/managing_state_with_rxjava_by_jake_wharton/)
- [RxFeedback](https://github.com/NoTests/RxFeedback.kt)
- [Mosby MVI](https://github.com/sockeqwe/mosby)
- [MvRx](https://github.com/airbnb/MvRx)

Thank you so much to everyone in the community for the support, whether direct
or not.
