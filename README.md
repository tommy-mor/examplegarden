examplegarden
=======

TLDR: store ~1-3 canonical examples of function arguments in git repo next to code, to replace/surpass a type system. implemented as an nREPL middleware.


> In hindsight, so much of what we hype up as “exploratory programming” in the REPL is really just coping with the lack of useful type information.
> -- [this post](https://discuss.ocaml.org/t/whats-your-development-workflow/10358/8)

> It's much easier for me to generalize from the concrete than conctretize from the general.
> -- a professor once told me

> The values of a program deserve to be tracked in git, not just the source of a program.
> -- my opinion


motivation
===

I have found myself frequently using the "inline def" hack for debugging/interactive development. It may be "too obvious to describe", but [here](https://blog.michielborkent.nl/inline-def-debugging.html) and [here](https://cognitect.com/blog/2017/6/5/repl-debugging-no-stacktrace-required) are good blog posts describing it.

I find this pattern useful for debugging, but also for just development. I inline def all the arguments to a function, run the function (or a different function that calls it) from a Rich comment block or a [rcf test block](https://github.com/hyperfiddle/rcf). Now that all the arg names are defined, I can send the body of the function to the repl as soon as I make a change. I see the values flow through my expressions milliseconds after writing them.

There are some problems with this:
  1. You have to manually write ``(def arg1 arg1)(def arg2 arg2)...`` for every function. This gets annoying quick.
  2. You have to delete those before commiting or risk smelling up your code.
  3. The values you used to write the body of the function are lost to time. Developers in the future must recreate this situation manually to hack on the function body.

high level solution
====

Here is how examplegarden lubricates (tightens up the feedback loop of) the inline def pattern:
  * Store the canonical values to selected function arguments data literals in a ``.examplegarden.edn`` file
  * When hacking on the body, pressing a keybind will evaluate the body in terms of saved values.


This speeds up my normal inline def development pattern. It also has these additional advantages:
  1. Examples show up in function tooltip, so you don't have to "cope with lack of useful type information." You have something better: an actual piece of data.
  2. Other developers (you in the future?) have access to the examples/evaluation context, not just the transient repl of the person writing the inline diffs.
  3. Changes to function inputs are documented by git diff.
  4. The "time to productive change" is much lowered for new team members. Instead of rigging up an entire web app and clicking the button on a form to get the right value in the inline def, you can just immediately start writing a new handler with the correct context of having pressed that button.
  5. You could automatically generate specs using these organically accreting examples.
  6. You could run examples through their functions as low impact, but free tests.

solution details
====
``examplegarden`` is implemented as an nrepl middleware. ``deps.edn``:
```clojure
{:deps {expgarden/exprgarden {:local/root "../examplegarden/"}}
 :aliases {:repl {:main-opts ["-m" "nrepl.cmdline" "--middleware"
 		              "[cider.nrepl/cider-middleware examplegarden.core/examplegarden-hook]"]}}}

```

During development, there are two ways to store an example.
  1. Call the function you intend to record, but with a `#record` reader tag: 
```clojure
 #record (function-i-am-working-on val1 val2 val3)
 ```
  3. Wrap your function in this globally qualified macro:
```clojure
(examplegarden.core/record (defn function-i-am-working-on [{:keys [a b c]} val2 val3] ...))
```
The next call to the instrumented function will be recorded in the .examplegarden.edn file.

Note that every non-serializable value will be replaced by nil. Any time this happens, a warning will print to stdout.

There is one way to recall the examples:
```clojure
#recall (defn function-i-am-working-on ...)
```
This will run the function body using the context from ``.examplegarden.edn``.
I bind a key to do this. The emacs/cider example code is in ``./bind.el``


downsides
====

1. only serializable values are supported. This has been annoying for the database connection object. This has forced me to separate the sql queries and data manipulation pure functions from eachother, and my code has benefitted. Now my code passes the color coding test described [here](https://youtu.be/WtdegIqQbrg?t=983).
