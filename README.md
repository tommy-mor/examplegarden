Intro
=====

My project is to do with exprgarden. Exprgraden asks the question: what if we used several (~~ 1-3) canonical examples of a type, rather than the mathematical description of the general type ("i am a string", "jdbc:postgres/arostienarsoitne", "i am, a row, in a csv file" VS java.util.String)

My project is to explore the design space of this question using clojure, hopefully getting a useful tool that I will be able to use.


Milestones
=====

There are several milestones of usefulness that I want to add.
  1. (COMPLETED). middleware integration to cider, way to store/recall only function arguments using #record #remember reader tags.
  2. (takes ~30m). replace #remember with cider protocol extension, bind that to key in emacs.
  3. (takes ~2hr?). hook into cider protocol for hover tooltip, show examples there.

  5. expgarden/flow: examples flow deeper into functions, you can hover over the names of variables in let bindings.

  6. exgrgarden/checkpoint: not everything should be stored. many/most of the things can be calculated on the fly. The interface should allow you this control.

  6. exprgarden/branch: your data thread/vine follows the happy path. you have an unhappy path somewhere you want to test. somehow you branch/fork/force a change in your data example to explore the sad path.

  7. exprgarden/obsolete: when you change something, stored data can possibly become obsolete. but if its a lovingly maintained branch, it would be mean to delete it automatically. How to handle dead branches. Handling is easy=delete or reattach. The hard part is visualizing/knowing about the dead branches.

  7. expgarden/gui: there is a lot of data. there are branches, multiple overlapping branches, some of them dead. some of the branches are saved in database, some of them are computed on the fly. All of this may not fit nicely into tooltips and existin editor features. It may be good to make a user interface to see this info. There is also several precedents/prior works in the space of repl-attached data visualizers for clojure. Also gui will have buttons/interface for controlling/gardening/threads.

  10. exprgarden/turbo: expgarden/flow tightens the feedback loop from writing expression to seeing its value in context. What if we tighten that so tightly, we zip it up. What if as soon as you save the file/release a key, you see the value propogated.

  11. exprgarden/spec: This data can be used to genrate clojure specs (predicate based data description language). That would be cool.

  12. exprgarden/check, exprgarden/coverage: This data can be used to "typecheck" the code, (in clojure that means make sure that everything runs without giving exception). Can also produce coverage data about repo.

  13. exprgarden/factory: How to handle data that is not serializable. In most clojure codebases, this is only the database connection. I figure that can be done using a cached factory method somewhere in repo that exprgarden knows about.


Questions
=====

  1. Describe the goals of your project: are you seeking to develop new functionality for an existing application, develop a greenfield application, or evaluate some existing system?

Greenfield application.

  2. What will be the concrete deliverables that you create?  If there will be an evaluation aspect, what metrics will you capture?  What technologies will you use?

Clojure repo/cider extension.

  3.  What are the major risks that you see in the project that you are proposing? Do you have contingency plans in case some key aspect of the project doesn’t work out?

I don't see any major risks. I think it is very likely that I will produce a tool that I am able to use daily. I don't think I will be able to meet the deeper milestones in 6 weeks though. I think I will get up to milestone 5 at the very least.


  4. (If a team project): What are the high level roles and responsibilities of each team member? It is often helpful to have some notion of task ownership, rater than a “we are all responsible for everything”

N/A


5.  New compared to preliminary proposal: What are the high-level tasks that you will need to complete in order to accomplish this project? By when do you plan to accomplish each? Note that you will need to provide a status update on your project on Mar 28 - it may be wise to ensure that any particularly high-risk tasks are scheduled to be completed before the status update, such that we can use that checkpoint to discuss alternatives as needed.

I want to finish 1,2,3,5 by end of first week after break. From there I will readjust timing.