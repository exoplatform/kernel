Summary

    * Status: Propose a maven profile for each supported configuration
    * CCP Issue: N/A, Product Jira Issue: KER-180.
    * Complexity: Low

The Proposal
Problem description

What is the problem to fix?
Changes for the kernel from issue JCR-1689.

See base issue https://jira.exoplatform.org/browse/JCR-1689

Fix description

How is the problem fixed?
  * Add new method PropertyConfigurator to work with the StandaloneContainer.
    If the system property PropertyManager.PROPERTIES_URL has been set properly, it will load the properties from the file and load them as system properties.

Patch file: KER-180.patch

Tests to perform

Reproduction test
  * No

Tests performed at DevLevel
  * No

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
*PM validated

Support Comment
*Validated by PM, on behalf of Support

QA Feedbacks
*
