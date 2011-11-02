Summary

    * Status: Provide a Util method to give an alternative to TimeZone.getTimeZone with less contention
    * CCP Issue: CCP-1118. Product Jira Issue: KER-178.
    * Complexity: Low

The Proposal
Problem description

What is the problem to fix?
Under heavy load we realized that the method java.util.TimeZone.getTimeZone(String ID) becomes a bottleneck so it is necessary to give an alternative with less contention as possible.

Fix description

How is the problem fixed?

* Avoid the usage of method java.util.TimeZone.getTimeZone(String ID). We use now a less synchronized method implemented in kernel project instead. 

Patch file: KER-178.patch

Tests to perform

Reproduction test
  * Activate the Concurrent GC during run of benchmark

Tests performed at DevLevel
  * Functional testing for all projects (kernel, core, ws, jcr)

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
  * No

Configuration changes

Configuration changes:
  * No

Will previous configuration continue to work?
  * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No

Is there a performance risk/cost?
  * No

Validation (PM/Support/QA)

PM Comment
* PM validated

Support Comment
*

QA Feedbacks
*
