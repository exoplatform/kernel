Summary
	* Issue title: Error of JDBC connection open due to RepositoryException and the datasource is closed due to SQL Exception
	* CCP Issue:  N/A
	* Product Jira Issue: KER-195.
	* Complexity: Low

Proposal

 
Problem description

What is the problem to fix?
	* DataSource is closed because close() was invoked CloseableDataSource instance

Fix description

Problem analysis
	* Dead session usage causes using closed datasources when repository is removed

How is the problem fixed?
	* Allow keep datasource opened due to special system property exo.jcr.prohibit.closed.datasource.usage

Tests to perform

Reproduction test
	* In cloud workspace, Error of JDBC connection open due to RepositoryException and the datasource is closed due to SQL Exception was actual about 30 minutes, then the tenant back to normal work. It looks like the datasource was cached somehow and has prevented the portal work until it was evicted.

Tests performed at DevLevel
	* Functional testing

Tests performed at Support Level
	*

Tests performed at QA
	*

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	* n/a

Changes in Selenium scripts 
	* n/a

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:
	* Related issue https://jira.exoplatform.org/browse/JCR-1788

Configuration changes

Configuration changes:
	* No

Will previous configuration continue to work?
	* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
	* Function or ClassName change: NO
	* Data (template, node type) migration/upgrade:  No

Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	*

QA Feedbacks
	*
