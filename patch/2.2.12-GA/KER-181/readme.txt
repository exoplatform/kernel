Summary

    * Status: PortalContainer.getConfigurationXML() returns duplicated configuration
    * CCP Issue: N/A Product Jira Issue: KER-181.
    * Complexity: low

The Proposal
Problem description

What is the problem to fix?

    * The browser falls when getting configurationXML in services management. Getting configuration XML file from services management, pcontainer is exacting. When you are following steps to reproduce: in each loop it's slower and slower. In the end the browser fall - Unresponsive script (on windows). In linux it's just slow, but the browser doesn't fall.

Fix description

Problem analysis

    * org.exoplatform.container.PortalContainer.getConfigurationXML() returns are duplicated each time it is called.

How is the problem fixed?

    * The method PortalContainer.getConfigurationXML() requires to merge the configuration of the RootContainer with the configuration of the Portal container to get the effiency. This merge is allowed by cloning the configuration of the RootContainer and adding the configuration of the portal container. The problem here was the fact that the object was badly cloned which had for consequence to grow up at each method call.
    * Fixed by rewriting clone() method.

Patch file: KER-181.patch

Tests to perform

Reproduction test
* Access to the MBean related to your PortalContainer (using the jconsole for example) then call several times the method getConfigurationXML(). 
** Before the patch application: the provided XML grows up.
** After the patch, the provided XML should not grow up.

Tests performed at DevLevel
* testGetConfigurationXML()

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

    * Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated.

Support Comment
*

QA Feedbacks
*
