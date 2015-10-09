# A dream meeting to see what we want in KFF v1.0 #

_**Attending**_: Jan Aertsen, Harris Ward, Juan Jose Ortilles, Ricardo Pires, André Simões, Matt Casters

_**Main topic**_: have a dream meeting to see what we want in KFF v1.0

_**Discussed topics**_:
  * Add templated solutions/transformations to solve specific tasks.
  * Scripting: preferable not in KFF, solve the thing itself in PDI.
  * Plugins: promote plugins as standard steps.
  * Contribute to PDI directly to get things fixed.
  * Standardization: should facilitate, not block.
  * Parameter files:  use the KETTLE\_HOME directory to point to the correct configuration Andre says he already has a script to do it.
  * Using JNDI to configure database connections? Some objections were raised.  This is a topic to flesh out.

  * Directory structure:
  * KFF Configuration: some discussion on why it is needed vs scripting, another topic to  flesh out.
  * Scheduling: it stops with an error now if another job is still running, is this desired?
  * Batch processing is stopped: clean up of logging tables, restartability, Andre can perhaps present the solution already built perhaps in a next meeting
> > Matt will do some brainstorming around dependencies

  * Quick-start guides: Juan felt a bit lost during the first tries with KFF.

  * How it should work: drop the code in a folder and it should work.
  * A few topics for the mailing lists:
    * Configuration and connections
    * Logging and error handling (reporting on it, XLS, dashboard, etc)
    * Ease of use TODO lists

  * **Next meeting: TBD**

  * Matt wants to get a release going in Jan-2011
  * Andre wants more action, less talk.