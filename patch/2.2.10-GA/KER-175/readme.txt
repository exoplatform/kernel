Summary

    * Status: Find a way to have a name for MBeans of JBossCaches used by the JCR
    * CCP Issue: CCP-1032, Product Jira Issue: KER-175.
    * Complexity: Medium

The Proposal
Problem description

What is the problem to fix?

    * Find a way to have a name for MBeans of JBossCaches used by the JCR

Fix description

How is the problem fixed?

    * Set MBeans names of JBossCaches explicitly

Patch file: KER-175.patch

Tests to perform

Reproduction test
PROBLEM:

    * When monitoring the JCR with a JMX Tool (like VisualVM)
    * The JBossCaches used by the JCR have generated names, and no informations about what they are managing.

EXPECTED:

    * A name stable between two start up
    * An indication of the content managed by the cache

Tests performed at DevLevel
* Functional testing jcr projcects

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
* Patch approved.

Support Comment
*

QA Feedbacks
*
