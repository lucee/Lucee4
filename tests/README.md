Lucee-Tests
===========

Unit Tests for Lucee Server.

## Instructions
This Test Suite needs no addional installation.  To write your own testcases, check out the folder `/testcase-templates`, there you can find various testcase templates with readmes to all of them.


## Properties
In the template {repository}/tests/properties.cfm you must define the properties necessary to execute the testcases that are enviroment specific.

## Mapped Folders

To run the tests from a mapped folder, create the following two mappings in the server or web admin.

> This is usually done if you are running a Lucee server and want to point the tests to this folder.

    virtual:    /tests
    resource:   {repository}/tests
    primary:    Resource
    inspect:    Always
    
    virtual:    /testcases
    resource:   {repository}/tests/testcases
    primary:    Resource
    inspect:    Always
    
Then you can run the tests by calling lucee at `/tests/index.cfm`, for example:

    http://localhost:8888/lucee-tests/index.cfm
    
    
