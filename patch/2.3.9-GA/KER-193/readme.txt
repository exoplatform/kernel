Summary
	* Issue title: The full thread context should be transferred in case of asynchronous listener 
    	* CCP Issue:  N/A
    	* Product Jira Issue: KER-193 COR-256 JCR-1784  PLF-3287
    	* Complexity: High

Proposal

 
Problem description

What is the problem to fix?
	* Need to provide a way to dynamically transfer the important values stored into thread local variables from the original thread to the thread that will execute the task

Fix description

Problem analysis
	* Values stored in thread local variables not available from another thread

How is the problem fixed?
	* Allow to restore context from another thread

Tests to perform

Reproduction test
	* Need to provide a way to dynamically transfer the important values stored into thread local variables from the original thread to the thread that will execute the task

Tests performed at DevLevel
	* Functional testing

Tests performed at Support Level
	*

Tests performed at QA
	* Performance test

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	*

Changes in Selenium scripts 
	*

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:
	* Added new section with id=Kernel.ContainerConfiguration.ThreadContextHolder

Configuration changes

Configuration changes:
	* No

Will previous configuration continue to work?
	* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
	* Function or ClassName change:  No
	* Data (template, node type) migration/upgrade: No

Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	*

QA Feedbacks
	*
